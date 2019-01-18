package jp.ats.blackbox.test;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.GroupHandler.RegisterRequest;

public class DeadlockDetectionTest {

	public static void main(String[] args) {
		TransferCommon.start();

		var req = new RegisterRequest();
		req.name = "test";
		req.parent_id = U.NULL_ID;

		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();

		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();

		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 1)).start();
	}
}
