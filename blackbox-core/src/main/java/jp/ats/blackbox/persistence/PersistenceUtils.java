package jp.ats.blackbox.persistence;

import org.blendee.jdbc.TablePath;
import org.blendee.util.GenericTable;

class Utils {

	static BlackboxPersistenceException decisionException(TablePath table, long id) {
		return new GenericTable(table).WHERE(a -> a.col("id").eq(id)).count() > 0
			? new DataAlreadyUpdatedException()
			: new DataNotFoundException();
	}
}
