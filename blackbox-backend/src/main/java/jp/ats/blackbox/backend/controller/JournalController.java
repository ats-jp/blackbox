package jp.ats.blackbox.backend.controller;

import java.util.UUID;

import jp.ats.blackbox.common.BlackboxException;
import jp.ats.blackbox.common.PrivilegeManager;
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

	public static JournalPromise register(JournalRegisterRequest request) throws PrivilegeException {
		var userId = SecurityValues.currentUserId();
		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id)) throw new PrivilegeException();

		return JournalExecutorMap.get(request.group_id).registerJournal(userId, request);
	}

	public static JournalPromise registerLazily(JournalRegisterRequest request) throws PrivilegeException {
		var userId = SecurityValues.currentUserId();
		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id)) throw new PrivilegeException();

		return JournalExecutorMap.get(request.group_id).registerJournalLazily(userId, request);
	}

	public static JournalPromise deny(JournalDenyRequest request) throws JournalNotFoundException, PrivilegeException {
		var groupId = U.recorder.play(
			() -> new journals()
				.SELECT(a -> a.group_id))
			.fetch(request.deny_id)
			.orElseThrow(() -> new JournalNotFoundException())
			.getGroup_id();

		var userId = SecurityValues.currentUserId();
		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, groupId)) throw new PrivilegeException();

		return JournalExecutorMap.get(groupId).denyJournal(userId, request);
	}

	public static OverwritePromise overwrite(JournalOverwriteRequest request) throws PrivilegeException {
		var userId = SecurityValues.currentUserId();
		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id)) throw new PrivilegeException();

		return JournalExecutorMap.get(request.group_id).overwriteJournal(userId, request, r -> {
			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, r.group_id)) throw new OverwriteDenyFailedException(r.denied_id.get());
		});
	}

	public static PausingGroup[] pauseGroups(GroupPauseRequest request) throws CommandFailedException, PrivilegeException, InterruptedException {
		var userId = SecurityValues.currentUserId();
		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id)) throw new PrivilegeException();

		return JournalExecutorMap.get(request.group_id).pauseGroups(userId, request);
	}

	public static PausingGroup[] resumeGroups(GroupProcessRequest request) throws CommandFailedException, PrivilegeException, InterruptedException {
		var userId = SecurityValues.currentUserId();
		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id)) throw new PrivilegeException();

		return JournalExecutorMap.get(request.group_id).resumeGroups(userId, request);
	}

	public static PausingGroup[] getPausingGroups(GroupProcessRequest request) throws CommandFailedException, InterruptedException {
		return JournalExecutorMap.get(request.group_id).getPausingGroups(SecurityValues.currentUserId(), request);
	}

	public static JournalPromise close(ClosingRequest request) throws PrivilegeException {
		var userId = SecurityValues.currentUserId();
		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id)) throw new PrivilegeException();

		return JournalExecutorMap.get(request.group_id).close(userId, request);
	}

	public static JournalPromise moveTransient(TransientMoveRequest request) throws JournalNotFoundException, PrivilegeException {
		var groupId = U.recorder.play(() -> new transients().SELECT(a -> a.group_id)).fetch(request.transient_id).orElseThrow(() -> new JournalNotFoundException()).getGroup_id();

		var userId = SecurityValues.currentUserId();
		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, groupId)) throw new PrivilegeException();

		return JournalExecutorMap.get(groupId).moveTransient(userId, request);
	}

	@SuppressWarnings("serial")
	public static class JournalNotFoundException extends Exception {
	}

	@SuppressWarnings("serial")
	public static class PrivilegeException extends Exception {
	}

	//overwriteで、未来のjournalをdenyしなければならない時にそのjournalのgroupの権限を持たないときに発生
	//journal_errorsに記録される
	@SuppressWarnings("serial")
	public static class OverwriteDenyFailedException extends BlackboxException {

		private OverwriteDenyFailedException(UUID journalId) {
			super("journal_id: " + journalId.toString());
		}
	}
}
