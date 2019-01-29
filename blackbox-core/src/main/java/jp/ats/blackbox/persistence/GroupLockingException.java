package jp.ats.blackbox.persistence;

import java.util.UUID;

@SuppressWarnings("serial")
public class GroupLockingException extends BlackboxPersistenceException {

	GroupLockingException(UUID groupId) {
		super("グループ: " + groupId + " はロックされています");
	}
}
