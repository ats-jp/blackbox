package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$BIGDECIMAL;
import static org.blendee.sql.Placeholder.$BOOLEAN;
import static org.blendee.sql.Placeholder.$UUID;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.blendee.jdbc.BatchStatement;
import org.blendee.jdbc.BlendeeManager;
import org.blendee.sql.Recorder;

import jp.ats.blackbox.common.U;
import sqlassist.bb.current_stocks;
import sqlassist.bb.jobs;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;

/**
 * job関連クラス
 */
public class JobHandler {

	private static final Recorder recorder = new Recorder();

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
					.SELECT(a -> a.ls(a.unlimited, a.total, a.$nodes().stock_id))
					.WHERE(sa -> sa.$nodes().$bundles().transfer_id.eq(r.getId()))
					.aggregate(result -> {
						while (result.next()) {
							//TODO pluginで個別処理を複数スレッドで行うようにする
							recorder.play(
								() -> new current_stocks()
									.UPDATE(a -> a.ls(a.unlimited.set($BOOLEAN), a.total.set($BIGDECIMAL)))
									.WHERE(a -> a.id.eq($UUID)),
								result.getBoolean(snapshots.unlimited),
								result.getBigDecimal(snapshots.total),
								U.uuid(result, nodes.stock_id))
								.execute(batch);
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
				r.next();
				var next = r.getTimestamp(1);

				if (next == null)
					//一件もない場合は次にjobが登録されるまで待つために最大値を返す
					return LocalDateTime.MAX;

				return U.convert(r.getTimestamp(1));
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
