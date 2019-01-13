package jp.ats.blackbox.executor;

@SuppressWarnings("serial")
public class TransferFailedException extends Exception {

	TransferFailedException(Throwable throwable) {
		super(throwable);
	}
}
