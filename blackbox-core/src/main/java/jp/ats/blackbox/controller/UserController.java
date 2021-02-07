package jp.ats.blackbox.controller;

import java.util.UUID;

import jp.ats.blackbox.executor.JournalExecutorMap;
import jp.ats.blackbox.persistence.UserHandler;
import jp.ats.blackbox.persistence.UserHandler.RegisterRequest;
import jp.ats.blackbox.persistence.UserHandler.UpdateRequest;

public class UserController {

	public static UUID register(RegisterRequest request, UUID userId) {
		var executor = JournalExecutorMap.get(request.group_id);
		executor.readLock();
		try {
			return UserHandler.register(request, userId);
		} finally {
			executor.readUnlock();
		}
	}

	public static void update(UpdateRequest request, UUID userId) {
		var executor = JournalExecutorMap.get(request.id);
		executor.readLock();
		try {
			UserHandler.update(request, userId);
		} finally {
			executor.readUnlock();
		}
	}
}
