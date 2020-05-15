package jp.ats.blackbox.test;

import java.util.Map;

import org.blendee.dialect.postgresql.PostgreSQLErrorConverter;
import org.blendee.jdbc.OptionKey;
import org.blendee.util.Blendee;
import org.blendee.util.BlendeeConstants;

public class TestBlendeeStarter {

	private static boolean blendeeStarted = false;

	private static synchronized void setStarted() {
		blendeeStarted = true;
	}

	public static boolean started() {
		return blendeeStarted;
	}

	public static void start(Map<OptionKey<?>, Object> param) {
		if (started()) return;

		//アプリケーション稼働に必須の設定
		param.put(BlendeeConstants.ERROR_CONVERTER_CLASS, PostgreSQLErrorConverter.class);
		param.put(BlendeeConstants.TABLE_FACADE_PACKAGE, "sqlassist");

		Blendee.start(param);

		setStarted();
	}
}
