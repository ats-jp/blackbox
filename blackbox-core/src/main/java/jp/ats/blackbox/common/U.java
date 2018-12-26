package jp.ats.blackbox.common;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class U {

	public static LocalDateTime convert(Timestamp timestamp) {
		return LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
	}
}
