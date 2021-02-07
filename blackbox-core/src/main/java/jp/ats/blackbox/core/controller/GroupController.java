package jp.ats.blackbox.core.controller;

import java.util.UUID;

import jp.ats.blackbox.common.PrivilegeManager;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.GroupHandler;
import jp.ats.blackbox.core.persistence.GroupHandler.RegisterRequest;
import jp.ats.blackbox.core.persistence.GroupHandler.UpdateRequest;
import jp.ats.blackbox.core.persistence.Privilege;
import jp.ats.blackbox.core.persistence.SecurityValues;

public class GroupController {

	public static UUID register(RegisterRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.parent_id);
		executor.writeLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.parent_id, Privilege.GROUP).success) throw new PrivilegeException();

			return GroupHandler.register(request, userId);
		} finally {
			executor.writeUnlock();
		}
	}

	public static void update(UpdateRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.id);
		executor.writeLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.id, Privilege.GROUP).success) throw new PrivilegeException();

			GroupHandler.update(request, userId);
		} finally {
			executor.writeUnlock();
		}
	}
}
