package jp.ats.blackbox.backend.api.core;

import static jp.ats.blackbox.backend.api.Utils.handleError;
import static jp.ats.blackbox.backend.api.Utils.parse;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jp.ats.blackbox.backend.api.Utils.IdResult;
import jp.ats.blackbox.backend.api.Utils.JsonProcessingException;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.controller.JournalController;
import jp.ats.blackbox.core.controller.JournalController.JournalNotFoundException;
import jp.ats.blackbox.core.executor.CommandFailedException;
import jp.ats.blackbox.core.persistence.Requests.JournalDenyRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalOverwriteRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalRegisterRequest;

@Path("journals")
public class Journals {

	@POST
	@Path("register")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String register(String json) {
		try {
			var promise = JournalController.register(parse(json, JournalRegisterRequest.class));

			promise.waitUntilFinished();

			var result = new IdResult();
			result.id = promise.getId();
			result.success = true;

			return U.toJson(result);
		} catch (JsonProcessingException | InterruptedException | CommandFailedException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	@POST
	@Path("register_nowait")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String registerNowait(String json) {
		try {
			var result = new IdResult();
			result.id = JournalController.register(parse(json, JournalRegisterRequest.class)).getId();
			result.success = true;

			return U.toJson(result);
		} catch (JsonProcessingException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	@POST
	@Path("register_lazily")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String registerLazily(String json) {
		try {
			var promise = JournalController.registerLazily(parse(json, JournalRegisterRequest.class));

			promise.waitUntilFinished();

			var result = new IdResult();
			result.id = promise.getId();
			result.success = true;

			return U.toJson(result);
		} catch (JsonProcessingException | InterruptedException | CommandFailedException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	@POST
	@Path("register_lazily_nowait")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String registerLazilyNowait(String json) {
		try {
			var result = new IdResult();
			result.id = JournalController.registerLazily(parse(json, JournalRegisterRequest.class)).getId();
			result.success = true;

			return U.toJson(result);
		} catch (JsonProcessingException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	@POST
	@Path("deny")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String deny(String json) {
		try {
			var promise = JournalController.deny(parse(json, JournalDenyRequest.class));

			promise.waitUntilFinished();

			var result = new IdResult();
			result.id = promise.getId();
			result.success = true;

			return U.toJson(result);
		} catch (JsonProcessingException | InterruptedException | CommandFailedException | JournalNotFoundException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	@POST
	@Path("deny_nowait")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String denyNowait(String json) {
		try {
			var result = new IdResult();
			result.id = JournalController.deny(parse(json, JournalDenyRequest.class)).getId();
			result.success = true;

			return U.toJson(result);
		} catch (JsonProcessingException | JournalNotFoundException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	public static class OverwriteResult {

		public boolean success;

		public UUID id;

		public UUID[] denied_ids;

		public String error;
	}

	@POST
	@Path("overwrite")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String overwrite(String json) {
		try {
			var promise = JournalController.overwrite(parse(json, JournalOverwriteRequest.class));

			promise.waitUntilFinished();

			var result = new OverwriteResult();
			result.id = promise.getId();
			result.success = true;
			result.denied_ids = promise.deniedJournalIds();

			return U.toJson(result);
		} catch (JsonProcessingException | InterruptedException | CommandFailedException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	@POST
	@Path("overwrite_nowait")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String overwriteNowait(String json) {
		try {
			var promise = JournalController.overwrite(parse(json, JournalOverwriteRequest.class));

			var result = new OverwriteResult();
			result.id = promise.getId();
			result.success = true;
			result.denied_ids = promise.deniedJournalIds();

			return U.toJson(result);
		} catch (JsonProcessingException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}
}
