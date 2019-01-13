package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;
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

import org.blendee.assist.Vargs;
import org.blendee.jdbc.BSQLException;
import org.blendee.jdbc.exception.CheckConstraintViolationException;
import org.blendee.jdbc.exception.UniqueConstraintViolationException;
import org.blendee.sql.AsyncRecorder;

import jp.ats.blackbox.model.InOut;
import jp.ats.blackbox.persistence.TransferComponent.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterResult;
import sqlassist.bb.bundles;
import sqlassist.bb.current_stocks;
import sqlassist.bb.groups;
import sqlassist.bb.jobs;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;
import sqlassist.bb.stocks;
import sqlassist.bb.transfers;
import sqlassist.bb.transfers_tags;
import sqlassist.bb.users;

/**
 * transfer操作クラス
 */
public class TransferHandler {

	private final AsyncRecorder recorder = new AsyncRecorder();

	private final UUID instanceId = SecurityValues.currentInstanceId();

	/**
	 * transfer登録処理
	 */
	public TransferRegisterResult register(UUID transferId, UUID userId, TransferRegisterRequest request) {
		var group = recorder.play(() -> new groups().SELECT(a -> a.ls(a.extension, a.$orgs().extension)))
			.fetch(request.group_id)
			.orElseThrow(() -> new DataNotFoundException(groups.$TABLE, request.group_id));

		var user = recorder.play(() -> new users().SELECT(r -> r.extension))
			.fetch(userId)
			.orElseThrow(() -> new DataNotFoundException(users.$TABLE, userId));

		var transfer = transfers.row();

		transfer.setId(transferId);
		transfer.setGroup_id(request.group_id);

		request.denied_id.ifPresent(v -> transfer.setDenied_id(v));

		transfer.setTransferred_at(request.transferred_at);

		request.restoredExtension.ifPresentOrElse(
			v -> transfer.setExtension(v),
			() -> request.extension.ifPresent(v -> transfer.setExtension(toJson(v))));

		transfer.setGroup_extension(group.getExtension());
		transfer.setOrg_extension(group.$orgs().getExtension());
		transfer.setUser_extension(user.getExtension());

		request.tags.ifPresent(v -> transfer.setTags(v));

		transfer.setInstance_id(instanceId);

		transfer.setCreated_by(userId);

		try {
			transfer.insert();
		} catch (UniqueConstraintViolationException e) {
			//同一groupで全く同一時刻に登録した場合、UNIQUE違反エラーとなるので再登録対象
			throw new Retry(e);
		}

		Arrays.stream(request.bundles).forEach(r -> registerBundle(userId, transferId, request.transferred_at, r));

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

		var result = new TransferRegisterResult();
		result.transferId = transferId;
		result.transferredAt = request.transferred_at;

		return result;
	}

	public TransferRegisterResult deny(UUID transferId, UUID userId, UUID denyTransferId) {
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
			denyTransferId)
			.forEach(transferOne -> {
				var transfer = transferOne.get();

				request.group_id = transfer.getGroup_id();
				request.denied_id = Optional.of(denyTransferId);
				request.transferred_at = transfer.getTransferred_at();
				request.restoredExtension = Optional.of(transfer.getExtension());

				try {
					request.tags = Optional.of(Utils.restoreTags(transfer.getTags()));
				} catch (SQLException e) {
					throw new BSQLException(e);
				}

				transferOne.many().forEach(bundleOne -> {
					var nodes = new LinkedList<NodeRegisterRequest>();

					var bundleRequest = new BundleRegisterRequest();

					bundleRequest.restoredExtension = Optional.of(bundleOne.get().getExtension());

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
						nodeRequest.grants_infinity = Optional.of(node.getGrants_unlimited());
						nodeRequest.restoredExtension = Optional.of(node.getExtension());

						nodes.add(nodeRequest);
					});

					bundleRequest.nodes = nodes.toArray(new NodeRegisterRequest[nodes.size()]);

					bundles.add(bundleRequest);
				});

				request.bundles = bundles.toArray(new BundleRegisterRequest[bundles.size()]);
			});

		return register(transferId, userId, request);
	}

	/**
	 * bundle登録処理
	 */
	private void registerBundle(
		UUID userId,
		UUID transferId,
		Timestamp transferredAt,
		BundleRegisterRequest request) {
		var bundle = bundles.row();

		UUID bundleId = UUID.randomUUID();

		bundle.setId(bundleId);
		bundle.setTransfer_id(transferId);
		request.restoredExtension
			.ifPresentOrElse(
				v -> bundle.setExtension(v),
				() -> request.extension.ifPresent(v -> bundle.setExtension(toJson(v))));

		bundle.insert();

		Arrays.stream(request.nodes).forEach(r -> registerNode(userId, bundleId, transferredAt, r));
	}

	/**
	 * node登録処理
	 */
	private void registerNode(
		UUID userId,
		UUID bundleId,
		Timestamp transferredAt,
		NodeRegisterRequest request) {
		//stockが既に存在すればそれを使う
		//なければ新たに登録
		var stock = recorder.play(
			() -> selectedStocks()
				.WHERE(a -> a.group_id.eq($UUID).AND.item_id.eq($UUID).AND.owner_id.eq($UUID).AND.location_id.eq($UUID).AND.status_id.eq($UUID)),
			request.group_id,
			request.item_id,
			request.owner_id,
			request.location_id,
			request.status_id)
			.willUnique()
			.orElseGet(() -> registerStock(userId, request));

		UUID stockId = stock.getId();

		var node = nodes.row();

		UUID nodeId = UUID.randomUUID();

		node.setId(nodeId);
		node.setBundle_id(bundleId);
		//nodeではgroup_idを持たないが、requestが持つgroup_idはstockに格納しており、それが在庫の所属グループを表す
		node.setStock_id(stockId);
		node.setIn_out(request.in_out.value);
		node.setQuantity(request.quantity);

		request.grants_infinity.ifPresent(v -> node.setGrants_unlimited(v));

		request.restoredExtension
			.ifPresentOrElse(
				v -> node.setExtension(v),
				() -> request.extension.ifPresent(v -> node.setExtension(toJson(v))));

		node.setGroup_extension(stock.$groups().getExtension());
		node.setItem_extension(stock.$items().getExtension());
		node.setOwner_extension(stock.$owners().getExtension());
		node.setLocation_extension(stock.$locations().getExtension());
		node.setStatus_extension(stock.$statuses().getExtension());

		node.insert();

		//この在庫の数量と無制限タイプの在庫かを知るため、直近のsnapshotを取得
		JustBefore justBefore = recorder.play(
			() -> new snapshots()
				.SELECT(a -> a.ls(a.total, a.unlimited))
				.WHERE(
					a -> a.$nodes().$bundles().$transfers().transferred_at.eq(
						new nodes()
							.SELECT(sa -> sa.MAX(sa.$bundles().$transfers().transferred_at))
							.WHERE(sa -> sa.stock_id.eq($UUID).AND.$bundles().$transfers().transferred_at.le($TIMESTAMP))))
				.ORDER_BY(a -> a.$nodes().$bundles().$transfers().created_at.DESC), //同一時刻であればtransfers.created_atが最近のもの
			stockId,
			transferredAt).aggregateAndGet(r -> {
				var container = new JustBefore();
				while (r.next()) {
					container.total = r.getBigDecimal(1);
					container.infinity = r.getBoolean(2);
					return container;
				}

				container.total = BigDecimal.ZERO;
				container.infinity = false;

				return container;
			});

		BigDecimal total = request.in_out.calcurate(justBefore.total, request.quantity);

		//移動した結果数量がマイナスになる場合エラー
		//ただし無制限設定がされていればOK
		if (!justBefore.infinity && total.compareTo(BigDecimal.ZERO) < 0) throw new MinusTotalException();

		//直前のsnapshotが無制限の場合、以降すべてのsnapshotが無制限になるので引き継ぐ
		//そうでなければ今回のリクエストに従う
		boolean infinity = justBefore.infinity ? true : request.grants_infinity.orElse(false);

		recorder.play(
			() -> new snapshots().insertStatement(
				a -> a.INSERT(a.id, a.unlimited, a.total, a.updated_by)
					.VALUES(
						$UUID,
						$BOOLEAN,
						$BIGDECIMAL,
						$UUID)),
			nodeId,
			infinity,
			total,
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
								new nodes()
									.SELECT(sa -> sa.id)
									.WHERE(swa -> swa.stock_id.eq($UUID).AND.$bundles().$transfers().transferred_at.ge($TIMESTAMP))))),
				infinity,
				request.in_out.normalize(request.quantity),
				stockId,
				transferredAt)
				.execute();
		} catch (CheckConstraintViolationException e) {
			//未来のsnapshotで数量がマイナスになった
			throw new MinusTotalException();
		}
	}

	/**
	 * 直前のsnapshotの情報を保持するコンテナ
	 */
	private static class JustBefore {

		private BigDecimal total;

		private boolean infinity;
	}

	/**
	 * stock登録処理
	 */
	private stocks.Row registerStock(UUID userId, TransferComponent.NodeRegisterRequest request) {
		UUID stockId = UUID.randomUUID();

		recorder.play(
			() -> new stocks().insertStatement(
				a -> a
					.INSERT(
						a.id,
						a.group_id,
						a.item_id,
						a.owner_id,
						a.location_id,
						a.status_id,
						a.created_by)
					.VALUES(
						$UUID,
						$UUID,
						$UUID,
						$UUID,
						$UUID,
						$UUID,
						$UUID)),
			stockId,
			request.group_id,
			request.item_id,
			request.owner_id,
			request.location_id,
			request.status_id,
			userId)
			.execute();

		recorder.play(
			() -> new current_stocks().insertStatement(
				//後でjobから更新されるのでinfinityはとりあえずfalse、totalは0
				a -> a.INSERT(a.id, a.unlimited, a.total).VALUES($UUID, $BOOLEAN, $INT)),
			stockId,
			false,
			0).execute();

		//関連情報取得のため改めて検索
		return recorder.play(() -> selectedStocks()).fetch(stockId).get();
	}

	private static stocks selectedStocks() {
		return new stocks().SELECT(
			a -> a.ls(
				a.id,
				a.$groups().extension,
				a.$items().extension,
				a.$owners().extension,
				a.$locations().extension,
				a.$statuses().extension));
	}
}
