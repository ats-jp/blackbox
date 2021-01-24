package jp.ats.blackbox.controller;

import java.util.UUID;

import jp.ats.blackbox.executor.JournalExecutorMap;
import jp.ats.blackbox.persistence.GroupHandler;
import jp.ats.blackbox.persistence.GroupHandler.RegisterRequest;
import jp.ats.blackbox.persistence.GroupHandler.UpdateRequest;

public class GroupController {

	public static UUID register(RegisterRequest request, UUID userId) {
		var executor = JournalExecutorMap.get(request.parent_id);
		executor.lock();
		try {
			return GroupHandler.register(request, userId);
		} finally {
			executor.unlock();
		}
	}

	public static void update(UpdateRequest request, UUID userId) {
		var executor = JournalExecutorMap.get(request.id);
		executor.lock();
		try {
			GroupHandler.update(request, userId);
		} finally {
			executor.unlock();
		}
	}
}
