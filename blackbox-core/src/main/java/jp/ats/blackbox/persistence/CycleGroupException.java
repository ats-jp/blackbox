package jp.ats.blackbox.persistence;

import java.util.UUID;

import jp.ats.blackbox.common.BlackboxException;

@SuppressWarnings("serial")
public class CycleGroupException extends BlackboxException {

	CycleGroupException(UUID groupId) {
		super(groupId + " に循環があります");
	}
}
