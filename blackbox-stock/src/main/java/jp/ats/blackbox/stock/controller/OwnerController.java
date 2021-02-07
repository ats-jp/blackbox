package jp.ats.blackbox.stock.controller;

import static org.blendee.sql.Placeholder.$UUID;

import java.util.UUID;

import jp.ats.blackbox.common.PrivilegeManager;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.Privilege;
import jp.ats.blackbox.core.persistence.SecurityValues;
import jp.ats.blackbox.stock.persistence.OwnerHandler;
import jp.ats.blackbox.stock.persistence.OwnerHandler.RegisterRequest;
import jp.ats.blackbox.stock.persistence.OwnerHandler.UpdateRequest;
import sqlassist.bb_stock.owners;

public class OwnerController {

	public static UUID register(RegisterRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.group_id);
		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id, Privilege.GROUP).success) throw new PrivilegeException();

			return OwnerHandler.register(request, userId);
		} finally {
			executor.readUnlock();
		}
	}

	public static void update(UpdateRequest request) throws PrivilegeException {
		var groupId = request.group_id.orElseGet(
			() -> U.recorder.play(
				() -> new owners()
					.SELECT(a -> a.group_id)
					.WHERE(a -> a.active.eq(true).AND.id.eq($UUID)),
				request.id).willUnique().get().getGroup_id());

		var executor = JournalExecutorMap.get(groupId);
		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.GROUP).success) throw new PrivilegeException();

			OwnerHandler.update(request, userId);
		} finally {
			executor.readUnlock();
		}
	}
}
