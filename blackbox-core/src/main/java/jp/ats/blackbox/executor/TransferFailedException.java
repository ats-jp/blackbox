package jp.ats.blackbox.executor;

public class TransferFailedException extends Exception {

	TransferFailedException(Throwable throwable) {
		super(throwable);
	}
}
