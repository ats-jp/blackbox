package jp.ats.blackbox.core.persistence;

import java.sql.SQLException;
import java.util.UUID;

import org.blendee.assist.SelectStatement;
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

	public static void delete(TablePath table, UUID id, long revision) throws AlreadyUsedException {
		try {
			if (new GenericTable(table).DELETE().WHERE(a -> a.col("id").eq(id).AND.col("revision").eq(revision)).execute() != 1)
				throw Utils.decisionException(table, id);
		} catch (ForeignKeyConstraintViolationException e) {
			throw new AlreadyUsedException(table, id, e);
		}
	}

	public static void updateRevision(TablePath table, long revision, SelectStatement subquery) {
		var result = new GenericTable(table).UPDATE(
			a -> a.ls(
				a.column("revision").set(revision + 1),
				a.column("updated_at").setAny("now()"),
				a.column("updated_by").set(SecurityValues.currentUserId())))
			.WHERE(a -> a.column("id").IN(subquery).AND.column("revision").eq(revision))
			.execute();

		if (result != 1) {
			var id = new GenericTable(table)
				.SELECT(a -> a.column("id"))
				.WHERE(a -> a.column("id").IN(subquery).AND.column("revision").eq(revision))
				.willUnique()
				.get()
				.getUUID("id");
			throw Utils.decisionException(table, id);
		}
	}

	public static void updateRevision(TablePath table, long revision, UUID id) {
		var result = new GenericTable(table).UPDATE(
			a -> a.ls(
				a.column("revision").set(revision + 1),
				a.column("updated_at").setAny("now()"),
				a.column("updated_by").set(SecurityValues.currentUserId())))
			.WHERE(a -> a.column("id").eq(id).AND.column("revision").eq(revision))
			.execute();

		if (result != 1) {
			throw Utils.decisionException(table, id);
		}
	}
}
