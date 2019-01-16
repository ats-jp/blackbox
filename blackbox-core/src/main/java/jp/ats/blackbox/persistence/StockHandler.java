package jp.ats.blackbox.persistence;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import org.blendee.assist.AnonymousTable;

import jp.ats.blackbox.common.U;
import sqlassist.bb.current_stocks;
import sqlassist.bb.snapshots;

public class StockHandler {

	public static void selectCurrentStocks() {
		new current_stocks();
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
}
