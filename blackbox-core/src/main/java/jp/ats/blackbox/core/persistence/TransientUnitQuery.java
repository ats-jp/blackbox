package jp.ats.blackbox.core.persistence;

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
import org.blendee.sql.Recorder;

import jp.ats.blackbox.common.U;
import sqlassist.bb.snapshots;
import sqlassist.bb.transient_nodes;

public class TransientUnitQuery {

	private final AnonymousTable query;

	private final Recorder recorder = Recorder.newAsyncInstance();

	public TransientUnitQuery(
		Consumer<transient_nodes> transientNodeDecorator,
		Consumer<snapshots> snapshotDecorator) {
		var nodes = new transient_nodes()
			.SELECT(
				a -> a.ls(
					a.unit_id,
					a.any("bool_or({0})", a.grants_unlimited).AS("unlimited"),
					a.SUM(a.expr("{0} * {1}", a.quantity, a.in_out)).AS("total")))
			.WHERE(
				a -> a.$transient_details().$transient_journals().fixed_at.le($TIMESTAMP), //1
				a -> a.$transient_details().$transient_journals().transient_id.eq($UUID)) //2
			.GROUP_BY(a -> a.unit_id);

		transientNodeDecorator.accept(nodes);

		var snapshots = UnitHandler.buildQuery(a -> a.fixed_at.le($TIMESTAMP)).SELECT(a -> a.total); //3

		snapshotDecorator.accept(snapshots);

		query = new AnonymousTable(
			nodes,
			"nodes_sum")
				.SELECT(a -> a.col("total"))
				.LEFT_OUTER_JOIN(snapshots)
				.ON((l, r) -> l.col("unit_id").eq(r.unit_id));
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

		//この時点ではクエリは固定されているので、このインスタンス固有のRecorderでキャッシュできる
		recorder.play(() -> query, values.toArray(new Object[values.size()])).execute(resultConsumer);
	}
}
