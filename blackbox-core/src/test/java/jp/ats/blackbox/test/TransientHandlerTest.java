package jp.ats.blackbox.test;

import java.util.UUID;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.TransientHandler;
import jp.ats.blackbox.persistence.TransientHandler.OwnerType;

public class TransientHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			SecurityValues.start(U.NULL_ID);

			var req = new TransientHandler.RegisterRequest();
			req.owner_type = OwnerType.USER;
			req.transient_owner_id = U.NULL_ID;

			UUID transientId = TransientHandler.register(req);

			IntStream.range(0, 100).forEach(i -> {
				TransientHandler.registerTransfer(transientId, TransferHandlerTest.createRequest(U.NULL_ID));
			});

			SecurityValues.end();
		});
	}
}
