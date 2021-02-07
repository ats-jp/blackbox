package jp.ats.blackbox.core.executor;

@SuppressWarnings("serial")
public class CommandFailedException extends Exception {

	CommandFailedException(Throwable throwable) {
		super(throwable);
	}
}
