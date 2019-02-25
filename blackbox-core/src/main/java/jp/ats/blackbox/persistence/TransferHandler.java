package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$BIGDECIMAL;
import static org.blendee.sql.Placeholder.$BOOLEAN;
import static org.blendee.sql.Placeholder.$INT;
import static org.blendee.sql.Placeholder.$TIMESTAMP;
import static org.blendee.sql.Placeholder.$UUID;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.blendee.assist.Vargs;
import org.blendee.jdbc.BSQLException;
import org.blendee.jdbc.exception.CheckConstraintViolationException;
import org.blendee.sql.Recorder;

import com.google.gson.Gson;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.TagExecutor;
import sqlassist.bb.details;
import sqlassist.bb.jobs;
import sqlassist.bb.journal_batches;
import sqlassist.bb.journals;
import sqlassist.bb.journals_tags;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;

/**
 * transfer操作クラス
 */
public class TransferHandler {

	/**
	 * transfer登録に必要な情報クラス
	 */
	public static class TransferRegisterRequest {

		/**
		 * このtransferが属するグループ
		 * 必須
		 */
		public UUID group_id;

		/**
		 * 移動時刻
		 * 必須
		 */
		public Timestamp transferred_at;

		/**
		 * 打消し元のtransfer_id
		 */
		public Optional<UUID> denied_id = Optional.empty();

		public Optional<String> deny_reason = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> extension = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_extension = Optional.empty();

		/**
		 * 検索用タグ
		 */
		public Optional<String[]> tags = Optional.empty();

		/**
		 * 配下のbundle
		 * 必須
		 */
		public BundleRegisterRequest[] bundles;
	}

	/**
	 * bundle登録に必要な情報クラス
	 */
	public static class BundleRegisterRequest {

		/**
		 * 追加情報JSON
		 */
		public Optional<String> extension = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_extension = Optional.empty();

		/**
		 * 配下のnode
		 * 必須
		 */
		public NodeRegisterRequest[] nodes;
	}

	/**
	 * node登録に必要な情報クラス
	 */
	public static class NodeRegisterRequest {

		/**
		 * unit
		 * 必須
		 */
		public UUID unit_id;

		/**
		 * 入出庫タイプ
		 */
		public InOut in_out;

		/**
		 * 数量
		 */
		public BigDecimal quantity;

		/**
		 * これ以降数量無制限を設定するか
		 * 数量無制限の場合、通常はunit登録時からtrueにしておく
		 */
		public Optional<Boolean> grants_unlimited = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> extension = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_extension = Optional.empty();

		/**
		 * unit追加情報JSON
		 */
		public Optional<String> unit_extension = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_unit_extension = Optional.empty();
	}

	/**
	 * transfer打消し
	 *
	 */
	public static class TransferDenyRequest {

		public UUID deny_id;

		public Optional<String> deny_reason = Optional.empty();
	}

	private final Recorder recorder;

	public TransferHandler(Recorder recorder) {
		this.recorder = recorder;
	}

	private long time = System.currentTimeMillis();

	private Timestamp uniqueTime() {
		while (true) {
			var current = System.currentTimeMillis();
			if (current == time) {
				Thread.yield();
				continue;
			}

			time = current;

			return new Timestamp(time);
		}
	}

	private UUID instanceId;

	private UUID instanceId() {
		if (instanceId == null) instanceId = SecurityValues.currentInstanceId();
		return instanceId;
	}

	private int nodeSeq;

	private class Transfer extends journals.Row implements TransferPreparer.Transfer {}

	/**
	 * transfer登録処理
	 */
	public void register(UUID transferId, UUID batchId, UUID userId, TransferRegisterRequest request) {
		//初期化
		nodeSeq = 0;

		var transfer = new Transfer();

		var createdAt = uniqueTime();

		TransferPreparer.prepareTransfer(transferId, instanceId(), batchId, userId, request, createdAt, transfer, recorder);

		try {
			transfer.insert();
		} catch (BSQLException e) {
			//既に締められているグループの場合
			var matcher = Pattern.compile("closed_check\\(\\): (\\{[^\\}]+\\})").matcher(e.getMessage());

			if (!matcher.find()) throw e;

			ClosedCheckError error = new Gson().fromJson(matcher.group(1), ClosedCheckError.class);
			throw new AlreadyClosedGroupException(error, e);
		}

		request.tags.ifPresent(tags -> TagExecutor.stickTags(tags, tagId -> {
			recorder.play(() -> new journals_tags().INSERT().VALUES($UUID, $UUID), transferId, tagId).execute();
		}));

		Arrays.stream(request.bundles)
			.forEach(
				r -> registerBundle(
					userId,
					transferId,
					request.group_id,
					request.transferred_at,
					createdAt,
					r));

		//jobを登録し、別プロセスで現在数量を更新させる
		recorder.play(() -> new jobs().INSERT(a -> a.id).VALUES($UUID), transferId).execute();
	}

	public static class ClosedCheckError {

		public String id;

		public String group_id;

		public String transferred_at;

		public String closed_at;
	}

	private class Bundle extends details.Row implements TransferPreparer.Bundle {}

	/**
	 * bundle登録処理
	 */
	private void registerBundle(
		UUID userId,
		UUID transferId,
		UUID groupId,
		Timestamp transferredAt,
		Timestamp createdAt,
		BundleRegisterRequest request) {
		var bundle = new Bundle();

		UUID bundleId = UUID.randomUUID();

		TransferPreparer.prepareBundle(transferId, bundleId, request, bundle);

		bundle.insert();

		Arrays.stream(request.nodes)
			.forEach(
				r -> registerNode(
					userId,
					bundleId,
					groupId,
					transferredAt,
					createdAt,
					r));
	}

	private class Node extends nodes.Row implements TransferPreparer.Node {}

	/**
	 * node登録処理
	 */
	private void registerNode(
		UUID userId,
		UUID bundleId,
		UUID groupId,
		Timestamp transferredAt,
		Timestamp createdAt,
		NodeRegisterRequest request) {
		UUID nodeId = UUID.randomUUID();
		var node = new Node();
		TransferPreparer.prepareNode(bundleId, nodeId, userId, request, node, ++nodeSeq, recorder);

		node.insert();

		//この在庫の数量と無制限タイプの在庫かを知るため、直近のsnapshotを取得
		JustBeforeSnapshot justBefore = getJustBeforeSnapshot(request.unit_id, transferredAt, recorder);

		BigDecimal total = request.in_out.calcurate(justBefore.total, request.quantity);

		//移動した結果数量がマイナスになる場合エラー
		//ただし無制限設定がされていればOK
		if (!justBefore.unlimited && total.compareTo(BigDecimal.ZERO) < 0) throw new MinusTotalException();

		//直前のsnapshotが無制限の場合、以降すべてのsnapshotが無制限になるので引き継ぐ
		//そうでなければ今回のリクエストに従う
		boolean unlimited = justBefore.unlimited ? true : request.grants_unlimited.orElse(false);

		recorder.play(
			() -> new snapshots().insertStatement(
				a -> a
					.INSERT(
						a.id,
						a.unlimited,
						a.total,
						a.unit_id,
						a.journal_group_id,
						a.fixed_at,
						a.created_at,
						a.node_seq,
						a.updated_by)
					.VALUES(
						$UUID,
						$BOOLEAN,
						$BIGDECIMAL,
						$UUID,
						$UUID,
						$TIMESTAMP,
						$TIMESTAMP,
						$INT,
						$UUID)),
			nodeId,
			unlimited,
			total,
			request.unit_id,
			groupId,
			transferredAt,
			createdAt,
			nodeSeq,
			userId)
			.execute();

		//登録以降のsnapshotの数量と無制限設定を更新
		try {
			recorder.play(
				() -> new snapshots().updateStatement(
					a -> a.UPDATE(
						//一度trueになったらずっとそのままtrue
						a.unlimited.set("{0} OR ?", Vargs.of(a.unlimited), Vargs.of($BOOLEAN)),
						//自身の数に今回の移動数量を正規化してプラス
						a.total.set("{0} + ?", Vargs.of(a.total), Vargs.of($BIGDECIMAL)))
						.WHERE(
							wa -> wa.id.IN(
								new snapshots()
									.SELECT(sa -> sa.id)
									//transferred_atが等しいものの最新は自分なので、それ以降のものに対して処理を行う
									.WHERE(swa -> swa.unit_id.eq($UUID).AND.fixed_at.gt($TIMESTAMP))))),
				unlimited,
				request.in_out.relativize(request.quantity),
				request.unit_id,
				transferredAt)
				.execute();
		} catch (CheckConstraintViolationException e) {
			//未来のsnapshotで数量がマイナスになった
			throw new MinusTotalException();
		}
	}

	//直近のsnapshotを取得
	static JustBeforeSnapshot getJustBeforeSnapshot(UUID stockId, Timestamp transferredAt, Recorder recorder) {
		return recorder.play(
			() -> new snapshots()
				.SELECT(a -> a.ls(a.total, a.unlimited))
				.WHERE(
					a -> a.unit_id.eq($UUID).AND.fixed_at.eq(
						new snapshots()
							.SELECT(sa -> sa.MAX(sa.fixed_at))
							.WHERE(sa -> sa.unit_id.eq($UUID).AND.in_search_scope.eq(true).AND.fixed_at.le($TIMESTAMP))))
				.ORDER_BY(
					a -> a.ls(
						a.created_at.DESC, //同一時刻であればcreated_atが最近のもの
						a.node_seq.DESC)), //created_atが等しければ同一伝票、同一伝票内であれば生成順
			stockId,
			stockId,
			transferredAt).aggregateAndGet(r -> {
				var container = new JustBeforeSnapshot();
				while (r.next()) {
					container.total = r.getBigDecimal(1);
					container.unlimited = r.getBoolean(2);
					return container;
				}

				container.total = BigDecimal.ZERO;
				container.unlimited = false;

				return container;
			});
	}

	/**
	 * 直前のsnapshotの情報を保持するコンテナ
	 */
	static class JustBeforeSnapshot {

		BigDecimal total;

		boolean unlimited;
	}

	public Timestamp deny(UUID transferId, UUID userId, TransferDenyRequest denyRequest) {
		var request = pickup(denyRequest.deny_id, r -> {
			r.denied_id = Optional.of(denyRequest.deny_id);
			r.deny_reason = denyRequest.deny_reason;
		}, true);

		register(transferId, U.NULL_ID, userId, request);

		return request.transferred_at;
	}

	public TransferRegisterRequest pickup(UUID transferId) {
		return pickup(transferId, r -> {}, false);
	}

	private TransferRegisterRequest pickup(
		UUID transferId,
		Consumer<TransferRegisterRequest> transferRequestDecorator,
		boolean reverse) {
		var request = new TransferRegisterRequest();

		var bundles = new LinkedList<BundleRegisterRequest>();

		recorder.play(
			() -> new nodes().selectClause(
				a -> {
					var bundlesAssist = a.$details();
					var transfersAssist = bundlesAssist.$journals();
					var stocksAssist = a.$units();

					a.SELECT(
						transfersAssist.group_id,
						transfersAssist.fixed_at,
						transfersAssist.extension,
						transfersAssist.tags,
						bundlesAssist.extension,
						stocksAssist.group_id,
						a.unit_id,
						a.in_out,
						a.quantity,
						a.grants_unlimited,
						a.extension);
				})
				.WHERE(a -> a.$details().journal_id.eq($UUID))
				.assist()
				.$details()
				.$journals()
				.intercept(),
			transferId)
			.forEach(transferOne -> {
				var transfer = transferOne.get();

				request.group_id = transfer.getGroup_id();

				transferRequestDecorator.accept(request);

				request.transferred_at = transfer.getFixed_at();
				request.restored_extension = Optional.of(transfer.getExtension());

				try {
					request.tags = Optional.of(Utils.restoreTags(transfer.getTags()));
				} catch (SQLException e) {
					throw new BSQLException(e);
				}

				transferOne.many().forEach(bundleOne -> {
					var nodes = new LinkedList<NodeRegisterRequest>();

					var bundleRequest = new BundleRegisterRequest();

					bundleRequest.restored_extension = Optional.of(bundleOne.get().getExtension());

					bundles.add(bundleRequest);

					bundleOne.many().forEach(nodeOne -> {
						var node = nodeOne.get();

						var nodeRequest = new NodeRegisterRequest();

						nodeRequest.unit_id = node.getUnit_id();

						var inOut = InOut.of(node.getIn_out());

						nodeRequest.in_out = reverse ? inOut.reverse() : inOut;
						nodeRequest.quantity = node.getQuantity();
						nodeRequest.grants_unlimited = Optional.of(node.getGrants_unlimited());
						nodeRequest.restored_extension = Optional.of(node.getExtension());

						nodes.add(nodeRequest);
					});

					bundleRequest.nodes = nodes.toArray(new NodeRegisterRequest[nodes.size()]);
				});

				request.bundles = bundles.toArray(new BundleRegisterRequest[bundles.size()]);
			});

		return request;
	}

	public void registerBatch(UUID batchId, UUID userId) {
		recorder.play(
			() -> new journal_batches().insertStatement(
				a -> a.INSERT(a.id, a.created_by).VALUES($UUID, $UUID)),
			batchId,
			userId).execute();
	}
}
