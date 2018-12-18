package jp.ats.blackbox.persistence;

import org.blendee.assist.Row;
import org.blendee.dialect.postgresql.ReturningUtilities;

import jp.ats.blackbox.blendee.bb.orgs;

public class CommonHandler {

	public static long register(Row row) {
		long[] id = { 0 };
		ReturningUtilities.insert(row, r -> {
			id[0] = r.getLong(orgs.id);
		}, orgs.id);

		return id[0];
	}
}
