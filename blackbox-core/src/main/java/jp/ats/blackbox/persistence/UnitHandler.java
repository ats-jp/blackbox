package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$BOOLEAN;
import static org.blendee.sql.Placeholder.$INT;
import static org.blendee.sql.Placeholder.$UUID;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.blendee.assist.AnonymousTable;

import jp.ats.blackbox.common.U;
import sqlassist.bb.current_units;
import sqlassist.bb.snapshots;
import sqlassist.bb.units;

public class UnitHandler {

	public static snapshots buildQuery(
		Consumer<snapshots.WhereAssist> criteriaDecorator) {
		var raw = new snapshots().SELECT(
			a -> a.ls(
				a.id,
				a.any(
					"RANK() OVER (ORDER BY {0} DESC, {1} DESC, {2} DESC)",
					a.fixed_at,
					a.created_at,
					a.node_seq).AS("rank")))
			.WHERE(a -> criteriaDecorator.accept(a));

		var inner = new AnonymousTable(raw, "subquery").SELECT(a -> a.any(0)).WHERE(a -> a.col("rank").eq(1));

		return new snapshots().WHERE(a -> a.EXISTS(inner.WHERE(wa -> wa.col("id").eq(a.id))));
	}

	/**
	 * stock登録処理
	 */
	public static units.Row register(
		UUID groupId,
		UUID userId,
		Supplier<units> supplier) {
		UUID unitId = UUID.randomUUID();

		U.recorder.play(
			() -> new units().insertStatement(
				a -> a
					.INSERT(
						a.id,
						a.group_id,
						a.created_by)
					.VALUES(
						$UUID,
						$UUID,
						$UUID)),
			unitId,
			groupId,
			userId)
			.execute();

		U.recorder.play(
			() -> new current_units().insertStatement(
				//後でjobから更新されるのでunlimitedはとりあえずfalse、totalは0
				a -> a.INSERT(a.id, a.unlimited, a.total, a.snapshot_id).VALUES($UUID, $BOOLEAN, $INT, $UUID)),
			unitId,
			false,
			0,
			U.NULL_ID).execute();

		//関連情報取得のため改めて検索
		return U.recorder.play(() -> supplier.get()).fetch(unitId).get();
	}
}
