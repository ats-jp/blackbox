package jp.ats.blackbox.core.controller;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jp.ats.blackbox.common.BlackboxException;
import jp.ats.blackbox.common.PrivilegeManager;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.executor.CommandFailedException;
import jp.ats.blackbox.core.executor.JournalExecutor.PausingGroup;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.executor.JournalPromise;
import jp.ats.blackbox.core.executor.OverwritePromise;
import jp.ats.blackbox.core.persistence.Privilege;
import jp.ats.blackbox.core.persistence.Requests.ClosingRequest;
import jp.ats.blackbox.core.persistence.Requests.GroupPauseRequest;
import jp.ats.blackbox.core.persistence.Requests.GroupProcessRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalDenyRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalOverwriteRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalRegisterRequest;
import jp.ats.blackbox.core.persistence.Requests.TransientMoveRequest;
import jp.ats.blackbox.core.persistence.SecurityValues;
import sqlassist.bb.journals;
import sqlassist.bb.transients;

public class JournalController {

	private static long checkPrivilegeForRegister(UUID userId, JournalRegisterRequest request) throws PrivilegeException {
		var result = PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id, Privilege.USER);
		if (!result.success) throw new PrivilegeException();

		var unitIds = Arrays.stream(request.details).flatMap(d -> Arrays.stream(d.nodes).map(n -> n.unit_id)).collect(Collectors.toSet());
		if (!PrivilegeManager.hasPrivilegeOfUnits(request.group_id, unitIds)) throw new PrivilegeException();

		return result.groupTreeRevision;
	}

	@FunctionalInterface
	public interface JournalRegisterRequestSupplier {

		JournalRegisterRequest get() throws PrivilegeException;
	}

	public static JournalPromise register(JournalRegisterRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.group_id);

		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			request.group_tree_revision = Optional.of(checkPrivilegeForRegister(userId, request));

			return executor.registerJournal(userId, request);
		} finally {
			executor.readUnlock();
		}
	}

	public static JournalPromise register(UUID journalGroupId, JournalRegisterRequestSupplier supplier) throws PrivilegeException {
		var executor = JournalExecutorMap.get(journalGroupId);

		executor.readLock();
		try {
			//ロック中に実行することでSupplier内部でGroupの検査実施が可能
			var request = supplier.get();

			var userId = SecurityValues.currentUserId();

			request.group_tree_revision = Optional.of(checkPrivilegeForRegister(userId, request));

			return executor.registerJournal(userId, request);
		} finally {
			executor.readUnlock();
		}
	}

	public static JournalPromise registerLazily(JournalRegisterRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.group_id);

		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			request.group_tree_revision = Optional.of(checkPrivilegeForRegister(userId, request));

			return executor.registerJournalLazily(userId, request);
		} finally {
			executor.readUnlock();
		}
	}

	public static JournalPromise registerLazily(UUID journalGroupId, JournalRegisterRequestSupplier supplier) throws PrivilegeException {
		var executor = JournalExecutorMap.get(journalGroupId);

		executor.readLock();
		try {
			//ロック中に実行することでSupplier内部でGroupの検査実施が可能
			var request = supplier.get();

			var userId = SecurityValues.currentUserId();

			request.group_tree_revision = Optional.of(checkPrivilegeForRegister(userId, request));

			return executor.registerJournalLazily(userId, request);
		} finally {
			executor.readUnlock();
		}
	}

	public static JournalPromise deny(JournalDenyRequest request) throws JournalNotFoundException, PrivilegeException {
		var groupId = U.recorder.play(
			() -> new journals()
				.SELECT(a -> a.group_id))
			.fetch(request.deny_id)
			.orElseThrow(() -> new JournalNotFoundException())
			.getGroup_id();

		var executor = JournalExecutorMap.get(groupId);

		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			var result = PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.USER);

			if (!result.success) throw new PrivilegeException();

			request.group_tree_revision = Optional.of(result.groupTreeRevision);

			return executor.denyJournal(userId, request);
		} finally {
			executor.readUnlock();
		}
	}

	public static OverwritePromise overwrite(JournalOverwriteRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.group_id);

		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			var result = PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id, Privilege.USER);

			if (!result.success) throw new PrivilegeException();

			if (!PrivilegeManager.hasPrivilegeOfUnit(request.group_id, request.unit_id)) throw new PrivilegeException();

			request.group_tree_revision = Optional.of(result.groupTreeRevision);

			return executor.overwriteJournal(userId, request, r -> {
				if (!PrivilegeManager.hasPrivilegeOfGroup(userId, r.group_id, Privilege.USER).success) throw new OverwriteDenyFailedException(r.denied_id.get());
			});
		} finally {
			executor.readUnlock();
		}
	}

	public static PausingGroup[] pauseGroups(GroupPauseRequest request) throws CommandFailedException, PrivilegeException, InterruptedException {
		var executor = JournalExecutorMap.get(request.group_id);

		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();
			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id, Privilege.USER).success) throw new PrivilegeException();

			return executor.pauseGroups(userId, request);
		} finally {
			executor.readUnlock();
		}
	}

	public static PausingGroup[] resumeGroups(GroupProcessRequest request) throws CommandFailedException, PrivilegeException, InterruptedException {
		var executor = JournalExecutorMap.get(request.group_id);

		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();
			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id, Privilege.USER).success) throw new PrivilegeException();

			return executor.resumeGroups(userId, request);
		} finally {
			executor.readUnlock();
		}
	}

	public static PausingGroup[] getPausingGroups(GroupProcessRequest request) throws CommandFailedException, InterruptedException {
		var executor = JournalExecutorMap.get(request.group_id);

		executor.readLock();
		try {
			return executor.getPausingGroups(SecurityValues.currentUserId(), request);
		} finally {
			executor.readUnlock();
		}
	}

	public static JournalPromise close(ClosingRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.group_id);

		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			var result = PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id, Privilege.USER);

			if (!result.success) throw new PrivilegeException();

			request.group_tree_revision = Optional.of(result.groupTreeRevision);

			return executor.close(userId, request);
		} finally {
		}
	}

	public static JournalPromise moveTransient(TransientMoveRequest request) throws JournalNotFoundException, PrivilegeException {
		var groupId = U.recorder.play(() -> new transients().SELECT(a -> a.group_id)).fetch(request.transient_id).orElseThrow(() -> new JournalNotFoundException()).getGroup_id();

		var executor = JournalExecutorMap.get(groupId);

		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			var result = PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.USER);

			if (!result.success) throw new PrivilegeException();

			request.group_tree_revision = Optional.of(result.groupTreeRevision);

			return executor.moveTransient(userId, request);
		} finally {
			executor.readUnlock();
		}
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
