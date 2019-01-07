package jp.ats.blackbox.test;

import java.util.HashMap;
import java.util.Map;

import org.blendee.jdbc.OptionKey;
import org.blendee.util.Blendee;
import org.blendee.util.BlendeeConstants;

public class TransferCommon {

	public static void start() {
		var param = param();
		param.put(BlendeeConstants.LOGGER_CLASS, org.blendee.jdbc.VoidLogger.class);

		Blendee.start(param);
	}

	public static void startWithLog() {
		var param = param();
		param.put(BlendeeConstants.LOGGER_CLASS, org.blendee.jdbc.SystemOutLogger.class);

		Blendee.start(param);
	}

	private static Map<OptionKey<?>, Object> param() {
		Map<OptionKey<?>, Object> param = new HashMap<>();
		param.put(BlendeeConstants.SCHEMA_NAMES, new String[] { "bb" });
		param.put(BlendeeConstants.LOG_STACKTRACE_FILTER, "^jp\\.ats\\.blackbox\\.");
		param.put(BlendeeConstants.TABLE_FACADE_PACKAGE, "sqlassist");
		param.put(BlendeeConstants.CAN_ADD_NEW_ENTRIES, true);
		param.put(BlendeeConstants.TRANSACTION_FACTORY_CLASS, TransferTestTransactionFactory.class);
		param.put(BlendeeConstants.JDBC_DRIVER_CLASS_NAME, "org.postgresql.Driver");
		param.put(BlendeeConstants.JDBC_URL, "jdbc:postgresql://192.168.193.128:5432/blackbox");
		param.put(BlendeeConstants.JDBC_USER, "blackbox");
		param.put(BlendeeConstants.JDBC_PASSWORD, "blackbox");
		param.put(BlendeeConstants.HOME_STORAGE_IDENTIFIER, "blackbox-core");
		//param.put(BlendeeConstants.AUTO_CLOSE_INTERVAL_MILLIS, 300);

		return param;
	}
}
