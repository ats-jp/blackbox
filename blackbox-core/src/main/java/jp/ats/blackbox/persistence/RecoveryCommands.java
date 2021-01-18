package jp.ats.blackbox.persistence;

import java.sql.Timestamp;
import java.util.function.Consumer;

import org.blendee.jdbc.BlendeeManager;
import org.blendee.jdbc.Transaction;

import jp.ats.blackbox.common.U;
import sqlassist.bb.current_units;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;
import sqlassist.bb.units;

public class RecoveryCommands {

	public static void linkCurrentUnitsToSnapshots(Consumer<units.WhereAssist> criteriaDecorator) {
		var updatedAt = new Timestamp(System.currentTimeMillis());

		var batch = BlendeeManager.getConnection().getBatch();

		new current_units()
			.SELECT(a -> a.id)
			.WHERE(a -> a.id.IN(new units().SELECT(sa -> sa.id).WHERE(criteriaDecorator)))
			.forEach(r -> {
				UnitHandler.buildQuery(a -> a.unit_id.eq(r.getId()))
					.SELECT(
						a -> a.ls(
							a.id,
							a.total,
							a.unlimited))
					.willUnique()
					.ifPresent(s -> {
						r.setSnapshot_id(s.getId());
						r.setTotal(s.getTotal());
						r.setUnlimited(s.getUnlimited());
						r.setUpdated_at(updatedAt);

						r.update(batch);
					});
			});

		batch.execute();
	}

	public static void recreateCurrentUnits() {
		new current_units().INSERT(
			a -> a.ls(
				a.id,
				a.unlimited,
				a.total,
				a.snapshot_id,
				a.updated_at),
			new units().SELECT(
				a -> a.ls(
					a.id,
					a.any("false"),
					a.any(0),
					a.any("'" + U.NULL_ID.toString() + "'"),
					a.any("now()")))
				.WHERE(a -> a.NOT_EXISTS(new current_units().SELECT(ea -> ea.any(0)).WHERE(ea -> ea.id.eq(a.id)))))
			.execute();
	}

	public static void recreateSnapshots(Transaction transaction) {
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
			.WHERE(a -> a.NOT_EXISTS(new snapshots().SELECT(ea -> ea.any(0)).WHERE(ea -> ea.id.eq(a.id))))
			.ORDER_BY(
				a -> a.ls(
					a.$details().$journals().fixed_at,
					a.$details().$journals().created_at,
					a.seq))
			.forEach(node -> {
				var journal = node.$details().$journals();

				JournalHandler.storeSnapshot(
					U.recorder,
					node.getId(),
					node.getSeq(),
					SecurityValues.currentUserId(),
					journal.getGroup_id(),
					journal.getFixed_at(),
					journal.getCreated_at(),
					node.getUnit_id(),
					node.getGrants_unlimited(),
					InOut.of(node.getIn_out()),
					node.getQuantity());

				transaction.commit();
			});
	}
}
