package jp.ats.blackbox.executor;

@SuppressWarnings("serial")
public class CommandFailedException extends Exception {

	CommandFailedException(Throwable throwable) {
		super(throwable);
	}
}
