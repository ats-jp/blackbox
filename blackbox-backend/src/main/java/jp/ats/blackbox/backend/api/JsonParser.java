package jp.ats.blackbox.backend.api;

import com.google.gson.JsonSyntaxException;

import jp.ats.blackbox.common.U;

public class JsonParser {

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
