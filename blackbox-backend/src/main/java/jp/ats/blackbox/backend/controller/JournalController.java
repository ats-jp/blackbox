package jp.ats.blackbox.backend.controller;

import java.util.UUID;

import com.google.gson.JsonSyntaxException;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.CommandFailedException;
import jp.ats.blackbox.executor.JournalExecutor.PausingGroup;
import jp.ats.blackbox.executor.JournalExecutorMap;
import jp.ats.blackbox.executor.JournalPromise;
import jp.ats.blackbox.persistence.Requests.ClosingRequest;
import jp.ats.blackbox.persistence.Requests.GroupPauseRequest;
import jp.ats.blackbox.persistence.Requests.GroupProcessRequest;
import jp.ats.blackbox.persistence.Requests.JournalDenyRequest;
import jp.ats.blackbox.persistence.Requests.JournalOverwriteRequest;
import jp.ats.blackbox.persistence.Requests.JournalRegisterRequest;
import jp.ats.blackbox.persistence.Requests.TransientMoveRequest;
import jp.ats.blackbox.persistence.SecurityValues;
import sqlassist.bb.journals;
import sqlassist.bb.transients;

public class JournalController {

	public static UUID register(String requestJson) throws JsonProcessingException, CommandFailedException, InterruptedException {
		var promise = registerInternal(requestJson);

		promise.waitUntilFinished();

		return promise.getId();
	}

	public static UUID registerNowait(String requestJson) throws JsonProcessingException {
		var promise = registerInternal(requestJson);
		return promise.getId();
	}

	private static JournalPromise registerInternal(String requestJson) throws JsonProcessingException {
		var request = parse(requestJson, JournalRegisterRequest.class);
		var executor = JournalExecutorMap.get(request.group_id);

		return executor.registerJournal(SecurityValues.currentUserId(), request);
	}

	public static void registerLazily(String requestJson) throws JsonProcessingException {
		var request = parse(requestJson, JournalRegisterRequest.class);
		var executor = JournalExecutorMap.get(request.group_id);

		executor.registerJournalLazily(SecurityValues.currentUserId(), request);
	}

	public static void deny(String requestJson) throws JsonProcessingException, JournalNotFoundException {
		var request = parse(requestJson, JournalDenyRequest.class);

		var groupId = U.recorder.play(() -> new journals().SELECT(a -> a.group_id)).fetch(request.deny_id).orElseThrow(() -> new JournalNotFoundException()).getGroup_id();

		var executor = JournalExecutorMap.get(groupId);

		executor.denyJournal(SecurityValues.currentUserId(), request);
	}

	public static void overwrite(String requestJson) throws JsonProcessingException {
		var request = parse(requestJson, JournalOverwriteRequest.class);
		var executor = JournalExecutorMap.get(request.group_id);

		executor.overwriteJournal(SecurityValues.currentUserId(), request);
	}

	public static PausingGroup[] pauseGroups(String requestJson) throws JsonProcessingException, CommandFailedException, InterruptedException {
		var request = parse(requestJson, GroupPauseRequest.class);
		var executor = JournalExecutorMap.get(request.group_id);

		return executor.pauseGroups(SecurityValues.currentUserId(), request);
	}

	public static PausingGroup[] resumeGroups(String requestJson) throws JsonProcessingException, CommandFailedException, InterruptedException {
		var request = parse(requestJson, GroupProcessRequest.class);
		var executor = JournalExecutorMap.get(request.group_id);

		return executor.resumeGroups(SecurityValues.currentUserId(), request);
	}

	public static PausingGroup[] getPausingGroups(UUID userId, String requestJson) throws JsonProcessingException, CommandFailedException, InterruptedException {
		var request = parse(requestJson, GroupProcessRequest.class);
		var executor = JournalExecutorMap.get(request.group_id);

		return executor.getPausingGroups(SecurityValues.currentUserId(), request);
	}

	public static void close(String requestJson) throws JsonProcessingException {
		var request = parse(requestJson, ClosingRequest.class);
		var executor = JournalExecutorMap.get(request.group_id);

		executor.close(SecurityValues.currentUserId(), request);
	}

	public static void moveTransient(String requestJson) throws JsonProcessingException, JournalNotFoundException {
		var request = parse(requestJson, TransientMoveRequest.class);

		var groupId = U.recorder.play(() -> new transients().SELECT(a -> a.group_id)).fetch(request.transient_id).orElseThrow(() -> new JournalNotFoundException()).getGroup_id();

		var executor = JournalExecutorMap.get(groupId);

		executor.moveTransient(SecurityValues.currentUserId(), request);
	}

	private static <T> T parse(String json, Class<T> objectClass) throws JsonProcessingException {
		try {
			return U.fromJson(json, objectClass);
		} catch (JsonSyntaxException e) {
			throw new JsonProcessingException(e);
		}
	}

	@SuppressWarnings("serial")
	public static class JournalNotFoundException extends Exception {
	}

	@SuppressWarnings("serial")
	public static class JsonProcessingException extends Exception {

		private JsonProcessingException(JsonSyntaxException e) {
			super(e);

		}
	}
}
