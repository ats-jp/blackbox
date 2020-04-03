package jp.ats.blackbox.persistence;

public enum Role {

	SYSTEM_ADMIN(0),

	ORG_ADMIN(1),

	GROUP_ADMIN(2),

	USER(3),

	NONE(9);

	private final int value;

	private Role(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
