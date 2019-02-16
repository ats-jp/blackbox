package jp.ats.blackbox.executor;

public enum CommandType {

	TRANSFER_REGISTER(Constant.TRANSFER_REGISTER),

	TRANSFER_DENY(Constant.TRANSFER_DENY),

	CLOSING(Constant.CLOSING),

	TRANSIENT_MOVE(Constant.TRANSIENT_MOVE);

	private static class Constant {

		private static final String TRANSFER_REGISTER = "R";

		private static final String TRANSFER_DENY = "D";

		private static final String CLOSING = "C";

		private static final String TRANSIENT_MOVE = "T";
	}

	public static CommandType of(String value) {
		switch (value) {
		case Constant.TRANSFER_REGISTER:
			return TRANSFER_REGISTER;
		case Constant.TRANSFER_DENY:
			return TRANSFER_DENY;
		case Constant.CLOSING:
			return CLOSING;
		case Constant.TRANSIENT_MOVE:
			return TRANSIENT_MOVE;
		default:
			throw new Error();
		}
	}

	public final String value;

	private CommandType(String value) {
		this.value = value;
	}
}
