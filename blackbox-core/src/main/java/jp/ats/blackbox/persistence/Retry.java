package jp.ats.blackbox.persistence;

@SuppressWarnings("serial")
public class Retry extends RuntimeException {//エラーではないため、BlackBoxExceptionのサブクラスとしない

	Retry(Throwable t) {
		super(t);
	}
}
