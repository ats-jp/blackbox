package jp.ats.blackbox.persistence;

@SuppressWarnings("serial")
public class BlackboxPersistenceException extends RuntimeException {

	public BlackboxPersistenceException(Throwable t) {
		super(t);
	}

	public BlackboxPersistenceException() {
		super();
	}

	public BlackboxPersistenceException(String message) {
		super(message);
	}

	public BlackboxPersistenceException(String message, Throwable t) {
		super(message, t);
	}
}
