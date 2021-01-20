package jp.ats.blackbox.backend.api;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonSyntaxException;

import jp.ats.blackbox.backend.api.core.Journals;
import jp.ats.blackbox.common.U;

public class Utils {

	private static final String errorMessage = "処理中に問題が発生しました";

	private static final Logger logger = LogManager.getLogger(Journals.class);

	public static class IdResult {

		public boolean success;

		public UUID id;

		public String error;
	}

	public static String handleError(Throwable t) {
		t.printStackTrace();
		logger.fatal(t);

		var result = new IdResult();

		result.id = null;
		result.success = false;
		result.error = errorMessage;

		return U.toJson(result);
	}

	public static <T> T parse(String json, Class<T> objectClass) throws JsonProcessingException {
		try {
			return U.fromJson(json, objectClass);
		} catch (JsonSyntaxException e) {
			throw new JsonProcessingException(e);
		}
	}

	@SuppressWarnings("serial")
	public static class JsonProcessingException extends Exception {

		private JsonProcessingException(JsonSyntaxException e) {
			super(e);

		}
	}
}
