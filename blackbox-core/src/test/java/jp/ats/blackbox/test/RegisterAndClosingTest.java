package jp.ats.blackbox.test;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.JournalExecutor;
import jp.ats.blackbox.persistence.Requests.ClosingRequest;
import jp.ats.blackbox.persistence.Requests.JournalDenyRequest;
import jp.ats.blackbox.persistence.SecurityValues;

public class RegisterAndClosingTest {

	private static final Logger logger = LogManager.getLogger(RegisterAndClosingTest.class);

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

					var denyReq = new JournalDenyRequest();
					denyReq.deny_id = newId;

					var denyPromise = executor.denyJournal(U.NULL_ID, denyReq);

					logger.trace("deny    : " + denyPromise.getId() + " " + Thread.currentThread());

					denyPromise.waitUntilFinished();

					var closingReq = new ClosingRequest();
					closingReq.group_id = groupId;
					closingReq.closed_at = new Timestamp(System.currentTimeMillis() - 100000);

					var closePromise = executor.close(U.NULL_ID, closingReq);

					logger.trace("close   : " + closePromise.getId() + " " + Thread.currentThread());

					closePromise.waitUntilFinished();
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
