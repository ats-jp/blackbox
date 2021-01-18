package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.TagExecutor;
import sqlassist.bb.users;

public class UserHandler {

	public static class RegisterRequest {

		public String name;

		public Optional<String> description = Optional.empty();

		public Role role;

		public UUID groupId;

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();
	}

	public static UUID register(RegisterRequest request) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = users.$TABLE;
		seqRequest.dependsColumn = users.group_id;
		seqRequest.dependsId = request.groupId;
		return SeqHandler.getInstance().nextSeqAndGet(seqRequest, seq -> {
			return registerInternal(request, seq);
		});
	}

	private static UUID registerInternal(RegisterRequest request, long seq) {
		var row = users.row();

		UUID id = UUID.randomUUID();

		UUID userId = SecurityValues.currentUserId();

		row.setId(id);
		row.setName(request.name);
		request.description.ifPresent(v -> row.setDescription(v));
		row.setRole(request.role.value());
		row.setGroup_id(request.groupId);
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

		public Optional<String> description = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}

	public static void update(UpdateRequest request) {
		int result = new users().UPDATE(a -> {
			a.revision.set(request.revision + 1);
			request.name.ifPresent(v -> a.name.set(v));
			request.description.ifPresent(v -> a.name.set(v));
			request.props.ifPresent(v -> a.props.set(U.toPGObject(v)));
			request.tags.ifPresent(v -> a.tags.set((Object) v));
			request.active.ifPresent(v -> a.active.set(v));
			a.updated_by.set(SecurityValues.currentUserId());
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
