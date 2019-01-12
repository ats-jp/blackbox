package jp.ats.blackbox.persistence;

import java.sql.SQLException;
import java.util.UUID;

import org.blendee.jdbc.TablePath;
import org.blendee.util.GenericTable;
import org.postgresql.jdbc.PgArray;

class Utils {

	static BlackboxPersistenceException decisionException(TablePath table, UUID id) {
		return new GenericTable(table).WHERE(a -> a.col("id").eq(id)).count() > 0
			? new DataAlreadyUpdatedException()
			: new DataNotFoundException(table, id);
	}

	static String[] restoreTags(Object tags) throws SQLException {
		var pgArray = (PgArray) tags;
		return (String[]) pgArray.getArray();
	}
}
