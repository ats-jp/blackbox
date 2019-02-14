package jp.ats.blackbox.common;

@SuppressWarnings("serial")
public class BlackboxException extends RuntimeException {

	public BlackboxException(Throwable t) {
		super(t);
	}

	public BlackboxException() {
		super();
	}

	public BlackboxException(String message) {
		super(message);
	}

	public BlackboxException(String message, Throwable t) {
		super(message, t);
	}
}
