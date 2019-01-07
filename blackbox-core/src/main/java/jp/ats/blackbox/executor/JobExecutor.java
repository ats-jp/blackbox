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
		service.scheduleWithFixedDelay(JobExecutor::execute, 0, 500, TimeUnit.MILLISECONDS);
	}

	public static void next(LocalDateTime time) {
		lock.lock();
		try {
			if (Objects.requireNonNull(time).isBefore(next)) {
				//より過去のものを採用
				next = time;
			}
		} finally {
			lock.unlock();
		}
	}

	public static void stop() {
		service.shutdown();
	}

	private static void execute() {
		var now = LocalDateTime.now();
		lock.lock();
		try {
			if (!next.isBefore(now)) return;
		} finally {
			lock.unlock();
		}

		executeJob(now);
	}

	private static void executeJob(LocalDateTime time) {
		try {
			Blendee.execute(t -> {
				JobHandler.execute(time);

				//他の接続からいち早く見えるようにcommit
				t.commit();

				var myNext = JobHandler.getNextTime();
				lock.lock();
				try {
					next = myNext;
				} finally {
					lock.unlock();
				}
			});
		} catch (Exception e) {
			//TODO 例外をlog
			e.printStackTrace();
		}
	}
}
