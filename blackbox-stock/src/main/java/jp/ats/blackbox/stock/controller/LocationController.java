package jp.ats.blackbox.stock.controller;

import java.util.UUID;

import jp.ats.blackbox.core.controller.ControllerUtils;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.stock.persistence.LocationHandler;
import jp.ats.blackbox.stock.persistence.LocationHandler.RegisterRequest;
import jp.ats.blackbox.stock.persistence.LocationHandler.UpdateRequest;
import sqlassist.bb_stock.locations;

public class LocationController {

	public static UUID register(RegisterRequest request) throws PrivilegeException {
		return ControllerUtils.register(request.group_id, userId -> LocationHandler.register(request, userId));
	}

	public static void update(UpdateRequest request) throws PrivilegeException {
		ControllerUtils.update(request.group_id, locations.$TABLE, request.id, userId -> LocationHandler.update(request, userId));
	}

	public static void delete(UUID ownerId, long revision) throws PrivilegeException, AlreadyUsedException {
		ControllerUtils.delete(locations.$TABLE, ownerId, () -> LocationHandler.delete(ownerId, revision));
	}
}
