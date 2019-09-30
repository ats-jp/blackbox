package jp.ats.blackbox.test;

import java.util.Map;

import org.blendee.jdbc.OptionKey;
import org.blendee.util.Blendee;
import org.blendee.util.BlendeeConstants;

public class JournalCommon {

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
		Map<OptionKey<?>, Object> param = Common.param();
		param.put(BlendeeConstants.TRANSACTION_FACTORY_CLASS, JournalTestTransactionFactory.class);
		param.put(BlendeeConstants.AUTO_CLOSE_INTERVAL_MILLIS, 0); //テスト時はコネクションを閉じないので起動しない

		return param;
	}
}
