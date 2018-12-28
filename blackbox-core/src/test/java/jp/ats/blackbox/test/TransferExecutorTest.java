package jp.ats.blackbox.test;

import java.util.stream.IntStream;

import jp.ats.blackbox.executor.TransferExecutor;

public class TransferExecutorTest {

	public static void main(String[] args) throws Exception {
		Common.start();

		TransferExecutor.start();

		Runnable r = () -> {
			IntStream.range(0, 100).forEach(i -> {
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

		try {
			Thread.sleep(1000000000);
		} catch (Exception e) {
			return;
		}

		TransferExecutor.stop();
	}
}
