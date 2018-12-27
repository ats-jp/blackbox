package jp.ats.blackbox.web;

import java.util.Collections;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

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
		if (started()) return;

		Blendee.start(
			Collections.list(context.getInitParameterNames())
				.stream()
				.collect(
					Collectors.toConcurrentMap(
						key -> BlendeeConstants.convert(removePrefix(key)),
						key -> BlendeeConstants.convert(removePrefix(key)).parse(context.getInitParameter(key)))));

		setStarted();
	}

	private static String removePrefix(String key) {
		return key.substring("blendee-".length());
	}
}
