package jp.ats.blackbox.test;

import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.GroupHandler;
import jp.ats.blackbox.persistence.GroupHandler.RegisterRequest;

public class GroupHandlerTest {

	public static void main(String[] args) {
		TransferCommon.start();
		register();
	}

	static UUID register() {
		var req = new RegisterRequest();
		req.name = "test";
		req.parent_id = U.NULL_ID;

		UUID[] id = { null };

		Blendee.execute(t -> {
			id[0] = GroupHandler.register(req);
		});

		return id[0];
	}
}
