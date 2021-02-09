package jp.ats.blackbox.stock.controller;

import java.util.UUID;

import jp.ats.blackbox.core.controller.ControllerUtils;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.stock.persistence.StatusHandler;
import jp.ats.blackbox.stock.persistence.StatusHandler.RegisterRequest;
import jp.ats.blackbox.stock.persistence.StatusHandler.UpdateRequest;
import sqlassist.bb_stock.statuses;

public class StatusController {

	public static UUID register(RegisterRequest request) throws PrivilegeException {
		return ControllerUtils.register(request.group_id, userId -> StatusHandler.register(request, userId));
	}

	public static void update(UpdateRequest request) throws PrivilegeException {
		ControllerUtils.update(request.group_id, statuses.$TABLE, request.id, userId -> StatusHandler.update(request, userId));
	}

	public static void delete(UUID ownerId, long revision) throws PrivilegeException, AlreadyUsedException {
		ControllerUtils.delete(statuses.$TABLE, ownerId, () -> StatusHandler.delete(ownerId, revision));
	}
}
