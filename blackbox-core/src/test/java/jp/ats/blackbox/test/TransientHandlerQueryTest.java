package jp.ats.blackbox.test;

import java.time.LocalDateTime;
import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.TransientHandler;
import jp.ats.blackbox.persistence.TransientHandler.OwnerType;

public class TransientHandlerQueryTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			SecurityValues.start(U.NULL_ID);

			var req = new TransientHandler.RegisterRequest();
			req.owner_type = OwnerType.USER;
			req.transient_owner_id = U.NULL_ID;

			UUID transientId = TransientHandler.register(req);

			TransientHandler.executeQuery(LocalDateTime.now(), transientId);

			SecurityValues.end();
		});
	}
}
