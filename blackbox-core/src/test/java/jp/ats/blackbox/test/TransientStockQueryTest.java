package jp.ats.blackbox.test;

import java.time.LocalDateTime;
import java.util.UUID;

import org.blendee.assist.Vargs;
import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.TransientHandler;
import jp.ats.blackbox.persistence.TransientHandler.OwnerType;
import jp.ats.blackbox.persistence.TransientStockQuery;

public class TransientStockQueryTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			SecurityValues.start(U.NULL_ID);

			var req = new TransientHandler.RegisterRequest();
			req.owner_type = OwnerType.USER;
			req.transient_owner_id = U.NULL_ID;

			UUID transientId = TransientHandler.register(req);

			var query = new TransientStockQuery(table -> {}, table -> {});

			query.execute(LocalDateTime.now(), transientId, Vargs.of(), Vargs.of(), r -> {});

			SecurityValues.end();
		});
	}
}
