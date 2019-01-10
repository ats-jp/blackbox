package jp.ats.blackbox.executor;

import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransferPromise {

	private final ReentrantLock lock = new ReentrantLock();

	private final Condition condition = lock.newCondition();

	private final UUID id = UUID.randomUUID();

	private boolean finished;

	private Throwable error;

	public UUID getTransferId() {
		return id;
	}

	public void waitUntilFinished() throws TransferFailedException, InterruptedException {
		lock.lock();
		try {
			if (finished) {
				checkError();
				return;
			}

			condition.await();
			checkError();
		} finally {
			lock.unlock();
		}
	}

	void notify(Throwable error) {
		lock.lock();
		try {
			finished = true;
			this.error = error;
			condition.signal();
		} finally {
			lock.unlock();
		}
	}

	private void checkError() throws TransferFailedException {
		if (error != null) throw new TransferFailedException(error);
	}
}
