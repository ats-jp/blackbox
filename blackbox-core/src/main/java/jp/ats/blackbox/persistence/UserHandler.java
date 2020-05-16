package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.executor.TagExecutor;
import sqlassist.bb.users;

public class UserHandler {

	public static UUID register(String name, Role role, UUID groupId, Optional<String> props, Optional<String[]> tags) {
		var request = new SeqHandler.Request();
		request.table = users.$TABLE;
		request.dependsColumn = users.group_id;
		request.dependsId = groupId;
		return SeqHandler.getInstance().nextSeqAndGet(request, seq -> {
			return registerInternal(name, role, groupId, seq, props, tags);
		});
	}

	private static UUID registerInternal(String name, Role role, UUID groupId, long seq, Optional<String> props, Optional<String[]> tags) {
		var row = users.row();

		UUID id = UUID.randomUUID();

		UUID userId = SecurityValues.currentUserId();

		row.setId(id);
		row.setName(name);
		row.setRole(role.value());
		row.setGroup_id(groupId);
		row.setSeq(seq);
		props.ifPresent(v -> row.setProps(JsonHelper.toJson(v)));
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		tags.ifPresent(v -> TagExecutor.stickTags(v, id, users.$TABLE));

		return id;
	}

	public static void update(
		UUID id,
		long revision,
		Optional<String> name,
		Optional<String> props,
		Optional<String[]> tags,
		Optional<Boolean> active) {
		int result = new users().UPDATE(a -> {
			a.revision.set(revision + 1);
			name.ifPresent(v -> a.name.set(v));
			props.ifPresent(v -> a.props.set(JsonHelper.toJson(v)));
			tags.ifPresent(v -> a.tags.set((Object) v));
			active.ifPresent(v -> a.active.set(v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(id).AND.revision.eq(revision)).execute();

		if (result != 1) throw Utils.decisionException(users.$TABLE, id);

		tags.ifPresent(v -> TagExecutor.stickTagsAgain(v, id, users.$TABLE));
	}

	public static void delete(UUID userId, long revision) {
		Utils.delete(users.$TABLE, userId, revision);
	}

	public static users.Row fetch(UUID id) {
		return new users().fetch(id).get();
	}
}
