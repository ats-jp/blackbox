package jp.ats.blackbox.test;

import java.util.UUID;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.TransferExecutor;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.TransferComponent.TransferDenyRequest;

public class TransferExecutorTest {

	private static final Logger logger = LogManager.getLogger(TransferExecutorTest.class);

	public static void main(String[] args) throws Exception {
		TransferCommon.start();
		//TransferCommon.startWithLog();

		execute(GroupHandlerTest.register(), 100, 5);
	}

	static void execute(UUID group, int transfers, int threads) {
		var executor = new TransferExecutor();

		executor.start();
		JobExecutor.start();

		Runnable r = () -> {
			IntStream.range(0, transfers).forEach(i -> {
				logger.trace("##### " + i);

				var promise = executor.register(SecurityValues.currentUserId(), () -> TransferHandlerTest.createRequest(group));

				try {
					UUID newId = promise.getTransferId();
					logger.trace("register: " + newId + " " + Thread.currentThread());

					promise.waitUntilFinished();

					var denyPromise = executor.deny(SecurityValues.currentUserId(), () -> {
						var req = new TransferDenyRequest();
						req.denyId = newId;
						return req;
					});

					logger.trace("deny    : " + denyPromise.getTransferId() + " " + Thread.currentThread());

					denyPromise.waitUntilFinished();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			});
		};

		IntStream.range(0, threads).forEach(i -> {
			new Thread(r).start();
		});

		System.gc();

		while (true) {
			try {
				Thread.sleep(1000);
				System.gc();
			} catch (Exception e) {
				break;
			}
		}

		executor.stop();
		JobExecutor.stop();
	}
}
