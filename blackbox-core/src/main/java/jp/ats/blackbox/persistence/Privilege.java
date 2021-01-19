package jp.ats.blackbox.persistence;

public enum Privilege {

	SYSTEM(0),

	ORG(1),

	GROUP(2),

	USER(3),

	NONE(9);

	private final int value;

	private Privilege(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
