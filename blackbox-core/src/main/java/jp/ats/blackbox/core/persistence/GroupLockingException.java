package jp.ats.blackbox.core.persistence;

import java.util.UUID;

import jp.ats.blackbox.common.BlackboxException;

@SuppressWarnings("serial")
public class GroupLockingException extends BlackboxException {

	GroupLockingException(UUID groupId) {
		super("グループ: " + groupId + " はロックされています");
	}
}
