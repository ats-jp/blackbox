package jp.ats.blackbox.test;

import java.util.UUID;

import org.blendee.util.annotation.SQL;
import org.blendee.util.annotation.SQLProxy;

@SQLProxy
public interface TestSQLProxy {

	SQLProxy.ResultSet select1(UUID id);

	@SQL("SELECT "
		+ "  g.id AS group_id, "
		+ "  o.id AS org_id "
		+ "FROM "
		+ "  bb.groups g "
		+ "JOIN "
		+ "  bb.orgs o "
		+ "ON "
		+ "  g.org_id = o.id "
		+ "WHERE "
		+ "  g.id = ${id}")
	SQLProxy.ResultSet select2(UUID id);
}
