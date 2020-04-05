package jp.ats.blackbox.test;

import java.util.UUID;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.Role;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.TransientHandler;
import jp.ats.blackbox.persistence.TransientHandler.OwnerType;
import jp.ats.blackbox.persistence.TransientHandler.TransientMoveRequest;
import jp.ats.blackbox.persistence.UserHandler;

public class TransientHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			SecurityValues.start(U.NULL_ID);

			var userId = UserHandler.register("test", Role.USER, U.NULL_ID, "{}");

			var req = new TransientHandler.RegisterRequest();
			req.owner_type = OwnerType.USER;
			req.transient_owner_id = userId;

			UUID transientId = TransientHandler.register(req);

			IntStream.range(0, 10).forEach(i -> {
				TransientHandler.registerJournal(i, transientId, JournalHandlerTest.createRequest(U.NULL_ID, U.NULL_ID));
			});

			TransientHandler.check(transientId, (e, c) -> {
				System.out.println(e);
			});

			UUID batchId = UUID.randomUUID();

			var batchReq = new TransientMoveRequest();
			batchReq.transient_id = transientId;

			TransientHandler.move(batchId, U.NULL_ID, batchReq, U.recorder);

			SecurityValues.end();
		});
	}
}