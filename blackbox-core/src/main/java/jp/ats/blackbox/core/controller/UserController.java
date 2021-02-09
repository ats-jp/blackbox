package jp.ats.blackbox.core.controller;

import java.util.UUID;

import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.core.persistence.UserHandler;
import jp.ats.blackbox.core.persistence.UserHandler.RegisterRequest;
import jp.ats.blackbox.core.persistence.UserHandler.UpdateRequest;
import sqlassist.bb.users;

public class UserController {

	public static UUID register(RegisterRequest request) throws PrivilegeException {
		return ControllerUtils.register(request.group_id, userId -> UserHandler.register(request, userId));
	}

	public static void update(UpdateRequest request) throws PrivilegeException {
		ControllerUtils.update(request.group_id, users.$TABLE, request.id, userId -> UserHandler.update(request, userId));
	}

	public static void delete(UUID subjectUserId, long revision) throws PrivilegeException, AlreadyUsedException {
		ControllerUtils.delete(users.$TABLE, subjectUserId, () -> UserHandler.delete(subjectUserId, revision));
	}
}
