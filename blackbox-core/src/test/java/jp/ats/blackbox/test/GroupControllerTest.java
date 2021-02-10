package jp.ats.blackbox.test;

import java.util.Optional;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.controller.GroupController;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.GroupHandler;
import jp.ats.blackbox.core.persistence.SecurityValues;

public class GroupControllerTest {

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
		var req = new GroupHandler.RegisterRequest();
		req.org_id = U.PRIVILEGE_ID;
		req.parent_id = U.NULL_ID;
		req.name = "test";
		req.props = Optional.of("{}");
		req.tags = Optional.of(new String[] { "tag1", "tag2" });

		var registered = GroupController.register(req);

		System.out.println("registered id: " + registered);

		var updateReq = new GroupHandler.UpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
		updateReq.revision = 0;

		GroupController.update(updateReq);

		GroupController.delete(registered, 1);
	}
}
