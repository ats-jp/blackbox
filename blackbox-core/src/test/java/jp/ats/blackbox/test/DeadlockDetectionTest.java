package jp.ats.blackbox.test;

import jp.ats.blackbox.common.U;

public class DeadlockDetectionTest {

	public static void main(String[] args) {
		TransferCommon.start();
		new Thread(() -> TransferExecutorTest.execute(U.NULL_ID, 1000, 5)).start();
		new Thread(() -> TransferExecutorTest.execute(U.PRIVILEGE_ID, 1000, 5)).start();
	}
}
