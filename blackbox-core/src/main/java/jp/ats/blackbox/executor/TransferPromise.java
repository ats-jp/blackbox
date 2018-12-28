package jp.ats.blackbox.executor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransferPromise {

	private static final long undecidedId = -1L;

	private final ReentrantLock lock = new ReentrantLock();

	private final Condition condition = lock.newCondition();

	private long id = undecidedId;

	private Throwable error;

	public long getTransferId() throws TransferFailedException, InterruptedException {
		lock.lock();
		try {
			if (id != undecidedId) return id;

			if (error != null) throw new TransferFailedException(error);

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

	void setError(Throwable error) {
		lock.lock();
		try {
			this.error = error;
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
}
