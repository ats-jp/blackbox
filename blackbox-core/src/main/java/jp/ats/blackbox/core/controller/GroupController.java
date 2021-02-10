package jp.ats.blackbox.core.controller;

import java.util.UUID;

import org.blendee.jdbc.BlendeeManager;

import jp.ats.blackbox.common.PrivilegeManager;
import jp.ats.blackbox.common.PrivilegeManager.PrivilegeResult;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.core.persistence.GroupHandler;
import jp.ats.blackbox.core.persistence.GroupHandler.RegisterRequest;
import jp.ats.blackbox.core.persistence.GroupHandler.UpdateRequest;
import jp.ats.blackbox.core.persistence.Privilege;
import jp.ats.blackbox.core.persistence.SecurityValues;

public class GroupController {

	public static UUID register(RegisterRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.parent_id);

		UUID result;

		executor.writeLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.parent_id, Privilege.GROUP).success) throw new PrivilegeException();

			result = GroupHandler.register(request, userId);
		} finally {
			executor.writeUnlock();
		}

		BlendeeManager.get().getCurrentTransaction().commit();
		JournalExecutorMap.reloadOrg(request.org_id);

		return result;
	}

	public static void update(UpdateRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.id);
		executor.writeLock();

		PrivilegeResult result;
		try {
			var userId = SecurityValues.currentUserId();

			result = PrivilegeManager.hasPrivilegeOfGroup(userId, request.id, Privilege.GROUP);

			if (!result.success) throw new PrivilegeException();

			GroupHandler.update(request, userId);
		} finally {
			executor.writeUnlock();
		}

		BlendeeManager.get().getCurrentTransaction().commit();

		JournalExecutorMap.reloadOrg(result.orgId);
	}

	public static void delete(UUID groupId, long revision) throws PrivilegeException, AlreadyUsedException {
		var executor = JournalExecutorMap.get(groupId);
		executor.writeLock();

		PrivilegeResult result;
		try {
			var userId = SecurityValues.currentUserId();

			result = PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.GROUP);

			if (!result.success) throw new PrivilegeException();

			GroupHandler.delete(groupId, revision);
		} finally {
			executor.writeUnlock();
		}

		BlendeeManager.get().getCurrentTransaction().commit();

		JournalExecutorMap.reloadOrg(result.orgId);
	}
}
