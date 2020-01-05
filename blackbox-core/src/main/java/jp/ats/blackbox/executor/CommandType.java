package jp.ats.blackbox.executor;

public enum CommandType {

	JOURNAL_REGISTER(Constant.JOURNAL_REGISTER),

	JOURNAL_DENY(Constant.JOURNAL_DENY),

	OVERWRITE(Constant.JOURNAL_DENY),

	CLOSING(Constant.CLOSING),

	TRANSIENT_MOVE(Constant.TRANSIENT_MOVE);

	private static class Constant {

		private static final String JOURNAL_REGISTER = "R";

		private static final String JOURNAL_DENY = "D";

		private static final String OVERWRITE = "O";

		private static final String CLOSING = "C";

		private static final String TRANSIENT_MOVE = "T";
	}

	public static CommandType of(String value) {
		switch (value) {
		case Constant.JOURNAL_REGISTER:
			return JOURNAL_REGISTER;
		case Constant.JOURNAL_DENY:
			return JOURNAL_DENY;
		case Constant.OVERWRITE:
			return OVERWRITE;
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
