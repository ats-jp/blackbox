package jp.ats.blackbox.persistence;

import java.util.function.Consumer;

import org.blendee.jdbc.BResultSet;
import org.blendee.jdbc.Result;
import org.blendee.jdbc.TablePath;
import org.blendee.util.GenericTable;

public class StockComponentHandler {

	private static final String id = "id";

	private static final String group_id = "group_id";

	private static final String name = "name";

	private static final String revision = "revision";

	private static final String extension = "extension";

	private static final String tags = "tags";

	private static final String active = "active";

	private static final String created_at = "created_at";

	private static final String created_by = "created_by";

	private static final String updated_at = "updated_at";

	private static final String updated_by = "updated_by";

	static long register(
		TablePath table,
		StockComponent.InsertRequest request) {
		var row = new GenericTable(table).row();

		row.setLong(group_id, request.group_id);
		row.setString(name, request.name);
		request.extension.ifPresent(v -> row.setString(extension, v));
		request.tags.ifPresent(v -> row.setObject(tags, v));

		return CommonHandler.register(row);
	}

	public static void update(
		TablePath table,
		StockComponent.UpdateRequest request) {
		int result = new GenericTable(table).UPDATE(a -> {
			a.col(revision).set(revision + 1);
			request.name.ifPresent(v -> a.col(name).set(v));
			request.extension.ifPresent(v -> a.col(extension).set(v));
			request.active.ifPresent(v -> a.col(active).set(v));
		}).WHERE(a -> a.col(id).eq(request.id).AND.col(revision).eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(table, request.id);
	}

	public static void delete(TablePath table, long id, long revision) {
		int result = new GenericTable(table)
			.DELETE()
			.WHERE(a -> a.col(StockComponentHandler.id).eq(id).AND.col(StockComponentHandler.revision).eq(revision))
			.execute();

		if (result != 1) throw Utils.decisionException(table, id);
	}

	public static void fetch(TablePath table, long id, Consumer<Result> consumer) {}

	public static void search(TablePath table, long id, Consumer<BResultSet> consumer) {}
}
