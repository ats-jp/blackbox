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
import java.util.regex.Pattern;

import org.blendee.assist.Vargs;
import org.blendee.jdbc.BSQLException;
import org.blendee.jdbc.exception.CheckConstraintViolationException;
import org.blendee.jdbc.exception.UniqueConstraintViolationException;
import org.blendee.sql.Recorder;

import com.google.gson.Gson;

import jp.ats.blackbox.persistence.TransferComponent.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferDenyRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
import sqlassist.bb.bundles;
import sqlassist.bb.jobs;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;
import sqlassist.bb.transfers;
import sqlassist.bb.transfers_tags;

/**
 * transfer操作クラス
 */
public class TransferHandler {

	private final Recorder recorder = Recorder.newAsyncInstance();

	private UUID instanceId;

	private UUID instanceId() {
		if (instanceId == null) instanceId = SecurityValues.currentInstanceId();
		return instanceId;
	}

	private int nodeSeq;

	private class Transfer extends transfers.Row implements TransferPreparer.Transfer {}

	/**
	 * transfer登録処理
	 */
	public void register(UUID transferId, UUID userId, TransferRegisterRequest request) {
		//初期化
		nodeSeq = 0;

		var transfer = new Transfer();
		TransferPreparer.prepareTransfer(transferId, userId, instanceId(), request, transfer, recorder);

		try {
			transfer.insert();
		} catch (UniqueConstraintViolationException e) {
			//同一groupで全く同一時刻に登録した場合、UNIQUE違反エラーとなるので再登録対象
			throw new Retry(e);
		} catch (BSQLException e) {
			//既に締められているグループの場合
			var matcher = Pattern.compile("closed_check\\(\\): (\\{[^\\}]+\\})").matcher(e.getMessage());

			if (!matcher.find()) throw e;

			ClosedCheckError error = new Gson().fromJson(matcher.group(1), ClosedCheckError.class);
			throw new AlreadyClosedGroupException(error, e);
		}

		Arrays.stream(request.bundles).forEach(r -> registerBundle(userId, transferId, request.group_id, request.transferred_at, r));

		try {
			request.tags.ifPresent(tags -> TagHandler.stickTags(tags, tagIds -> {
				var table = new transfers_tags();
				tagIds.forEach(tagId -> {
					recorder.play(() -> table.INSERT().VALUES($UUID, $UUID), transferId, tagId).execute();
				});
			}));
		} catch (UniqueConstraintViolationException e) {
			//他のorg, groupのtransfer登録処理が僅差で同じtagを登録した場合エラーとなるので再登録対象
			throw new Retry(e);
		}

		//jobを登録し、別プロセスで現在数量を更新させる
		recorder.play(() -> new jobs().INSERT(a -> a.id).VALUES($UUID), transferId).execute();
	}

	public static class ClosedCheckError {

		public String id;

		public String group_id;

		public String transferred_at;

		public String closed_at;
	}

	private class Bundle extends bundles.Row implements TransferPreparer.Bundle {}

	/**
	 * bundle登録処理
	 */
	private void registerBundle(
		UUID userId,
		UUID transferId,
		UUID groupId,
		Timestamp transferredAt,
		BundleRegisterRequest request) {
		var bundle = new Bundle();

		UUID bundleId = UUID.randomUUID();

		TransferPreparer.prepareBundle(transferId, bundleId, request, bundle);

		bundle.insert();

		Arrays.stream(request.nodes).forEach(r -> registerNode(userId, bundleId, groupId, transferredAt, r));
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
		NodeRegisterRequest request) {
		UUID nodeId = UUID.randomUUID();
		var node = new Node();
		UUID stockId = TransferPreparer.prepareNode(bundleId, nodeId, userId, request, node, ++nodeSeq, recorder);

		node.insert();

		//この在庫の数量と無制限タイプの在庫かを知るため、直近のsnapshotを取得
		JustBeforeSnapshot justBefore = getJustBeforeSnapshot(stockId, transferredAt, recorder);

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
						a.stock_id,
						a.transfer_group_id,
						a.transferred_at,
						a.node_seq,
						a.updated_by)
					.VALUES(
						$UUID,
						$BOOLEAN,
						$BIGDECIMAL,
						$UUID,
						$UUID,
						$TIMESTAMP,
						$INT,
						$UUID)),
			nodeId,
			unlimited,
			total,
			stockId,
			groupId,
			transferredAt,
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
									.WHERE(swa -> swa.stock_id.eq($UUID).AND.transferred_at.gt($TIMESTAMP))))),
				unlimited,
				request.in_out.normalize(request.quantity),
				stockId,
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
					a -> a.stock_id.eq($UUID).AND.transferred_at.eq(
						new snapshots()
							.SELECT(sa -> sa.MAX(sa.transferred_at))
							.WHERE(sa -> sa.stock_id.eq($UUID).AND.in_search_scope.eq(true).AND.transferred_at.le($TIMESTAMP))))
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
		var request = new TransferRegisterRequest();

		var bundles = new LinkedList<BundleRegisterRequest>();

		recorder.play(
			() -> new nodes().selectClause(
				a -> {
					var bundlesAssist = a.$bundles();
					var transfersAssist = bundlesAssist.$transfers();
					var stocksAssist = a.$stocks();

					a.SELECT(
						transfersAssist.group_id,
						transfersAssist.transferred_at,
						transfersAssist.extension,
						transfersAssist.tags,
						bundlesAssist.extension,
						stocksAssist.group_id,
						stocksAssist.item_id,
						stocksAssist.owner_id,
						stocksAssist.location_id,
						stocksAssist.status_id,
						a.in_out,
						a.quantity,
						a.grants_unlimited,
						a.extension);
				})
				.WHERE(a -> a.$bundles().transfer_id.eq($UUID))
				.assist()
				.$bundles()
				.$transfers()
				.intercept(),
			denyRequest.denyId)
			.forEach(transferOne -> {
				var transfer = transferOne.get();

				request.group_id = transfer.getGroup_id();
				request.denied_id = Optional.of(denyRequest.denyId);
				request.deny_reason = denyRequest.denyReason;
				request.transferred_at = transfer.getTransferred_at();
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

						var stock = node.$stocks();

						nodeRequest.group_id = stock.getGroup_id();
						nodeRequest.item_id = stock.getItem_id();
						nodeRequest.owner_id = stock.getOwner_id();
						nodeRequest.location_id = stock.getLocation_id();
						nodeRequest.status_id = stock.getStatus_id();
						nodeRequest.in_out = InOut.of(node.getIn_out()).reverse();
						nodeRequest.quantity = node.getQuantity();
						nodeRequest.grants_unlimited = Optional.of(node.getGrants_unlimited());
						nodeRequest.restored_extension = Optional.of(node.getExtension());

						nodes.add(nodeRequest);
					});

					bundleRequest.nodes = nodes.toArray(new NodeRegisterRequest[nodes.size()]);
				});

				request.bundles = bundles.toArray(new BundleRegisterRequest[bundles.size()]);
			});

		register(transferId, userId, request);

		return request.transferred_at;
	}
}
