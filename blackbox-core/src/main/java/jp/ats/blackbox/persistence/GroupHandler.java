package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.UUID;

import sqlassist.bb.groups;

public class GroupHandler {

	public static UUID register(RegisterRequest request) {
		var row = groups.row();

		UUID id = UUID.randomUUID();

		row.setId(id);
		row.setOrg_id(SecurityValues.currentOrgId());
		row.setParent_id(request.parent_id);
		request.extension.ifPresent(v -> row.setExtension(v));
		request.tags.ifPresent(v -> row.setTags(v));

		UUID userId = SecurityValues.currentUserId();

		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		request.tags.ifPresent(v -> TagHandler.stickTags(v, groups.$TABLE, id));

		return id;
	}

	public static void update(UpdateRequest request) {}

	public static class RegisterRequest {

		public String name;

		public UUID parent_id;

		public Optional<String> extension = Optional.empty();

		public Optional<String[]> tags = Optional.empty();
	}

	public static class UpdateRequest {

		public UUID id;

		public Optional<String> name = Optional.empty();

		public Optional<UUID> parent_id = Optional.empty();

		public long revision;

		public Optional<String> extension = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}
}
