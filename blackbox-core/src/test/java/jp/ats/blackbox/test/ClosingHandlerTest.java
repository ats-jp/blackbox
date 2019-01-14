package jp.ats.blackbox.test;

import java.sql.Timestamp;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.ClosingHandler;
import jp.ats.blackbox.persistence.ClosingHandler.ClosingRequest;
import jp.ats.blackbox.persistence.SecurityValues;

public class ClosingHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			var req = new ClosingRequest();
			req.group_id = U.NULL_ID;
			req.closed_at = new Timestamp(System.currentTimeMillis());

			IntStream.range(0, 10).forEach(i -> {
				ClosingHandler.close(SecurityValues.currentUserId(), req);
			});

			t.rollback();
		});
	}
}
