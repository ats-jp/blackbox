package jp.ats.blackbox.backend.api.core;

import static jp.ats.blackbox.backend.api.Utils.handleError;
import static jp.ats.blackbox.backend.api.Utils.parse;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jp.ats.blackbox.backend.api.Utils.JsonProcessingException;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.controller.JournalController;
import jp.ats.blackbox.core.executor.CommandFailedException;
import jp.ats.blackbox.core.executor.JournalExecutor.PausingGroup;
import jp.ats.blackbox.core.persistence.Requests.GroupPauseRequest;
import jp.ats.blackbox.core.persistence.Requests.GroupProcessRequest;

public class Groups {

	@SuppressWarnings("unused")
	private static class GroupsResult {

		private boolean success;

		private UUID[] ids;

		private String error;
	}

	@POST
	@Path("pause")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String pause(String json) {
		try {
			return processPausingGroups(JournalController.pauseGroups(parse(json, GroupPauseRequest.class)));
		} catch (JsonProcessingException | InterruptedException | CommandFailedException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	@POST
	@Path("resume")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String resume(String json) {
		try {
			return processPausingGroups(JournalController.resumeGroups(parse(json, GroupProcessRequest.class)));
		} catch (JsonProcessingException | InterruptedException | CommandFailedException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	@POST
	@Path("get_pausing")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String getPausing(String json) {
		try {
			return processPausingGroups(JournalController.getPausingGroups(parse(json, GroupProcessRequest.class)));
		} catch (JsonProcessingException | InterruptedException | CommandFailedException e) {
			return handleError(e);
		} catch (Throwable t) {
			return handleError(t);
		}
	}

	private String processPausingGroups(PausingGroup[] groups) {
		var result = new GroupsResult();
		var ids = Arrays.asList(groups).stream().map(g -> g.groupId).collect(Collectors.toList());
		result.ids = ids.toArray(new UUID[ids.size()]);
		result.success = true;

		return U.toJson(result);
	}
}
