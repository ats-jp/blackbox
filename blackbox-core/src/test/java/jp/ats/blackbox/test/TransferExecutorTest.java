package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.TransferExecutor;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.TransferHandler.TransferDenyRequest;

public class TransferExecutorTest {

	private static final Logger logger = LogManager.getLogger(TransferExecutorTest.class);

	public static void main(String[] args) throws Exception {
		TransferCommon.start();
		//TransferCommon.startWithLog();

		SecurityValues.start(U.NULL_ID);
		execute(GroupHandlerTest.register(), 100, 5);
		SecurityValues.end();
	}

	static void execute(UUID groupId, int transfers, int threads) {
		AtomicInteger counter = new AtomicInteger(0);

		var executor = new TransferExecutor();

		var unitId = UnitHandlerTest.register(groupId);

		executor.start();
		JobExecutor.start();

		Runnable r = () -> {
			IntStream.range(0, transfers).forEach(i -> {
				logger.trace("##### " + i);

				var promise = executor.registerTransfer(U.NULL_ID, () -> TransferHandlerTest.createRequest(groupId, unitId));

				try {
					UUID newId = promise.getId();
					logger.trace("register: " + newId + " " + Thread.currentThread());

					promise.waitUntilFinished();

					var denyPromise = executor.denyTransfer(U.NULL_ID, () -> {
						var req = new TransferDenyRequest();
						req.deny_id = newId;
						req.deny_reason = Optional.of("deny reson");
						return req;
					});

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
