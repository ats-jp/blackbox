package jp.ats.blackbox.test;

import java.util.Optional;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.SecurityValues;
import jp.ats.blackbox.stock.controller.OwnerController;
import jp.ats.blackbox.stock.persistence.OwnerHandler;

public class OwnerControllerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		SecurityValues.start(U.PRIVILEGE_ID);

		try {
			Blendee.execute(t -> {
				execute();
			});
		} finally {
			JournalExecutorMap.shutdown();
		}

		SecurityValues.end();
	}

	private static void execute() throws Exception {
		var req = new OwnerHandler.RegisterRequest();
		req.group_id = U.PRIVILEGE_ID;
		req.name = "test";
		req.props = Optional.of("{}");
		req.tags = Optional.of(new String[] { "tag1", "tag2" });

		var registered = OwnerController.register(req);

		System.out.print("registered id: " + registered);

		var updateReq = new OwnerHandler.UpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
		updateReq.revision = 0;

		OwnerController.update(updateReq);

		OwnerController.delete(registered, 1);
	}
}
