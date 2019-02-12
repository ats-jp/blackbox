package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.UUID;

import sqlassist.bb.orgs;

public class OrgHandler {

	public static UUID register(String name, String extension) {
		var row = orgs.row();

		UUID id = UUID.randomUUID();

		row.setId(id);
		row.setName(name);
		row.setExtension(extension);

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
			extension.ifPresent(v -> a.extension.set(v));
			active.ifPresent(v -> a.active.set(v));
		}).WHERE(a -> a.id.eq(id).AND.revision.eq(revision)).execute();

		if (result != 1) throw Utils.decisionException(orgs.$TABLE, id);
	}

	public static void delete(UUID id, long revision) {
		int result = new orgs().DELETE().WHERE(a -> a.id.eq(id).AND.revision.eq(revision)).execute();

		if (result != 1) throw Utils.decisionException(orgs.$TABLE, id);
	}

	public static orgs.Row fetch(UUID id) {
		return new orgs().fetch(id).get();
	}
}
