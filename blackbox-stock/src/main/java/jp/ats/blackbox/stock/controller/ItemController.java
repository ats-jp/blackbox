package jp.ats.blackbox.stock.controller;

import java.util.UUID;

import jp.ats.blackbox.common.PrivilegeManager;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.controller.ControllerUtils;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.core.persistence.Privilege;
import jp.ats.blackbox.core.persistence.SecurityValues;
import jp.ats.blackbox.stock.persistence.ItemHandler;
import jp.ats.blackbox.stock.persistence.ItemHandler.ItemRegisterRequest;
import jp.ats.blackbox.stock.persistence.ItemHandler.ItemUpdateRequest;
import jp.ats.blackbox.stock.persistence.ItemHandler.SkuRegisterRequest;
import jp.ats.blackbox.stock.persistence.ItemHandler.SkuUpdateRequest;
import sqlassist.bb_stock.items;
import sqlassist.bb_stock.skus;

public class ItemController {

	public static UUID register(ItemRegisterRequest request) throws PrivilegeException {
		return ControllerUtils.register(request.group_id, userId -> ItemHandler.register(request, userId));
	}

	public static void update(ItemUpdateRequest request) throws PrivilegeException {
		ControllerUtils.update(request.group_id, items.$TABLE, request.id, userId -> ItemHandler.update(request, userId));
	}

	public static void deleteItem(UUID itemId, long revision) throws PrivilegeException, AlreadyUsedException {
		ControllerUtils.delete(items.$TABLE, itemId, () -> ItemHandler.deleteItem(itemId, revision));
	}

	public static UUID register(SkuRegisterRequest request) throws PrivilegeException {
		var userId = SecurityValues.currentUserId();

		var groupId = U.recorder.play(
			() -> new items().SELECT(a -> a.group_id).WHERE(a -> a.active.eq(true))).fetch(request.item_id).get().getGroup_id();

		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.GROUP).success) throw new PrivilegeException();

		return ItemHandler.register(request, userId);
	}

	public static void update(SkuUpdateRequest request) throws PrivilegeException {
		var userId = SecurityValues.currentUserId();

		var groupId = U.recorder.play(
			() -> new skus().SELECT(a -> a.$items().group_id).WHERE(a -> a.active.eq(true).AND.$items().active.eq(true))).fetch(request.id).get().$items().getGroup_id();

		if (!PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.GROUP).success) throw new PrivilegeException();

		ItemHandler.update(request, userId);
	}

	public static void deleteSku(UUID itemId, long revision) throws AlreadyUsedException {
		ItemHandler.deleteItem(itemId, revision);
	}
}
