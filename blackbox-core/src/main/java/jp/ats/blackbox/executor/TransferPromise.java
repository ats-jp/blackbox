package jp.ats.blackbox.executor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransferPromise {

	private static final long undecidedId = -1L;

	private final ReentrantLock lock = new ReentrantLock();

	private final Condition condition = lock.newCondition();

	private long id = undecidedId;

	public long getTransferId() throws InterruptedException {
		lock.lock();
		try {
			if (id != undecidedId) return id;

			condition.await();
			return id;
		} finally {
			lock.unlock();
		}
	}

	void setTransferId(long id) {
		lock.lock();
		try {
			this.id = id;
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
}
