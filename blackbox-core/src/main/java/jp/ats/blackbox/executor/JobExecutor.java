package jp.ats.blackbox.executor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.blendee.util.Blendee;

import jp.ats.blackbox.persistence.JobHandler;

public class JobExecutor {

	private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

	private static final ReentrantLock lock = new ReentrantLock();

	private static LocalDateTime next = LocalDateTime.now();

	public static void start() {
		service.scheduleWithFixedDelay(JobExecutor::execute, 0, 1, TimeUnit.SECONDS);
	}

	public static void next(LocalDateTime time) {
		lock.lock();
		try {
			//より過去のものを採用
			next = Objects.requireNonNull(time).isBefore(next) ? time : next;
		} finally {
			lock.unlock();
		}
	}

	public static void stop() {
		service.shutdown();
	}

	private static void execute() {
		lock.lock();
		try {
			if (!next.isBefore(LocalDateTime.now())) return;
		} finally {
			lock.unlock();
		}

		try {
			Blendee.execute(t -> {
				JobHandler.execute(next);

				//他の接続からいち早く見えるようにcommit
				t.commit();

				lock.lock();
				try {
					next = JobHandler.getNextTime();
				} finally {
					lock.unlock();
				}
			});
		} catch (Exception e) {
			//TODO 例外をlog
		}
	}
}
