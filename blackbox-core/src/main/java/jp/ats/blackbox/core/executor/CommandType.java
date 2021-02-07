package jp.ats.blackbox.core.executor;

public enum CommandType {

	JOURNAL_REGISTER,

	JOURNAL_LAZY_REGISTER,

	JOURNAL_DENY,

	OVERWRITE,

	PAUSE,

	RESUME,

	GET_PAUSING_GROUPS,

	CLOSE,

	TRANSIENT_MOVE;
}
