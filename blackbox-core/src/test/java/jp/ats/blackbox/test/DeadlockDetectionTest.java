package jp.ats.blackbox.test;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.GroupHandler.RegisterRequest;
import jp.ats.blackbox.persistence.SecurityValues;

public class DeadlockDetectionTest {

	public static void main(String[] args) {
		TransferCommon.start();

		var req = new RegisterRequest();
		req.name = "test";
		req.parent_id = U.NULL_ID;

		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();

		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();

		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
		new Thread(() -> exec()).start();
	}

	private static void exec() {
		SecurityValues.start(U.NULL_ID);
		TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1);
		SecurityValues.end();
	}
}
