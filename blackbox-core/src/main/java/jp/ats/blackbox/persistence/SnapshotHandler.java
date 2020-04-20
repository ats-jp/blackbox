package jp.ats.blackbox.persistence;

import java.util.function.Consumer;

import org.blendee.jdbc.Transaction;

import jp.ats.blackbox.common.U;
import sqlassist.bb.current_units;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;

public class SnapshotHandler {

	public static void refreshSnapshots(Transaction transaction, Consumer<snapshots> snapshotDecorator) {
		var snapshotQuery = new snapshots().SELECT(a -> a.id);
		snapshotDecorator.accept(snapshotQuery);

		new current_units().UPDATE(a -> a.snapshot_id.set(U.NULL_ID)).WHERE(a -> a.snapshot_id.IN(snapshotQuery)).execute();

		new nodes().SELECT(
			a -> a.ls(
				a.id,
				a.seq,
				a.$details().$journals().group_id,
				a.$details().$journals().fixed_at,
				a.$details().$journals().created_at,
				a.unit_id,
				a.grants_unlimited,
				a.in_out,
				a.quantity))
			.WHERE(a -> a.id.IN(snapshotQuery))
			.forEach(node -> {
				var id = node.getId();
				new snapshots().DELETE().WHERE(a -> a.id.eq(id)).execute();

				var journal = node.$details().$journals();

				var fixedAt = journal.getFixed_at();

				JournalHandler.storeSnapshot(
					U.recorder,
					id,
					node.getSeq(),
					SecurityValues.currentUserId(),
					journal.getGroup_id(),
					fixedAt,
					journal.getCreated_at(),
					node.getUnit_id(),
					node.getGrants_unlimited(),
					InOut.of(node.getIn_out()),
					node.getQuantity());

				transaction.commit();
			});
	}
}
