package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.stock.OwnerHandler;

public class OwnerHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		SecurityValues.start(U.NULL_ID);

		Blendee.execute(t -> {
			execute();
		});

		SecurityValues.end();
	}

	private static void execute() {
		var req = new OwnerHandler.RegisterRequest();
		req.group_id = U.NULL_ID;
		req.name = "test";
		req.props = Optional.of("{}");
		req.tags = Optional.of(new String[] { "tag1", "tag2" });

		UUID registered = OwnerHandler.register(req, U.NULL_ID);

		System.out.print("registered id: " + registered);

		var updateReq = new OwnerHandler.UpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
		updateReq.revision = 0;

		OwnerHandler.update(updateReq, U.NULL_ID);
	}
}
