package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.JournalExecutor;
import jp.ats.blackbox.persistence.Requests.JournalDenyRequest;
import jp.ats.blackbox.persistence.SecurityValues;

public class JournalExecutorTest {

	private static final Logger logger = LogManager.getLogger(JournalExecutorTest.class);

	public static void main(String[] args) throws Exception {
		JournalCommon.start();
		//TransferCommon.startWithLog();

		SecurityValues.start(U.NULL_ID);
		execute(GroupHandlerTest.register(), 100, 5);
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
				logger.trace("##### " + i);

				var promise = executor.registerJournal(U.NULL_ID, JournalHandlerTest.createRequest(groupId, unitId));

				try {
					UUID newId = promise.getId();
					logger.trace("register: " + newId + " " + Thread.currentThread());

					promise.waitUntilFinished();

					var req = new JournalDenyRequest();
					req.deny_id = newId;
					req.deny_reason = Optional.of("deny reson");

					var denyPromise = executor.denyJournal(U.NULL_ID, req);

					logger.trace("deny    : " + denyPromise.getId() + " " + Thread.currentThread());

					denyPromise.waitUntilFinished();
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

		System.gc();

		executor.stop();
	}
}
