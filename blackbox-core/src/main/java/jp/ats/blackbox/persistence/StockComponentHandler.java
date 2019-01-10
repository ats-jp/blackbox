package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;

import java.util.UUID;
import java.util.function.Consumer;

import org.blendee.assist.Vargs;
import org.blendee.jdbc.BResultSet;
import org.blendee.jdbc.Result;
import org.blendee.jdbc.TablePath;
import org.blendee.util.GenericTable;

import jp.ats.blackbox.persistence.StockComponent.ForcibleUpdateRequest;
import jp.ats.blackbox.persistence.StockComponent.RegisterRequest;
import jp.ats.blackbox.persistence.StockComponent.UpdateRequest;

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

	public static UUID register(
		TablePath table,
		RegisterRequest request) {
		var row = new GenericTable(table).row();

		UUID uuid = UUID.randomUUID();

		row.setUUID(id, uuid);

		row.setUUID(group_id, request.group_id);
		row.setString(name, request.name);
		request.extension.ifPresent(v -> row.setObject(extension, toJson(v)));
		request.tags.ifPresent(v -> row.setObject(tags, v));

		UUID userId = SecurityValues.currentUserId();

		row.setUUID(created_by, userId);
		row.setUUID(updated_by, userId);

		row.insert();

		request.tags.ifPresent(tags -> TagHandler.stickTags(tags, table, uuid));

		return uuid;
	}

	public static void update(
		TablePath table,
		UpdateRequest request) {
		int result = new GenericTable(table).UPDATE(a -> {
			a.col(revision).set("{0} + 1", Vargs.of(a.col(revision)), Vargs.of());
			request.name.ifPresent(v -> a.col(name).set(v));
			request.extension.ifPresent(v -> a.col(extension).set(toJson(v)));
			request.tags.ifPresent(v -> a.col(tags).set((Object) v));
			request.active.ifPresent(v -> a.col(active).set(v));
			a.col(updated_by).set(SecurityValues.currentUserId());
		}).WHERE(a -> a.col(id).eq(request.id).AND.col(revision).eq(request.revision)).execute();

		request.tags.ifPresent(tags -> TagHandler.stickTags(tags, table, request.id));

		if (result != 1) throw Utils.decisionException(table, request.id);
	}

	public static void updateForcibly(
		TablePath table,
		ForcibleUpdateRequest request) {
		int result = new GenericTable(table).UPDATE(a -> {
			a.col(revision).set("{0} + 1", Vargs.of(a.col(revision)), Vargs.of());
			request.name.ifPresent(v -> a.col(name).set(v));
			request.extension.ifPresent(v -> a.col(extension).set(toJson(v)));
			request.tags.ifPresent(v -> a.col(tags).set((Object) v));
			request.active.ifPresent(v -> a.col(active).set(v));
			a.col(updated_by).set(SecurityValues.currentUserId());
		}).WHERE(a -> a.col(id).eq(request.id)).execute();

		request.tags.ifPresent(tags -> TagHandler.stickTags(tags, table, request.id));

		if (result != 1) throw Utils.decisionException(table, request.id);
	}

	public static void delete(TablePath table, UUID id, long revision) {
		int result = new GenericTable(table)
			.DELETE()
			.WHERE(a -> a.col(StockComponentHandler.id).eq(id).AND.col(StockComponentHandler.revision).eq(revision))
			.execute();

		if (result != 1) throw Utils.decisionException(table, id);
	}

	public static void fetch(TablePath table, long id, Consumer<Result> consumer) {}

	public static void search(TablePath table, long id, Consumer<BResultSet> consumer) {}
}
