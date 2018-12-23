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
		executeWithoutTrigger(time);
		executeWithTrigger(time);
	}

	//current_stocksの数量更新のみを高速に行うための処理
	private static void executeWithoutTrigger(LocalDateTime time) {
		var subquery = new jobs()
			.SELECT(a -> a.id)
			.WHERE(a -> a.trigger_id.ne(0).AND.completed.eq(false).AND.$transfers().transferred_at.le(Timestamp.valueOf(time)));

		BatchStatement batch = BlendeeManager.getConnection().getBatchStatement();

		new snapshots().selectClause(a -> a.SELECT(a.total, a.$nodes().stock_id))
			.WHERE(
				sa -> sa.$nodes().$bundles().$transfers().id.IN(subquery))
			.aggregate(r -> {
				var updater = new current_stocks()
					.UPDATE(a -> a.total.set(Placeholder.$B))
					.WHERE(a -> a.id.eq(Placeholder.$L));

				while (r.next()) {
					updater.reproduce(r.getBigDecimal(snapshots.total), r.getLong(nodes.stock_id)).execute(batch);
				}
			});

		batch.executeBatch();
	}

	//TODO trigger起動処理未実装
	//複数のworkerでパラレルにtriggerを処理する予定
	private static void executeWithTrigger(LocalDateTime time) {}
}
