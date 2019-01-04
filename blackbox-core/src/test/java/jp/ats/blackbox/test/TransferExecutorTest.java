package jp.ats.blackbox.test;

import java.util.stream.IntStream;

import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.TransferExecutor;
import jp.ats.blackbox.persistence.SecurityValues;

public class TransferExecutorTest {

	public static void main(String[] args) throws Exception {
		Common.start();

		TransferExecutor.start();
		JobExecutor.start();

		Runnable r = () -> {
			IntStream.range(0, 10).forEach(i -> {
				var promise = TransferExecutor.register(SecurityValues.currentUserId(), () -> TransferHandlerTest.createRequest());

				try {
					long newId = promise.getTransferId();
					System.out.println("returned: " + newId + " " + Thread.currentThread());

					var denyPromise = TransferExecutor.deny(SecurityValues.currentUserId(), newId);
					System.out.println("denied: " + denyPromise.getTransferId() + " " + Thread.currentThread());
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			});
		};

		new Thread(r).start();
		new Thread(r).start();
		new Thread(r).start();
		new Thread(r).start();
		new Thread(r).start();

		System.gc();

		while (true) {
			try {
				Thread.sleep(1000);
				System.gc();
			} catch (Exception e) {
				break;
			}
		}

		TransferExecutor.stop();
		JobExecutor.stop();
	}
}
