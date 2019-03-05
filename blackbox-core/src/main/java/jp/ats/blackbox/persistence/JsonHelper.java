package jp.ats.blackbox.persistence;

import java.sql.SQLException;

import org.postgresql.util.PGobject;

public class JsonHelper {

	//JDBC URLに?stringtype=unspecifiedとつけてもJSONを変換してくれるようになるが、JDBC URLはソース管理に含まれないので直接変換してセットすることとする
	public static Object toJson(String json) {
		var object = new PGobject();
		object.setType("json");
		try {
			object.setValue(json);
		} catch (SQLException e) {
			throw new IllegalJsonException();
		}

		return object;
	}

	public static String toString(Object json) {
		return ((PGobject) json).getValue();
	}
}
