package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.Requests.TransientMoveRequest;
import jp.ats.blackbox.persistence.Role;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.TransientHandler;
import jp.ats.blackbox.persistence.UserHandler;

public class TransientHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			SecurityValues.start(U.NULL_ID);

			var req = new TransientHandler.RegisterRequest();

			req.group_id = U.NULL_ID;

			var userReq = new UserHandler.RegisterRequest();
			userReq.name = "test";
			userReq.role = Role.USER;
			userReq.groupId = U.NULL_ID;
			userReq.props = Optional.of("{}");
			userReq.tags = Optional.of(new String[] {});

			req.user_id = Optional.of(UserHandler.register(userReq));

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
			batchReq.lazy = true;

			TransientHandler.move(batchId, U.NULL_ID, batchReq, U.recorder, () -> {
			});

			SecurityValues.end();
		});
	}
}
