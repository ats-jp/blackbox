package jp.ats.blackbox.backend.controller;

import jp.ats.blackbox.backend.api.JsonParser.JsonProcessingException;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.CommandFailedException;
import jp.ats.blackbox.executor.JournalExecutor.PausingGroup;
import jp.ats.blackbox.executor.JournalExecutorMap;
import jp.ats.blackbox.executor.JournalPromise;
import jp.ats.blackbox.executor.OverwritePromise;
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

	public static JournalPromise register(JournalRegisterRequest request) {
		return JournalExecutorMap.get(request.group_id).registerJournal(SecurityValues.currentUserId(), request);
	}

	public static JournalPromise registerLazily(JournalRegisterRequest request) {
		return JournalExecutorMap.get(request.group_id).registerJournalLazily(SecurityValues.currentUserId(), request);
	}

	public static JournalPromise deny(JournalDenyRequest request) throws JournalNotFoundException {
		var groupId = U.recorder.play(() -> new journals().SELECT(a -> a.group_id)).fetch(request.deny_id).orElseThrow(() -> new JournalNotFoundException()).getGroup_id();

		return JournalExecutorMap.get(groupId).denyJournal(SecurityValues.currentUserId(), request);
	}

	public static OverwritePromise overwrite(JournalOverwriteRequest request) {
		return JournalExecutorMap.get(request.group_id).overwriteJournal(SecurityValues.currentUserId(), request);
	}

	public static PausingGroup[] pauseGroups(GroupPauseRequest request) throws CommandFailedException, InterruptedException {
		return JournalExecutorMap.get(request.group_id).pauseGroups(SecurityValues.currentUserId(), request);
	}

	public static PausingGroup[] resumeGroups(GroupProcessRequest request) throws CommandFailedException, InterruptedException {
		return JournalExecutorMap.get(request.group_id).resumeGroups(SecurityValues.currentUserId(), request);
	}

	public static PausingGroup[] getPausingGroups(GroupProcessRequest request) throws CommandFailedException, InterruptedException {
		return JournalExecutorMap.get(request.group_id).getPausingGroups(SecurityValues.currentUserId(), request);
	}

	public static JournalPromise close(ClosingRequest request) {
		return JournalExecutorMap.get(request.group_id).close(SecurityValues.currentUserId(), request);
	}

	public static JournalPromise moveTransient(TransientMoveRequest request) throws JsonProcessingException, JournalNotFoundException {
		var groupId = U.recorder.play(() -> new transients().SELECT(a -> a.group_id)).fetch(request.transient_id).orElseThrow(() -> new JournalNotFoundException()).getGroup_id();

		return JournalExecutorMap.get(groupId).moveTransient(SecurityValues.currentUserId(), request);
	}

	@SuppressWarnings("serial")
	public static class JournalNotFoundException extends Exception {
	}
}
