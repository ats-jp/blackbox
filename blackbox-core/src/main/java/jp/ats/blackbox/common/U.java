package jp.ats.blackbox.common;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.blendee.jdbc.Result;
import org.blendee.sql.Recorder;

public class U {

	public static final UUID NULL = UUID.fromString("00000000-0000-0000-0000-000000000000");

	public static final Recorder recorder = new Recorder();

	public static LocalDateTime convert(Timestamp timestamp) {
		return LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
	}

	public static UUID uuid(Result result, int parameterIndex) {
		return (UUID) result.getObject(parameterIndex);
	}

	public static UUID uuid(Result result, String columnName) {
		return (UUID) result.getObject(columnName);
	}
}
