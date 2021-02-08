package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.SecurityValues;
import jp.ats.blackbox.stock.controller.StatusController;
import jp.ats.blackbox.stock.persistence.StatusHandler;

public class StatusHandlerTest {

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
		var req = new StatusHandler.RegisterRequest();
		req.group_id = U.PRIVILEGE_ID;
		req.name = "test";
		req.owner_id = U.NULL_ID;
		req.props = Optional.of("{}");
		req.tags = Optional.of(new String[] { "tag1", "tag2" });

		UUID registered = StatusController.register(req);

		System.out.print("registered id: " + registered);

		var updateReq = new StatusHandler.UpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
		updateReq.revision = 0;

		StatusController.update(updateReq);
	}
}
