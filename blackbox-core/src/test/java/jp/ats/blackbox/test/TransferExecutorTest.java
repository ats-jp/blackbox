package jp.ats.blackbox.test;

import java.util.stream.IntStream;

import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.TransferExecutor;

public class TransferExecutorTest {

	public static void main(String[] args) throws Exception {
		Common.start();

		TransferExecutor.start();
		JobExecutor.start();

		Runnable r = () -> {
			IntStream.range(0, 10).forEach(i -> {
				System.out.println(i + " " + Thread.currentThread());
				var promise = TransferExecutor.register(() -> TransferHandlerTest.createRequest());

				try {
					System.out.println("returned: " + promise.getTransferId() + " " + Thread.currentThread());
				} catch (InterruptedException e) {
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
