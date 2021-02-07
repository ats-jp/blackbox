package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.TagExecutor;
import sqlassist.bb.users;

public class UserHandler {

	public static class RegisterRequest {

		public String name;

		public Optional<String> code = Optional.empty();

		public Optional<String> description = Optional.empty();

		public Privilege privilege;

		public UUID group_id;

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();
	}

	public static UUID register(RegisterRequest request, UUID userId) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = users.$TABLE;
		seqRequest.dependsColumn = users.group_id;
		seqRequest.dependsId = request.group_id;
		return SeqHandler.getInstance().nextSeqAndGet(seqRequest, seq -> {
			return registerInternal(request, seq, userId);
		});
	}

	private static UUID registerInternal(RegisterRequest request, long seq, UUID userId) {
		var row = users.row();

		UUID id = UUID.randomUUID();

		row.setId(id);
		row.setName(request.name);
		row.setCode(request.code.orElseGet(() -> DefaultCodeGenerator.generate(U.recorder, request.group_id, seq)));
		request.description.ifPresent(v -> row.setDescription(v));
		row.setPrivilege(request.privilege.value);
		row.setGroup_id(request.group_id);
		row.setSeq(seq);
		request.tags.ifPresent(v -> row.setTags(v));
		request.props.ifPresent(v -> row.setProps(U.toPGObject(v)));
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		request.tags.ifPresent(v -> TagExecutor.stickTags(v, id, users.$TABLE));

		return id;
	}

	public static class UpdateRequest {

		public UUID id;

		public long revision;

		public Optional<String> name = Optional.empty();

		public Optional<String> code = Optional.empty();

		public Optional<String> description = Optional.empty();

		public Optional<Privilege> privilege = Optional.empty();

		public Optional<UUID> group_id = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}

	public static void update(UpdateRequest request, UUID userId) {
		int result = new users().UPDATE(a -> {
			a.revision.set(request.revision + 1);
			request.name.ifPresent(v -> a.name.set(v));
			request.code.ifPresent(v -> a.code.set(v));
			request.description.ifPresent(v -> a.name.set(v));
			request.privilege.ifPresent(v -> a.privilege.set(v.value));
			request.group_id.ifPresent(v -> a.group_id.set(v));
			request.props.ifPresent(v -> a.props.set(U.toPGObject(v)));
			request.tags.ifPresent(v -> a.tags.set((Object) v));
			request.active.ifPresent(v -> a.active.set(v));
			a.updated_by.set(userId);
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(users.$TABLE, request.id);

		request.tags.ifPresent(v -> TagExecutor.stickTagsAgain(v, request.id, users.$TABLE));
	}

	public static void delete(UUID userId, long revision) {
		Utils.delete(users.$TABLE, userId, revision);
	}

	public static users.Row get(UUID id) {
		return optional(id).get();
	}

	public static Optional<users.Row> optional(UUID id) {
		return new users().fetch(id);
	}
}
