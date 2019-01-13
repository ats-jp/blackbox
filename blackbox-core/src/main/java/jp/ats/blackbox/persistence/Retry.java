package jp.ats.blackbox.persistence;

@SuppressWarnings("serial")
public class Retry extends BlackboxPersistenceException {

	Retry(Throwable t) {
		super(t);
	}
}
