package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$TIMESTAMP;
import static org.blendee.sql.Placeholder.$UUID;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Consumer;

import org.blendee.assist.AnonymousTable;
import org.blendee.assist.Vargs;
import org.blendee.jdbc.BResultSet;

import jp.ats.blackbox.common.U;
import sqlassist.bb.snapshots;
import sqlassist.bb.transient_nodes;

public class TransientStockQuery {

	private final AnonymousTable query;

	public TransientStockQuery(
		Consumer<transient_nodes> transientNodeDecorator,
		Consumer<snapshots> snapshotDecorator) {
		var nodes = new transient_nodes()
			.SELECT(
				a -> a.ls(
					a.stock_id,
					a.any("bool_or({0})", a.grants_unlimited).AS("unlimited"),
					a.SUM(a.expr("{0} * {1}", a.quantity, a.in_out)).AS("total")))
			.WHERE(
				a -> a.$transient_bundles().$transient_transfers().transferred_at.le($TIMESTAMP), //1
				a -> a.$transient_bundles().$transient_transfers().transient_id.eq($UUID)) //2
			.GROUP_BY(a -> a.stock_id);

		transientNodeDecorator.accept(nodes);

		var snapshots = StockHandler.buildQuery(a -> a.transferred_at.le($TIMESTAMP)).SELECT(a -> a.total); //3

		snapshotDecorator.accept(snapshots);

		query = new AnonymousTable(
			nodes,
			"nodes_sum")
				.SELECT(a -> a.col("total"))
				.LEFT_OUTER_JOIN(snapshots)
				.ON((l, r) -> l.col("stock_id").eq(r.stock_id));
	}

	public void execute(
		LocalDateTime time,
		UUID transientId,
		Vargs<Object> transientNodePlaceholderValues,
		Vargs<Object> snapshotPlaceholderValues,
		Consumer<BResultSet> resultConsumer) {
		Timestamp timestamp = U.convert(time);

		var values = new LinkedList<Object>();

		values.add(timestamp);
		values.add(transientId);

		transientNodePlaceholderValues.stream().forEach(values::add);

		values.add(timestamp);

		snapshotPlaceholderValues.stream().forEach(values::add);

		U.recorder.play(() -> query, values.toArray(new Object[values.size()])).aggregate(resultConsumer);
	}
}
