package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.UUID;

import sqlassist.bb.orgs;

public class OrgHandler {

	public static UUID register(String name, String extension) {
		var row = orgs.row();

		UUID id = UUID.randomUUID();

		UUID userId = SecurityValues.currentUserId();

		row.setId(id);
		row.setName(name);
		row.setProps(extension);
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		return id;
	}

	public static void update(
		UUID id,
		long revision,
		Optional<String> name,
		Optional<String> extension,
		Optional<Boolean> active) {
		int result = new orgs().UPDATE(a -> {
			a.revision.set(revision + 1);
			name.ifPresent(v -> a.name.set(v));
			extension.ifPresent(v -> a.props.set(v));
			active.ifPresent(v -> a.active.set(v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(id).AND.revision.eq(revision)).execute();

		if (result != 1) throw Utils.decisionException(orgs.$TABLE, id);
	}

	public static void delete(UUID orgId, long revision) {
		Utils.delete(orgs.$TABLE, orgId, revision);
	}

	public static orgs.Row fetch(UUID id) {
		return new orgs().fetch(id).get();
	}
}
