package jp.ats.blackbox.persistence;

import java.util.Optional;

import sqlassist.bb.groups;

public class GroupHandler {

	public static long register(RegisterRequest request) {
		var row = groups.row();

		row.setOrg_id(SecurityValues.currentOrgId());
		row.setParent_id(request.parent_id);
		request.extension.ifPresent(v -> row.setExtension(v));
		request.tags.ifPresent(v -> row.setTags(v));

		long userId = SecurityValues.currentUserId();

		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		long groupId = CommonHandler.register(row);

		request.tags.ifPresent(v -> TagHandler.stickTags(v, groups.$TABLE, groupId));

		return groupId;
	}

	public static void update(UpdateRequest request) {}

	public static class RegisterRequest {

		public String name;

		public long parent_id;

		public Optional<String> extension = Optional.empty();

		public Optional<String[]> tags = Optional.empty();
	}

	public static class UpdateRequest {

		public long id;

		public Optional<String> name = Optional.empty();

		public Optional<Long> parent_id = Optional.empty();

		public long revision;

		public Optional<String> extension = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}
}
