package jp.ats.blackbox.persistence;

import java.util.UUID;

@SuppressWarnings("serial")
public class CycleGroupException extends BlackboxPersistenceException {

	CycleGroupException(UUID groupId) {
		super(groupId + " に循環があります");
	}
}
