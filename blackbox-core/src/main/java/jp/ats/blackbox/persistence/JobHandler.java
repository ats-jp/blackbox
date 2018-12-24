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

	public void execute(LocalDateTime time) {
		var subquery = new jobs()
			.SELECT(a -> a.id)
			.WHERE(a -> a.completed.eq(false).AND.$transfers().transferred_at.le(Timestamp.valueOf(time)));

		BatchStatement batch = BlendeeManager.getConnection().getBatchStatement();

		new snapshots().selectClause(a -> a.SELECT(a.total, a.$nodes().stock_id))
			.WHERE(
				sa -> sa.$nodes().$bundles().transfer_id.IN(subquery))
			.aggregate(r -> {
				var updater = new current_stocks()
					.UPDATE(a -> a.total.set(Placeholder.$B))
					.WHERE(a -> a.id.eq(Placeholder.$L));

				while (r.next()) {
					//TODO pluginで個別処理を複数スレッドで行うようにする
					updater.reproduce(
						r.getBigDecimal(snapshots.total),
						r.getLong(nodes.stock_id)).execute(batch);
				}
			});

		batch.executeBatch();
	}

	public void executeTransient(LocalDateTime time) {
		//TODO transient用
		//jobsの代わりにtransient_transfersを使用する
	}
}