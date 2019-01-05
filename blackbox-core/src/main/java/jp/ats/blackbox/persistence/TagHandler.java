package jp.ats.blackbox.persistence;

import static org.blendee.util.Placeholder.$STRING;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.blendee.dialect.postgresql.ReturningUtilities;
import org.blendee.jdbc.TablePath;
import org.blendee.util.GenericTable;
import org.blendee.util.Recorder;

import sqlassist.bb.tags;

public class TagHandler {

	private static final Recorder recorder = new Recorder();

	public static void stickTags(String[] tagStrings, TablePath target, long targetId) {
		stickTags(
			tagStrings,
			ids -> ids.forEach(
				id -> {
					var table = new GenericTable(new TablePath(target.getSchemaName(), target.getTableName() + "_tags"));
					table.DELETE().WHERE(a -> a.col("id").eq(targetId)).execute();
					table.INSERT().VALUES(targetId, id).execute();
				}));
	}

	public static void stickTags(String[] tagStrings, Consumer<Stream<Long>> stickAction) {
		var stream = Arrays.stream(tagStrings).map(t -> {
			return recorder.play(
				() -> new tags().SELECT(a -> a.id).WHERE(a -> a.tag.eq($STRING)),
				t).aggregateAndGet(r -> {
					if (r.next()) return r.getLong(1);
					return ReturningUtilities.insertAndReturn(
						tags.$TABLE,
						u -> u.add(tags.tag, t),
						result -> result.getLong(tags.id),
						tags.id);
				});
		});

		stickAction.accept(stream);
	}
}
