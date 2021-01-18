package jp.ats.blackbox.backend.api.core;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.ats.blackbox.common.U;

public class Utils {

	private static final String errorMessage = "処理中に問題が発生しました";

	private static final Logger logger = LogManager.getLogger(Journals.class);

	static class IdResult {

		public boolean success;

		public UUID id;

		public String error;
	}

	static String handleError(Throwable t) {
		logger.fatal(t);

		var result = new IdResult();

		result.id = null;
		result.success = false;
		result.error = errorMessage;

		return U.toJson(result);
	}
}
