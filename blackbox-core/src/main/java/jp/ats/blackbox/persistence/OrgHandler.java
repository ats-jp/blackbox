package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.function.Consumer;

import org.blendee.jdbc.Result;

import sqlassist.bb.orgs;

public class OrgHandler {

	public static long register(String name, String extension) {
		var row = orgs.row();

		row.setName(name);
		row.setExtension(extension);

		return CommonHandler.register(row);
	}

	public static void update(
		long id,
		long revision,
		Optional<String> name,
		Optional<String> extension,
		Optional<Boolean> active) {
		orgs facade = new orgs();
		int result = facade.UPDATE(a -> {
			a.revision.set(revision + 1);
			name.ifPresent(v -> a.name.set(v));
			extension.ifPresent(v -> a.extension.set(v));
			active.ifPresent(v -> a.active.set(v));
		}).WHERE(a -> a.id.eq(id).AND.revision.eq(revision)).execute();

		if (result != 1) throw Utils.decisionException(orgs.$TABLE, id);
	}

	public static void delete(long id, long revision) {
		int result = new orgs().DELETE().WHERE(a -> a.id.eq(id).AND.revision.eq(revision)).execute();

		if (result != 1) throw Utils.decisionException(orgs.$TABLE, id);
	}

	public static void fetch(long id, Consumer<Result> consumer) {
		new orgs().fetch(id);
	}
}
