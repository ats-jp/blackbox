package jp.ats.blackbox.core.persistence;

import static org.blendee.sql.Placeholder.$STRING;
import static org.blendee.sql.Placeholder.$UUID;

import java.util.UUID;

import org.blendee.jdbc.TablePath;
import org.blendee.sql.Recorder;
import org.blendee.util.GenericTable;

import jp.ats.blackbox.common.U;
import sqlassist.bb.tags;

public class TagHandler {

	public static void stickTag(UUID tagId, UUID targetId, TablePath target) {
		var table = new GenericTable(new TablePath(target.getSchemaName(), target.getTableName() + "_tags"));
		table.INSERT().VALUES(targetId, tagId).execute();
	}

	public static void stickTagAgain(UUID tagId, UUID targetId, TablePath target) {
		var table = new GenericTable(new TablePath(target.getSchemaName(), target.getTableName() + "_tags"));
		table.DELETE().WHERE(a -> a.col("id").eq(targetId)).execute();
		table.INSERT().VALUES(targetId, tagId).execute();
	}

	public static UUID getTagId(String tag, Recorder recorder) {
		return recorder.play(
			() -> new tags().SELECT(a -> a.id).WHERE(a -> a.tag.eq($STRING)),
			tag)
			.executeAndGet(r -> {
				if (r.next()) return U.uuid(r, 1);

				UUID newId = UUID.randomUUID();
				U.recorder.play(
					() -> new tags().insertStatement(
						a -> a.INSERT(a.id, a.tag).VALUES($UUID, $STRING)),
					newId,
					tag).execute();

				return newId;
			});
	}
}
