package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.UUID;

import sqlassist.bb.users;

public class UserHandler {

	public static UUID register(String name, UUID groupId, String props) {
		var row = users.row();

		UUID id = UUID.randomUUID();

		UUID userId = SecurityValues.currentUserId();

		row.setId(id);
		row.setName(name);
		row.setGroup_id(groupId);
		row.setProps(props);
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		return id;
	}

	public static void update(
		UUID id,
		long revision,
		Optional<String> name,
		Optional<UUID> groupId,
		Optional<String> props,
		Optional<String[]> tags,
		Optional<Boolean> active) {
		int result = new users().UPDATE(a -> {
			a.revision.set(revision + 1);
			name.ifPresent(v -> a.name.set(v));
			groupId.ifPresent(v -> a.group_id.set(v));
			props.ifPresent(v -> a.props.set(JsonHelper.toJson(v)));
			tags.ifPresent(v -> a.tags.set((Object) v));
			active.ifPresent(v -> a.active.set(v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(id).AND.revision.eq(revision)).execute();

		if (result != 1) throw Utils.decisionException(users.$TABLE, id);
	}

	public static void delete(UUID userId, long revision) {
		Utils.delete(users.$TABLE, userId, revision);
	}

	public static users.Row fetch(UUID id) {
		return new users().fetch(id).get();
	}
}
