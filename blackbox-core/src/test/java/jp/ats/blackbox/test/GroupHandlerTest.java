package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.persistence.GroupHandler;
import jp.ats.blackbox.core.persistence.GroupHandler.RegisterRequest;
import jp.ats.blackbox.core.persistence.SecurityValues;

public class GroupHandlerTest {

	static final AtomicInteger counter = new AtomicInteger();

	public static void main(String[] args) {
		JournalCommon.startWithLog();
		register();
		SecurityValues.end();
	}

	public static UUID register() {
		var req = new RegisterRequest();
		req.name = name();
		req.parent_id = U.NULL_ID;
		req.org_id = U.NULL_ID;
		req.props = Optional.of("{}");

		UUID[] id = { null };

		Blendee.execute(t -> {
			id[0] = GroupHandler.register(req, U.NULL_ID);
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
			id[0] = GroupHandler.register(req, U.NULL_ID);
		});

		return id[0];
	}

	private static String name() {
		return "test-" + counter.getAndIncrement();
	}
}
