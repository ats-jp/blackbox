package jp.ats.blackbox.stock;

import static jp.ats.blackbox.common.U.toPGObject;

import java.util.UUID;

import org.blendee.assist.Vargs;
import org.blendee.jdbc.TablePath;
import org.blendee.util.GenericTable;

import jp.ats.blackbox.executor.TagExecutor;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.SeqHandler;
import jp.ats.blackbox.persistence.Utils;
import jp.ats.blackbox.stock.StockComponent.RegisterRequest;
import jp.ats.blackbox.stock.StockComponent.UpdateRequest;

public class StockComponentHandler {

	private static final String id = "id";

	private static final String group_id = "group_id";

	private static final String seq = "seq";

	private static final String name = "name";

	private static final String description = "description";

	private static final String revision = "revision";

	private static final String props = "props";

	private static final String tags = "tags";

	private static final String active = "active";

	private static final String created_by = "created_by";

	private static final String updated_by = "updated_by";

	private static final String updated_at = "updated_at";

	public static UUID register(
		TablePath table,
		RegisterRequest request) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = table;
		seqRequest.dependsColumn = group_id;
		seqRequest.dependsId = request.group_id;

		return SeqHandler.getInstance().nextSeqAndGet(seqRequest, seq -> registerInternal(table, request, seq));
	}

	private static UUID registerInternal(
		TablePath table,
		RegisterRequest request,
		long seqValue) {
		var row = new GenericTable(table).row();

		UUID uuid = UUID.randomUUID();

		row.setUUID(id, uuid);

		row.setUUID(group_id, request.group_id);
		row.setLong(seq, seqValue);
		row.setString(name, request.name);
		request.description.ifPresent(v -> row.setString(description, v));
		request.props.ifPresent(v -> row.setObject(props, toPGObject(v)));
		request.tags.ifPresent(v -> row.setObject(tags, v));

		UUID userId = SecurityValues.currentUserId();

		row.setUUID(created_by, userId);
		row.setUUID(updated_by, userId);

		row.insert();

		request.tags.ifPresent(tags -> TagExecutor.stickTags(tags, uuid, table));

		return uuid;
	}

	public static void update(
		TablePath table,
		UpdateRequest request) {
		int result = new GenericTable(table).UPDATE(a -> {
			a.col(revision).set("{0} + 1", Vargs.of(a.col(revision)), Vargs.of());
			request.name.ifPresent(v -> a.col(name).set(v));
			request.description.ifPresent(v -> a.col(description).set(v));
			request.props.ifPresent(v -> a.col(props).set(toPGObject(v)));
			request.tags.ifPresent(v -> a.col(tags).set((Object) v));
			request.active.ifPresent(v -> a.col(active).set(v));
			a.col(updated_by).set(SecurityValues.currentUserId());
			a.col(updated_at).setAny("now()");
		}).WHERE(a -> a.col(id).eq(request.id).AND.col(revision).eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(table, request.id);

		request.tags.ifPresent(tags -> TagExecutor.stickTagsAgain(tags, request.id, table));
	}

	public static void delete(TablePath table, UUID id, long revision) {
		int result = new GenericTable(table)
			.DELETE()
			.WHERE(a -> a.col(StockComponentHandler.id).eq(id).AND.col(StockComponentHandler.revision).eq(revision))
			.execute();

		if (result != 1) throw Utils.decisionException(table, id);
	}
}
