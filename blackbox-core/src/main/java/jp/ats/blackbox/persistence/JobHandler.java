package jp.ats.blackbox.persistence;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.blendee.jdbc.BatchStatement;
import org.blendee.jdbc.BlendeeManager;
import org.blendee.util.Placeholder;

import sqlassist.bb.current_stocks;
import sqlassist.bb.jobs;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;

public class JobHandler {

	public static void execute(LocalDateTime time) {
		BatchStatement batch = BlendeeManager.getConnection().getBatchStatement();

		new jobs()
			.SELECT(a -> a.id)
			.WHERE(a -> a.completed.eq(false).AND.$transfers().transferred_at.le(Timestamp.valueOf(time)))
			.ORDER_BY(a -> a.ls(a.$transfers().transferred_at, a.id))
			.forEach(r -> {
				new snapshots()
					.SELECT(a -> a.ls(a.infinity, a.total, a.$nodes().stock_id))
					.WHERE(sa -> sa.$nodes().$bundles().transfer_id.eq(r.getId()))
					.aggregate(result -> {
						var updater = new current_stocks()
							.UPDATE(a -> a.ls(a.infinity.set(Placeholder.$BO), a.total.set(Placeholder.$B)))
							.WHERE(a -> a.id.eq(Placeholder.$L));

						while (result.next()) {
							//TODO pluginで個別処理を複数スレッドで行うようにする
							updater.reproduce(
								result.getBoolean(snapshots.infinity),
								result.getBigDecimal(snapshots.total),
								result.getLong(nodes.stock_id)).execute(batch);
						}
					});

				r.setCompleted(true);
				r.update(batch);
			});

		batch.executeBatch();
	}

	public static void executeTransient(LocalDateTime time) {
		//TODO transient用
		//jobsの代わりにtransient_transfersを使用する
	}
}
