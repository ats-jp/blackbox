package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

import org.blendee.assist.AnonymousTable;
import org.blendee.assist.Vargs;
import org.blendee.dialect.postgresql.ReturningUtilities;
import org.blendee.jdbc.exception.CheckConstraintViolationException;

import jp.ats.blackbox.persistence.TransferComponent.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
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

	/**
	 * transfer登録処理
	 */
	public static long register(TransferRegisterRequest request) {
		long userId = User.currentUserId();

		var group = new groups().SELECT(a -> a.ls(a.extension, a.$orgs().extension))
			.fetch(request.group_id)
			.orElseThrow(() -> new DataNotFoundException());

		var user = new users().SELECT(r -> r.extension)
			.fetch(userId)
			.orElseThrow(() -> new DataNotFoundException());

		long transferId = ReturningUtilities.insertAndReturn(
			transfers.$TABLE,
			u -> {
				u.add(transfers.group_id, request.group_id);
				request.denied_id.ifPresent(v -> u.add(transfers.denied_id, v));
				u.add(transfers.transferred_at, request.transferred_at);
				request.extension.ifPresent(v -> u.add(transfers.extension, toJson(v)));
				u.add(transfers.group_extension, group.getExtension());
				u.add(transfers.org_extension, group.$orgs().getExtension());
				u.add(transfers.user_extension, user.getExtension());
				request.tags.ifPresent(v -> u.add(transfers.tags, v));
				u.add(transfers.created_by, User.currentUserId());
			},
			r -> r.getLong(transfers.id),
			transfers.id);

		Arrays.stream(request.bundles).forEach(r -> registerBundle(transferId, request.transferred_at, r));

		request.tags.ifPresent(tags -> TagHandler.stickTags(tags, tagIds -> {
			var table = new transfers_tags();
			tagIds.forEach(tagId -> {
				table.INSERT().VALUES(transferId, tagId).execute();
			});
		}));

		//jobを登録し、別プロセスで現在数量を更新させる
		new jobs().INSERT(a -> a.id).VALUES(transferId).execute();

		return transferId;
	}

	/**
	 * bundle登録処理
	 */
	private static void registerBundle(
		long transferId,
		Timestamp transferredAt,
		BundleRegisterRequest request) {
		long bundleId = ReturningUtilities.insertAndReturn(
			bundles.$TABLE,
			u -> {
				u.add(bundles.transfer_id, transferId);
				request.extension.ifPresent(v -> u.add(bundles.extension, toJson(v)));
			},
			r -> r.getLong(bundles.id),
			bundles.id);

		Arrays.stream(request.nodes).forEach(r -> registerNode(bundleId, transferredAt, r));
	}

	/**
	 * node登録処理
	 */
	private static void registerNode(
		long bundleId,
		Timestamp transferredAt,
		NodeRegisterRequest request) {
		//stockが既に存在すればそれを使う
		//なければ新たに登録
		var stock = selectedStocks()
			.WHERE(a -> a.group_id.eq(request.group_id).AND.item_id.eq(request.item_id).AND.owner_id.eq(request.owner_id).AND.location_id.eq(request.location_id).AND.status_id.eq(request.status_id))
			.willUnique()
			.orElseGet(() -> registerStock(request));

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
				request.extension.ifPresent(v -> u.add(nodes.extension, toJson(v)));
				u.add(nodes.group_extension, stock.$groups().getExtension());
				u.add(nodes.item_extension, stock.$items().getExtension());
				u.add(nodes.owner_extension, stock.$owners().getExtension());
				u.add(nodes.location_extension, stock.$locations().getExtension());
				u.add(nodes.status_extension, stock.$statuses().getExtension());
			},
			r -> r.getLong(bundles.id),
			bundles.id);

		//この在庫の数量と無制限タイプの在庫かを知るため、直近のsnapshotを取得
		JustBefore justBefore = new AnonymousTable(
			new snapshots().SELECT(
				a -> a.ls(
					a.total,
					a.infinity,
					//transferred_atの逆順、登録順の逆順
					a.any(
						"RANK() OVER (ORDER BY {0} DESC, {1} DESC)",
						a.$nodes().$bundles().$transfers().transferred_at,
						a.$nodes().id).AS("rank")))
				.WHERE(a -> a.$nodes().stock_id.eq(stockId)),
			"ordered").SELECT(a -> a.ls(a.col("total"), a.col("infinity")))
				.WHERE(a -> a.col("rank").eq(1))
				.aggregateAndGet(r -> {
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

		new snapshots().insertStatement(
			a -> a.INSERT(a.id, a.infinity, a.total, a.updated_by)
				.VALUES(
					nodeId,
					infinity,
					total,
					User.currentUserId()))
			.execute();

		//登録以降のsnapshotの数量と無制限設定を更新
		try {
			new snapshots().updateStatement(
				a -> a.UPDATE(
					//一度trueになったらずっとそのままtrue
					a.infinity.set("{0} OR ?", Vargs.of(a.infinity), Vargs.of(infinity)),
					//自身の数に今回の移動数量を正規化してプラス
					a.total.set("{0} + ?", Vargs.of(a.total), Vargs.of(request.in_out.normalize(request.quantity))))
					.WHERE(
						wa -> wa.id.IN(
							new nodes()
								.SELECT(sa -> sa.id)
								.WHERE(swa -> swa.stock_id.eq(stockId).AND.$bundles().$transfers().transferred_at.ge(transferredAt)))))
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
	private static stocks.Row registerStock(TransferComponent.NodeRegisterRequest request) {
		long stockId = ReturningUtilities.insertAndReturn(
			stocks.$TABLE,
			u -> {
				u.add(stocks.group_id, request.group_id);
				u.add(stocks.item_id, request.item_id);
				u.add(stocks.owner_id, request.owner_id);
				u.add(stocks.location_id, request.location_id);
				u.add(stocks.status_id, request.status_id);
				u.add(stocks.created_by, User.currentUserId());
			},
			r -> r.getLong(transfers.id),
			transfers.id);

		new current_stocks().insertStatement(
			//後でjobから更新されるのでinfinityはとりあえずfalse、totalは0
			a -> a.INSERT(a.id, a.infinity, a.total).VALUES(stockId, false, 0))
			.execute();

		//関連情報取得のため改めて検索
		return selectedStocks().fetch(stockId).get();
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
