package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$BIGDECIMAL;
import static org.blendee.sql.Placeholder.$BOOLEAN;
import static org.blendee.sql.Placeholder.$UUID;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.blendee.jdbc.BlendeeManager;
import org.blendee.sql.Recorder;

import jp.ats.blackbox.common.U;
import sqlassist.bb.current_units;
import sqlassist.bb.jobs;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;

/**
 * job関連クラス
 */
public class JobHandler {

	private static final Recorder recorder = U.recorder;

	/**
	 * パラメータのtime以降のjobをもとにcurrent_stockを更新する
	 */
	public static void execute(LocalDateTime time) {
		//トランザクション内の他の検索で参照されない、数が多い可能性があるのでbatchで実行
		var batch = BlendeeManager.getConnection().getBatch();

		new jobs()
			.SELECT(a -> a.id)
			.WHERE(a -> a.completed.eq(false).AND.$journals().fixed_at.le(Timestamp.valueOf(time)))
			.ORDER_BY(a -> a.ls(a.$journals().fixed_at, a.$journals().created_at))
			.forEach(row -> {
				new snapshots()
					.SELECT(a -> a.ls(a.id, a.unlimited, a.total, a.$nodes().unit_id))
					.WHERE(sa -> sa.$nodes().$details().journal_id.eq(row.getId()))
					.ORDER_BY(a -> a.node_seq)
					.aggregate(result -> {
						while (result.next()) {
							//TODO pluginで個別処理を複数スレッドで行うようにする
							recorder.play(
								() -> new current_units()
									.UPDATE(a -> a.ls(a.snapshot_id.set($UUID), a.unlimited.set($BOOLEAN), a.total.set($BIGDECIMAL)))
									.WHERE(a -> a.id.eq($UUID)),
								(UUID) result.getObject(snapshots.id),
								result.getBoolean(snapshots.unlimited),
								result.getBigDecimal(snapshots.total),
								U.uuid(result, nodes.unit_id))
								.execute(batch);
						}
					});

				//完了済み
				row.setCompleted(true);

				row.update(batch);
			});

		batch.execute();
	}

	public static LocalDateTime getNextTime() {
		return new jobs()
			.SELECT(a -> a.MIN(a.$journals().fixed_at))
			.WHERE(a -> a.completed.eq(false))
			.aggregateAndGet(result -> {
				result.next();
				var next = result.getTimestamp(1);

				if (next == null)
					//一件もない場合は次にjobが登録されるまで待つために最大値を返す
					return LocalDateTime.MAX;

				return U.convert(result.getTimestamp(1));
			});
	}
}
