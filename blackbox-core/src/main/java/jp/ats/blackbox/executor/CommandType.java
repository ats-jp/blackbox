package jp.ats.blackbox.executor;

public enum CommandType {

	TRANSFER_REGISTER,

	TRANSFER_DENY,

	CLOSING;

	public CommandType getInstance(int type) {
		return CommandType.values()[type];
	}
}
