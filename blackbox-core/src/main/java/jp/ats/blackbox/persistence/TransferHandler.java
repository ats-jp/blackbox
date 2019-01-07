package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;
import static org.blendee.util.Placeholder.$BIGDECIMAL;
import static org.blendee.util.Placeholder.$BOOLEAN;
import static org.blendee.util.Placeholder.$INT;
import static org.blendee.util.Placeholder.$LONG;
import static org.blendee.util.Placeholder.$TIMESTAMP;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

import org.blendee.assist.Vargs;
import org.blendee.dialect.postgresql.ReturningUtilities;
import org.blendee.jdbc.exception.CheckConstraintViolationException;
import org.blendee.util.AsyncRecorder;

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

	/**
	 * transfer登録処理
	 */
	public TransferRegisterResult register(long userId, TransferRegisterRequest request) {
		var group = recorder.play(() -> new groups().SELECT(a -> a.ls(a.extension, a.$orgs().extension)))
			.fetch(request.group_id)
			.orElseThrow(() -> new DataNotFoundException());

		var user = recorder.play(() -> new users().SELECT(r -> r.extension))
			.fetch(userId)
			.orElseThrow(() -> new DataNotFoundException());

		long transferId = ReturningUtilities.insertAndReturn(
			transfers.$TABLE,
			u -> {
				u.add(transfers.group_id, request.group_id);
				request.denied_id.ifPresent(v -> u.add(transfers.denied_id, v));
				u.add(transfers.transferred_at, request.transferred_at);

				request.restoredExtension
					.ifPresentOrElse(
						v -> u.add(transfers.extension, v),
						() -> request.extension.ifPresent(v -> u.add(transfers.extension, toJson(v))));

				u.add(transfers.group_extension, group.getExtension());
				u.add(transfers.org_extension, group.$orgs().getExtension());
				u.add(transfers.user_extension, user.getExtension());
				request.tags.ifPresent(v -> u.add(transfers.tags, v));
				u.add(transfers.created_by, userId);
			},
			r -> r.getLong(transfers.id),
			transfers.id);

		Arrays.stream(request.bundles).forEach(r -> registerBundle(userId, transferId, request.transferred_at, r));

		request.tags.ifPresent(tags -> TagHandler.stickTags(tags, tagIds -> {
			var table = new transfers_tags();
			tagIds.forEach(tagId -> {
				recorder.play(() -> table.INSERT().VALUES($LONG, $LONG), transferId, tagId).execute();
			});
		}));

		//jobを登録し、別プロセスで現在数量を更新させる
		recorder.play(() -> new jobs().INSERT(a -> a.id).VALUES($LONG), transferId).execute();

		var result = new TransferRegisterResult();
		result.transferId = transferId;
		result.transferredAt = request.transferred_at;

		return result;
	}

	public TransferRegisterResult deny(long userId, long transferId) {
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
						a.grants_infinity,
						a.extension);
				})
				.WHERE(a -> a.$bundles().transfer_id.eq($LONG))
				.assist()
				.$bundles()
				.$transfers()
				.intercept(),
			transferId)
			.forEach(transferOne -> {
				var transfer = transferOne.get();

				request.group_id = transfer.getGroup_id();
				request.denied_id = Optional.of(transferId);
				request.transferred_at = transfer.getTransferred_at();
				request.restoredExtension = Optional.of(transfer.getExtension());

				try {
					request.tags = Optional.of(Utils.restoreTags(transfer.getTags()));
				} catch (SQLException e) {
					throw new Error(e);
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
						nodeRequest.grants_infinity = Optional.of(node.getGrants_infinity());
						nodeRequest.restoredExtension = Optional.of(node.getExtension());

						nodes.add(nodeRequest);
					});

					bundleRequest.nodes = nodes.toArray(new NodeRegisterRequest[nodes.size()]);

					bundles.add(bundleRequest);
				});

				request.bundles = bundles.toArray(new BundleRegisterRequest[bundles.size()]);
			});

		return register(userId, request);
	}

	/**
	 * bundle登録処理
	 */
	private void registerBundle(
		long userId,
		long transferId,
		Timestamp transferredAt,
		BundleRegisterRequest request) {
		long bundleId = ReturningUtilities.insertAndReturn(
			bundles.$TABLE,
			u -> {
				u.add(bundles.transfer_id, transferId);
				request.restoredExtension
					.ifPresentOrElse(
						v -> u.add(transfers.extension, v),
						() -> request.extension.ifPresent(v -> u.add(transfers.extension, toJson(v))));

			},
			r -> r.getLong(bundles.id),
			bundles.id);

		Arrays.stream(request.nodes).forEach(r -> registerNode(userId, bundleId, transferredAt, r));
	}

	/**
	 * node登録処理
	 */
	private void registerNode(
		long userId,
		long bundleId,
		Timestamp transferredAt,
		NodeRegisterRequest request) {
		//stockが既に存在すればそれを使う
		//なければ新たに登録
		var stock = recorder.play(
			() -> selectedStocks()
				.WHERE(a -> a.group_id.eq($LONG).AND.item_id.eq($LONG).AND.owner_id.eq($LONG).AND.location_id.eq($LONG).AND.status_id.eq($LONG)),
			request.group_id,
			request.item_id,
			request.owner_id,
			request.location_id,
			request.status_id)
			.willUnique()
			.orElseGet(() -> registerStock(userId, request));

		long stockId = stock.getId();

		//nodeではgroup_idを持たないが、requestが持つgroup_idはstockに格納しており、それが在庫の所属グループを表す
		long nodeId = ReturningUtilities.insertAndReturn(
			nodes.$TABLE,
			u -> {
				u.add(nodes.bundle_id, bundleId);
				u.add(nodes.stock_id, stockId);
				u.add(nodes.in_out, request.in_out.value);
				u.add(nodes.quantity, request.quantity);
				request.grants_infinity.ifPresent(v -> u.add(nodes.grants_infinity, v));
				request.restoredExtension
					.ifPresentOrElse(
						v -> u.add(transfers.extension, v),
						() -> request.extension.ifPresent(v -> u.add(transfers.extension, toJson(v))));
				u.add(nodes.group_extension, stock.$groups().getExtension());
				u.add(nodes.item_extension, stock.$items().getExtension());
				u.add(nodes.owner_extension, stock.$owners().getExtension());
				u.add(nodes.location_extension, stock.$locations().getExtension());
				u.add(nodes.status_extension, stock.$statuses().getExtension());
			},
			r -> r.getLong(bundles.id),
			bundles.id);

		//この在庫の数量と無制限タイプの在庫かを知るため、直近のsnapshotを取得
		JustBefore justBefore = recorder.play(
			() -> new snapshots()
				.SELECT(a -> a.ls(a.total, a.infinity))
				.WHERE(
					a -> a.$nodes().$bundles().$transfers().transferred_at.eq(
						new nodes()
							.SELECT(sa -> sa.MAX(sa.$bundles().$transfers().transferred_at))
							.WHERE(sa -> sa.stock_id.eq($LONG).AND.$bundles().$transfers().transferred_at.le($TIMESTAMP))))
				.ORDER_BY(a -> a.id.DESC), //同一時刻であればidが最新のもの
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
				a -> a.INSERT(a.id, a.infinity, a.total, a.updated_by)
					.VALUES(
						$LONG,
						$BOOLEAN,
						$BIGDECIMAL,
						$LONG)),
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
						a.infinity.set("{0} OR ?", Vargs.of(a.infinity), Vargs.of($BOOLEAN)),
						//自身の数に今回の移動数量を正規化してプラス
						a.total.set("{0} + ?", Vargs.of(a.total), Vargs.of($BIGDECIMAL)))
						.WHERE(
							wa -> wa.id.IN(
								new nodes()
									.SELECT(sa -> sa.id)
									.WHERE(swa -> swa.stock_id.eq($LONG).AND.$bundles().$transfers().transferred_at.ge($TIMESTAMP))))),
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
	private stocks.Row registerStock(long userId, TransferComponent.NodeRegisterRequest request) {
		long stockId = ReturningUtilities.insertAndReturn(
			stocks.$TABLE,
			u -> {
				u.add(stocks.group_id, request.group_id);
				u.add(stocks.item_id, request.item_id);
				u.add(stocks.owner_id, request.owner_id);
				u.add(stocks.location_id, request.location_id);
				u.add(stocks.status_id, request.status_id);
				u.add(stocks.created_by, userId);
			},
			r -> r.getLong(transfers.id),
			transfers.id);

		recorder.play(
			() -> new current_stocks().insertStatement(
				//後でjobから更新されるのでinfinityはとりあえずfalse、totalは0
				a -> a.INSERT(a.id, a.infinity, a.total).VALUES($LONG, $BOOLEAN, $INT)),
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
