package jp.ats.blackbox.test;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.stream.IntStream;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.TransferExecutor;
import jp.ats.blackbox.persistence.ClosingHandler.ClosingRequest;
import jp.ats.blackbox.persistence.SecurityValues;

public class TransferErrorTest {

	public static void main(String[] args) throws Exception {
		TransferCommon.startWithLog();

		SecurityValues.start(U.NULL_ID);
		execute(GroupHandlerTest.register(), 1, 1);
		SecurityValues.end();
	}

	static void execute(UUID group, int transfers, int threads) {
		var executor = new TransferExecutor();

		executor.start();
		JobExecutor.start();

		Runnable r = () -> {
			IntStream.range(0, transfers).forEach(i -> {

				try {
					var closePromise = executor.close(U.NULL_ID, () -> {
						var req = new ClosingRequest();
						req.group_id = group;
						req.closed_at = new Timestamp(System.currentTimeMillis() + 1000000);

						return req;
					});

					closePromise.waitUntilFinished();

					var promise = executor.registerTransfer(U.NULL_ID, () -> TransferHandlerTest.createRequest(group));

					promise.waitUntilFinished();
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
	}
}
