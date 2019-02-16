package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$STRING;
import static org.blendee.sql.Placeholder.$UUID;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.blendee.jdbc.TablePath;
import org.blendee.sql.Recorder;
import org.blendee.util.GenericTable;

import jp.ats.blackbox.common.U;
import sqlassist.bb.tags;

public class TagHandler {

	private static final ReentrantLock lock = new ReentrantLock();

	public static void stickTags(String[] tagStrings, TablePath target, UUID targetId) {
		stickTags(
			tagStrings,
			ids -> ids.forEach(
				id -> {
					var table = new GenericTable(new TablePath(target.getSchemaName(), target.getTableName() + "_tags"));
					table.DELETE().WHERE(a -> a.col("id").eq(targetId)).execute();
					table.INSERT().VALUES(targetId, id).execute();
				}),
			U.recorder);
	}

	public static void stickTags(String[] tagStrings, Consumer<Stream<UUID>> stickAction, Recorder recorder) {
		var stream = Arrays.stream(tagStrings).map(t -> {
			lock.lock();
			try {
				return recorder.play(
					() -> new tags().SELECT(a -> a.id).WHERE(a -> a.tag.eq($STRING)),
					t).aggregateAndGet(r -> {
						if (r.next()) return U.uuid(r, 1);

						UUID id = UUID.randomUUID();
						U.recorder.play(() -> new tags().insertStatement(a -> a.INSERT(a.id, a.tag).VALUES($UUID, $STRING)), id, t).execute();
						return id;
					});
			} finally {
				lock.unlock();
			}
		});

		stickAction.accept(stream);
	}
}
