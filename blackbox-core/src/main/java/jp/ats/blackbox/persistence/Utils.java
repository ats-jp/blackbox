package jp.ats.blackbox.persistence;

import java.sql.SQLException;
import java.util.UUID;

import org.blendee.jdbc.TablePath;
import org.blendee.jdbc.exception.ForeignKeyConstraintViolationException;
import org.blendee.util.GenericTable;
import org.postgresql.jdbc.PgArray;

import jp.ats.blackbox.common.BlackboxException;

public class Utils {

	public static BlackboxException decisionException(TablePath table, UUID id) {
		return new GenericTable(table).WHERE(a -> a.col("id").eq(id)).count() > 0
			? new DataAlreadyUpdatedException()
			: new DataNotFoundException(table, id);
	}

	public static String[] restoreTags(Object tags) throws SQLException {
		var pgArray = (PgArray) tags;
		return (String[]) pgArray.getArray();
	}

	static void delete(TablePath table, UUID id, long revision) {
		try {
			if (new GenericTable(table).DELETE().WHERE(a -> a.col("id").eq(id).AND.col("revision").eq(revision)).execute() != 1)
				throw Utils.decisionException(table, id);
		} catch (ForeignKeyConstraintViolationException e) {
			throw new AlreadyUsedException(table, id, e);
		}
	}
}
