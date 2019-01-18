package jp.ats.blackbox.web;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.blendee.dialect.postgresql.PostgreSQLErrorConverter;
import org.blendee.jdbc.OptionKey;
import org.blendee.util.Blendee;
import org.blendee.util.BlendeeConstants;

public class BlendeeStarter {

	private static boolean blendeeStarted = false;

	private static synchronized void setStarted() {
		blendeeStarted = true;
	}

	public static boolean started() {
		return blendeeStarted;
	}

	public static void start(ServletContext context) {
		start(
			Collections.list(context.getInitParameterNames())
				.stream()
				.collect(
					Collectors.toConcurrentMap(
						key -> BlendeeConstants.convert(removePrefix(key)),
						key -> BlendeeConstants.convert(removePrefix(key)).parse(context.getInitParameter(key)))));
	}

	public static void start(Map<OptionKey<?>, Object> param) {
		if (started()) return;

		//アプリケーション稼働に必須の設定
		param.put(BlendeeConstants.ERROR_CONVERTER_CLASS, PostgreSQLErrorConverter.class);
		param.put(BlendeeConstants.TABLE_FACADE_PACKAGE, "sqlassist");

		Blendee.start(param);

		setStarted();
	}

	private static String removePrefix(String key) {
		return key.substring("blendee-".length());
	}
}
