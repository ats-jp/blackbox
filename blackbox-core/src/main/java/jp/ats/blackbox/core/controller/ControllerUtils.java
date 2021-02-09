package jp.ats.blackbox.core.controller;

import java.util.Optional;
import java.util.UUID;

import org.blendee.jdbc.TablePath;
import org.blendee.util.GenericTable;

import jp.ats.blackbox.common.PrivilegeManager;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.core.persistence.Privilege;
import jp.ats.blackbox.core.persistence.SecurityValues;

public class ControllerUtils {

	@FunctionalInterface
	public interface RegisterBlock {

		UUID execute(UUID userId);
	}

	@FunctionalInterface
	public interface UpdateBlock {

		void execute(UUID userId);
	}

	@FunctionalInterface
	public interface DeleteBlock {

		void execute() throws AlreadyUsedException;
	}

	public static UUID register(UUID groupId, RegisterBlock block) throws PrivilegeException {
		var executor = JournalExecutorMap.get(groupId);
		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.GROUP).success) throw new PrivilegeException();

			return block.execute(userId);
		} finally {
			executor.readUnlock();
		}
	}

	public static void update(Optional<UUID> optionalGroupId, TablePath table, UUID id, UpdateBlock block) throws PrivilegeException {
		var groupId = optionalGroupId.orElseGet(() -> getGroupId(table, id));

		var executor = JournalExecutorMap.get(groupId);
		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.GROUP).success) throw new PrivilegeException();

			block.execute(userId);
		} finally {
			executor.readUnlock();
		}
	}

	public static void delete(TablePath table, UUID id, DeleteBlock block) throws PrivilegeException, AlreadyUsedException {
		var groupId = getGroupId(table, id);

		var executor = JournalExecutorMap.get(groupId);
		executor.readLock();
		try {
			var userId = SecurityValues.currentUserId();

			if (!PrivilegeManager.hasPrivilegeOfGroup(userId, groupId, Privilege.GROUP).success) throw new PrivilegeException();

			block.execute();
		} finally {
			executor.readUnlock();
		}
	}

	private static UUID getGroupId(TablePath table, UUID id) {
		String groupIdColumnName = "group_id";
		return new GenericTable(table)
			.SELECT(a -> a.col(groupIdColumnName))
			.WHERE(a -> a.col("active").eq(true))
			.fetch(id)
			.get()
			.getUUID(groupIdColumnName);
	}
}
