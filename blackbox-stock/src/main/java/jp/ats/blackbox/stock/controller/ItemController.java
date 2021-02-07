package jp.ats.blackbox.stock.controller;

import static org.blendee.sql.Placeholder.$UUID;

import java.util.UUID;

import jp.ats.blackbox.common.PrivilegeManager;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.Privilege;
import jp.ats.blackbox.core.persistence.SecurityValues;
import jp.ats.blackbox.stock.persistence.ItemHandler;
import jp.ats.blackbox.stock.persistence.ItemHandler.ItemRegisterRequest;
import jp.ats.blackbox.stock.persistence.ItemHandler.ItemUpdateRequest;
import sqlassist.bb_stock.items;

public class ItemController {

	public static UUID register(ItemRegisterRequest request) throws PrivilegeException {
		var executor = JournalExecutorMap.get(request.group_id);
		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, request.group_id, Privilege.GROUP).success) throw new PrivilegeException();

			return ItemHandler.registerItem(request, userId);
		} finally {
			executor.readUnlock();
		}
	}

	public static void update(ItemUpdateRequest request) throws PrivilegeException {
		var groupId = request.group_id.orElseGet(
			() -> U.recorder.play(
				() -> new items()
					.SELECT(a -> a.group_id)
					.WHERE(a -> a.active.eq(true).AND.id.eq($UUID)),
				request.id).willUnique().get().getGroup_id());

		var executor = JournalExecutorMap.get(groupId);
		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.GROUP).success) throw new PrivilegeException();

			ItemHandler.updateItem(request, userId);
		} finally {
			executor.readUnlock();
		}
	}
}
