package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.PrivilegeValues.GROUP_VALUE;
import static jp.ats.blackbox.persistence.PrivilegeValues.NONE_VALUE;
import static jp.ats.blackbox.persistence.PrivilegeValues.ORG_VALUE;
import static jp.ats.blackbox.persistence.PrivilegeValues.SYSTEM_VALUE;
import static jp.ats.blackbox.persistence.PrivilegeValues.USER_VALUE;

public enum Privilege {

	SYSTEM(SYSTEM_VALUE),

	ORG(ORG_VALUE),

	GROUP(GROUP_VALUE),

	USER(USER_VALUE),

	NONE(NONE_VALUE);

	public final int value;

	private Privilege(int value) {
		this.value = value;
	}

	public static Privilege of(int value) {
		switch (value) {
		case SYSTEM_VALUE:
			return SYSTEM;
		case ORG_VALUE:
			return ORG;
		case GROUP_VALUE:
			return GROUP;
		case USER_VALUE:
			return USER;
		case NONE_VALUE:
			return NONE;
		default:
			throw new IllegalStateException(String.valueOf(value));
		}
	}
}
