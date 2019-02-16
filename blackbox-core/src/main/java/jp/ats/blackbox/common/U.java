package jp.ats.blackbox.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.blendee.jdbc.Result;
import org.blendee.sql.Recorder;

public class U {

	public static final Charset defaultCharset = StandardCharsets.UTF_8;

	public static final UUID NULL_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	public static final long LONG_NULL_ID = 0L;

	public static final UUID PRIVILEGE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	public static final Recorder recorder = Recorder.instance();

	public static LocalDateTime convert(Timestamp timestamp) {
		return LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
	}

	public static Timestamp convert(LocalDateTime dateTime) {
		var zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
		return Timestamp.from(zonedDateTime.toInstant());
	}

	public static UUID uuid(Result result, int parameterIndex) {
		return (UUID) result.getObject(parameterIndex);
	}

	public static UUID uuid(Result result, String columnName) {
		return (UUID) result.getObject(columnName);
	}

	public static String getStackTrace(Throwable t) {
		var out = new ByteArrayOutputStream();
		var writer = new PrintWriter(out);
		t.printStackTrace(writer);
		writer.flush();
		return new String(out.toByteArray(), defaultCharset);
	}

	public static Optional<String> getSQLState(Throwable t) {
		if (t instanceof SQLException) {
			return Optional.of(((SQLException) t).getSQLState());
		}

		var cause = t.getCause();

		if (cause == null) return Optional.empty();

		return getSQLState(cause);
	}
}
