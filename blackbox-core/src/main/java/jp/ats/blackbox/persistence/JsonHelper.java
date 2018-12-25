package jp.ats.blackbox.persistence;

import java.sql.SQLException;

import org.postgresql.util.PGobject;

public class JsonHelper {

	public static Object toJson(String json) {
		PGobject object = new PGobject();
		object.setType("json");
		try {
			object.setValue(json);
		} catch (SQLException e) {
			throw new IllegalJsonException();
		}

		return object;
	}
}
