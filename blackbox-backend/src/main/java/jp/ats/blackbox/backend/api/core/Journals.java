package jp.ats.blackbox.backend.api.core;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.ats.blackbox.backend.controller.JournalController;
import jp.ats.blackbox.backend.controller.JournalController.JsonProcessingException;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.CommandFailedException;

@Path("journals")
public class Journals {

	private static final Logger logger = LogManager.getLogger(Journals.class);

	private static final String errorMessage = "処理中に問題が発生しました";

	public static class JournalResult {

		public boolean success;

		public UUID id;

		public String error;
	}

	@POST
	@Path("register")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String registerJournal(String json) {
		var result = new JournalResult();
		UUID id;
		try {
			id = JournalController.register(json);

			result.success = true;
		} catch (JsonProcessingException | InterruptedException e) {
			logger.fatal(e);

			id = null;
			result.success = false;
			result.error = errorMessage;
		} catch (CommandFailedException e) {
			id = null;
			result.success = false;
			result.error = errorMessage;
		}

		result.id = id;

		return U.toJson(result);
	}

	@POST
	@Path("register_nowait")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String registerJournalNowait(String json) {
		var result = new JournalResult();
		UUID id;
		try {
			id = JournalController.registerNowait(json);

			result.success = true;
		} catch (JsonProcessingException e) {
			logger.fatal(e);

			id = null;
			result.success = false;
			result.error = errorMessage;
		}

		result.id = id;

		return U.toJson(result);
	}
}
