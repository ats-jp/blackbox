package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$BOOLEAN;
import static org.blendee.sql.Placeholder.$INT;
import static org.blendee.sql.Placeholder.$UUID;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.blendee.assist.AnonymousTable;
import org.blendee.sql.Recorder;

import jp.ats.blackbox.common.U;
import sqlassist.bb.current_stocks;
import sqlassist.bb.snapshots;
import sqlassist.bb.stocks;

public class StockHandler {

	public static void selectCurrentStocks() {
		new current_stocks();//TODO
	}

	public static snapshots buildQuery(
		LocalDateTime time,
		Consumer<snapshots.WhereAssist> criteriaDecorator) {
		var raw = new snapshots().SELECT(
			a -> a.ls(
				a.id,
				a.any(
					"RANK() OVER (ORDER BY {0} DESC, {1} DESC, {2} DESC)",
					a.transferred_at,
					a.created_at,
					a.node_seq).AS("rank")))
			.WHERE(a -> a.transferred_at.le(U.convert(time)));

		raw.WHERE(a -> criteriaDecorator.accept(a));

		var inner = new AnonymousTable(raw, "subquery").SELECT(a -> a.any(0)).WHERE(a -> a.col("rank").eq(1));

		return new snapshots().WHERE(a -> a.EXISTS(inner.WHERE(wa -> wa.col("id").eq(a.id))));
	}

	public static interface StockComponents {

		UUID groupId();

		UUID itemId();

		UUID ownerId();

		UUID locationId();

		UUID statusId();
	}

	public static stocks.Row prepareStock(
		Supplier<stocks> supplier,
		UUID userId,
		StockComponents components,
		Recorder recorder) {
		//stockが既に存在すればそれを使う
		//なければ新たに登録
		return recorder.play(
			() -> supplier.get()
				.WHERE(a -> a.group_id.eq($UUID).AND.item_id.eq($UUID).AND.owner_id.eq($UUID).AND.location_id.eq($UUID).AND.status_id.eq($UUID)),
			components.groupId(),
			components.itemId(),
			components.ownerId(),
			components.locationId(),
			components.statusId())
			.willUnique()
			.orElseGet(() -> registerStock(supplier, userId, components, recorder));
	}

	/**
	 * stock登録処理
	 */
	private static stocks.Row registerStock(
		Supplier<stocks> supplier,
		UUID userId,
		StockComponents components,
		Recorder recorder) {
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
			components.groupId(),
			components.itemId(),
			components.ownerId(),
			components.locationId(),
			components.statusId(),
			userId)
			.execute();

		recorder.play(
			() -> new current_stocks().insertStatement(
				//後でjobから更新されるのでunlimitedはとりあえずfalse、totalは0
				a -> a.INSERT(a.id, a.unlimited, a.total, a.snapshot_id).VALUES($UUID, $BOOLEAN, $INT, $UUID)),
			stockId,
			false,
			0,
			U.NULL_ID).execute();

		//関連情報取得のため改めて検索
		return recorder.play(() -> supplier.get()).fetch(stockId).get();
	}
}
