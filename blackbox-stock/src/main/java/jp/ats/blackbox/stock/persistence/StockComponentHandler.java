package jp.ats.blackbox.stock.persistence;

import static jp.ats.blackbox.common.U.toPGObject;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.blendee.assist.Vargs;
import org.blendee.jdbc.TablePath;
import org.blendee.util.GenericTable;
import org.blendee.util.GenericTable.UpdateAssist;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.executor.TagExecutor;
import jp.ats.blackbox.core.persistence.DefaultCodeGenerator;
import jp.ats.blackbox.core.persistence.SeqHandler;
import jp.ats.blackbox.core.persistence.Utils;

class StockComponentHandler {

	static class RegisterRequest {

		public String name;

		public Optional<String> code = Optional.empty();

		public Optional<String> description = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();
	}

	static class UpdateRequest {

		public UUID id;

		public Optional<String> name = Optional.empty();

		public Optional<String> code = Optional.empty();

		public Optional<String> description = Optional.empty();

		public long revision;

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}

	private static final String id = "id";

	private static final String name = "name";

	private static final String code = "code";

	private static final String description = "description";

	private static final String revision = "revision";

	private static final String props = "props";

	private static final String tags = "tags";

	private static final String active = "active";

	private static final String created_by = "created_by";

	private static final String updated_by = "updated_by";

	private static final String updated_at = "updated_at";

	interface RegisterInfo {

		String seqColumn();

		String dependsColumn();

		UUID dependsId();

		String generateDefaultCode(long seq);
	}

	static class GroupInfo implements RegisterInfo {

		private final UUID groupId;

		GroupInfo(UUID groupId) {
			this.groupId = groupId;
		}

		@Override
		public String seqColumn() {
			return "seq";
		}

		@Override
		public String dependsColumn() {
			return "group_id";
		}

		@Override
		public UUID dependsId() {
			return groupId;
		}

		@Override
		public String generateDefaultCode(long seq) {
			return DefaultCodeGenerator.generate(U.recorder, groupId, seq);
		}
	}

	static UUID register(
		TablePath table,
		RegisterRequest request,
		RegisterInfo info,
		UUID userId,
		Consumer<GenericTable.Row> consumer) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = table;
		seqRequest.dependsColumn = info.dependsColumn();
		seqRequest.dependsId = info.dependsId();
		seqRequest.seqColumn = info.seqColumn();

		return SeqHandler.getInstance()
			.nextSeqAndGet(
				seqRequest,
				seq -> registerInternal(
					table,
					request,
					info,
					seq,
					userId,
					consumer));
	}

	private static UUID registerInternal(
		TablePath table,
		RegisterRequest request,
		RegisterInfo info,
		long seqValue,
		UUID userId,
		Consumer<GenericTable.Row> consumer) {
		var row = new GenericTable(table).row();

		UUID uuid = UUID.randomUUID();

		row.setUUID(id, uuid);

		row.setUUID(info.dependsColumn(), info.dependsId());
		row.setLong(info.seqColumn(), seqValue);
		row.setString(name, request.name);
		row.setString(code, request.code.orElseGet(() -> info.generateDefaultCode(seqValue)));
		request.description.ifPresent(v -> row.setString(description, v));
		request.props.ifPresent(v -> row.setObject(props, toPGObject(v)));
		request.tags.ifPresent(v -> row.setObject(tags, v));

		row.setUUID(created_by, userId);
		row.setUUID(updated_by, userId);

		consumer.accept(row);

		row.insert();

		request.tags.ifPresent(tags -> TagExecutor.stickTags(tags, uuid, table));

		return uuid;
	}

	static void update(
		TablePath table,
		UpdateRequest request,
		UUID userId,
		Consumer<UpdateAssist> consumer) {
		int result = new GenericTable(table).UPDATE(a -> {
			a.col(revision).set("{0} + 1", Vargs.of(a.col(revision)), Vargs.of());
			request.name.ifPresent(v -> a.col(name).set(v));
			request.description.ifPresent(v -> a.col(description).set(v));
			request.props.ifPresent(v -> a.col(props).set(toPGObject(v)));
			request.tags.ifPresent(v -> a.col(tags).set((Object) v));
			request.active.ifPresent(v -> a.col(active).set(v));
			a.col(updated_by).set(userId);
			a.col(updated_at).setAny("now()");
			consumer.accept(a);
		}).WHERE(a -> a.col(id).eq(request.id).AND.col(revision).eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(table, request.id);

		request.tags.ifPresent(tags -> TagExecutor.stickTagsAgain(tags, request.id, table));
	}

	static void delete(TablePath table, UUID id, long revision) {
		int result = new GenericTable(table)
			.DELETE()
			.WHERE(a -> a.col(StockComponentHandler.id).eq(id).AND.col(StockComponentHandler.revision).eq(revision))
			.execute();

		if (result != 1) throw Utils.decisionException(table, id);
	}
}
