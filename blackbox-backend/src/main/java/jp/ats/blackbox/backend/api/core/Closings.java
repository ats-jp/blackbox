package jp.ats.blackbox.backend.api.core;

import static jp.ats.blackbox.backend.api.Utils.handleError;
import static jp.ats.blackbox.backend.api.Utils.parse;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jp.ats.blackbox.backend.api.Utils.IdResult;
import jp.ats.blackbox.backend.api.Utils.JsonProcessingException;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.controller.JournalController;
import jp.ats.blackbox.executor.CommandFailedException;
import jp.ats.blackbox.persistence.Requests.ClosingRequest;

public class Closings {

	@POST
	@Path("close")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String close(String json) {
		try {
			var promise = JournalController.close(parse(json, ClosingRequest.class));

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
	@Path("close_nowait")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String closeNowait(String json) {
		try {
			var result = new IdResult();
			result.id = JournalController.close(parse(json, ClosingRequest.class)).getId();
			result.success = true;

			return U.toJson(result);
		} catch (JsonProcessingException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}
}
