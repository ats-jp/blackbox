package jp.ats.blackbox.persistence;

import static org.blendee.util.Placeholder.$B;
import static org.blendee.util.Placeholder.$BO;
import static org.blendee.util.Placeholder.$L;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.blendee.jdbc.BatchStatement;
import org.blendee.jdbc.BlendeeManager;

import jp.ats.blackbox.common.U;
import sqlassist.bb.current_stocks;
import sqlassist.bb.jobs;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;

/**
 * job関連クラス
 */
public class JobHandler {

	/**
	 * パラメータのtime以降のjobをもとにcurrent_stockを更新する
	 */
	public static void execute(LocalDateTime time) {
		//トランザクション内の他の検索で参照されない、数が多い可能性があるのでbatchで実行
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
						//繰り返しのためのSQLを先に作る
						var updater = new current_stocks()
							.UPDATE(a -> a.ls(a.infinity.set($BO), a.total.set($B)))
							.WHERE(a -> a.id.eq($L));

						while (result.next()) {
							//TODO pluginで個別処理を複数スレッドで行うようにする
							updater.reproduce(
								result.getBoolean(snapshots.infinity),
								result.getBigDecimal(snapshots.total),
								result.getLong(nodes.stock_id)).execute(batch);
						}
					});

				//完了済み
				r.setCompleted(true);
				r.update(batch);
			});

		batch.executeBatch();
	}

	public static LocalDateTime getNextTime() {
		return new jobs()
			.SELECT(a -> a.MIN(a.$transfers().transferred_at))
			.WHERE(a -> a.completed.eq(false))
			.aggregateAndGet(r -> {
				if (r.next()) return U.convert(r.getTimestamp(1));

				//一件もない場合は次にjobが登録されるまで待つために最大値を返す
				return LocalDateTime.MAX;
			});
	}

	public static void executeTransient(LocalDateTime time) {
		//TODO transient用
		//jobsの代わりにtransient_transfersを使用する
	}

	public static LocalDateTime getNextTimeTransient() {
		//TODO transient用
		return null;
	}
}
