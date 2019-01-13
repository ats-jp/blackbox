package jp.ats.blackbox.test;

import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.GroupHandler;
import jp.ats.blackbox.persistence.GroupHandler.RegisterRequest;

public class DeadlockDetectionTest {

	public static void main(String[] args) {
		TransferCommon.start();

		var req = new RegisterRequest();
		req.name = "test";
		req.parent_id = U.NULL_ID;

		UUID[] ids = { null, null };

		Blendee.execute(t -> {
			ids[0] = GroupHandler.register(req);
			ids[1] = GroupHandler.register(req);
		});

		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 5)).start();
		new Thread(() -> TransferExecutorTest.execute(GroupHandlerTest.register(), 1000, 5)).start();
	}
}
