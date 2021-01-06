package jp.ats.blackbox.test;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.JournalExecutor;
import jp.ats.blackbox.persistence.Requests.ClosingRequest;
import jp.ats.blackbox.persistence.SecurityValues;

public class JournalErrorTest {

	public static void main(String[] args) throws Exception {
		JournalCommon.startWithLog();

		SecurityValues.start(U.NULL_ID);
		execute(GroupHandlerTest.register(), 1, 1);
		SecurityValues.end();
	}

	static void execute(UUID groupId, int transfers, int threads) {
		AtomicInteger counter = new AtomicInteger(0);

		var executor = new JournalExecutor();

		var unitId = UnitHandlerTest.register();

		executor.start();
		JobExecutor.start();

		Runnable r = () -> {
			IntStream.range(0, transfers).forEach(i -> {

				try {
					var req = new ClosingRequest();
					req.group_id = groupId;
					req.closed_at = new Timestamp(System.currentTimeMillis() + 1000000);

					var closePromise = executor.close(U.NULL_ID, req);

					closePromise.waitUntilFinished();

					var promise = executor.registerJournal(U.NULL_ID, JournalHandlerTest.createRequest(groupId, unitId));

					promise.waitUntilFinished();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			});

			counter.incrementAndGet();
		};

		IntStream.range(0, threads).forEach(i -> {
			new Thread(r).start();
		});

		System.gc();

		while (true) {
			try {
				Thread.sleep(100);

				if (counter.get() == threads) break;
			} catch (Exception e) {
				break;
			}
		}

		executor.stop();
	}
}
