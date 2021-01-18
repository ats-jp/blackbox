package jp.ats.blackbox.test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;

import org.blendee.jdbc.Transaction;
import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.ClosingHandler;
import jp.ats.blackbox.persistence.Requests.ClosingRequest;

public class ClosingHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			exec(t, U.NULL_ID);
		});
	}

	private static void exec(Transaction t, UUID... ids) {
		Arrays.stream(ids).forEach(id -> {
			var req = new ClosingRequest();

			req.group_id = id;
			req.closed_at = new Timestamp(System.currentTimeMillis());

			ClosingHandler.close(UUID.randomUUID(), U.NULL_ID, req);

			t.commit();
		});
	}
}
