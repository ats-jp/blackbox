package jp.ats.blackbox.stock.controller;

import java.util.UUID;

import jp.ats.blackbox.core.controller.ControllerUtils;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.stock.persistence.OwnerHandler;
import jp.ats.blackbox.stock.persistence.OwnerHandler.RegisterRequest;
import jp.ats.blackbox.stock.persistence.OwnerHandler.UpdateRequest;
import sqlassist.bb_stock.owners;

public class OwnerController {

	public static UUID register(RegisterRequest request) throws PrivilegeException {
		return ControllerUtils.register(request.group_id, userId -> OwnerHandler.register(request, userId));
	}

	public static void update(UpdateRequest request) throws PrivilegeException {
		ControllerUtils.update(request.group_id, owners.$TABLE, request.id, userId -> OwnerHandler.update(request, userId));
	}

	public static void delete(UUID ownerId, long revision) throws PrivilegeException, AlreadyUsedException {
		ControllerUtils.delete(owners.$TABLE, ownerId, () -> OwnerHandler.delete(ownerId, revision));
	}
}
