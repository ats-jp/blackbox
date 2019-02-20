package jp.ats.blackbox.test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.GroupHandler;
import jp.ats.blackbox.persistence.GroupHandler.RegisterRequest;
import jp.ats.blackbox.persistence.SecurityValues;

public class GroupHandlerTest {

	static final AtomicInteger counter = new AtomicInteger();

	public static void main(String[] args) {
		TransferCommon.startWithLog();
		SecurityValues.start(U.NULL_ID);
		register();
		SecurityValues.end();
	}

	static UUID register() {
		var req = new RegisterRequest();
		req.name = name();
		req.parent_id = U.NULL_ID;
		req.org_id = U.NULL_ID;

		UUID[] id = { null };

		Blendee.execute(t -> {
			id[0] = GroupHandler.register(req);
		});

		return id[0];
	}

	static UUID register(UUID parentId) {
		var req = new RegisterRequest();
		req.name = name();
		req.parent_id = parentId;
		req.org_id = U.NULL_ID;

		UUID[] id = { null };

		Blendee.execute(t -> {
			id[0] = GroupHandler.register(req);
		});

		return id[0];
	}

	private static String name() {
		return "test-" + counter.getAndIncrement();
	}
}
