package jp.ats.blackbox.test;

import java.util.Optional;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.controller.OrgController;
import jp.ats.blackbox.core.persistence.OrgHandler;
import jp.ats.blackbox.core.persistence.SecurityValues;

public class OrgControllerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		SecurityValues.start(U.PRIVILEGE_ID);

		Blendee.execute(t -> {
			execute();
		});

		SecurityValues.end();
	}

	private static void execute() throws Exception {
		var req = new OrgHandler.RegisterRequest();
		req.name = "test";
		req.props = Optional.of("{}");

		var registered = OrgController.register(req);

		System.out.print("registered id: " + registered);

		var updateReq = new OrgHandler.UpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.revision = 0;

		OrgController.update(updateReq);

		OrgController.delete(registered, 1);
	}
}
