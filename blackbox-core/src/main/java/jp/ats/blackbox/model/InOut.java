package jp.ats.blackbox.model;

public enum InOut {

	IN('I'),

	OUT('O');

	public final char value;

	private InOut(char value) {
		this.value = value;
	}
}
