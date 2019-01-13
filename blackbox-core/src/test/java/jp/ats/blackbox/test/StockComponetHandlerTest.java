package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.StockComponent;
import jp.ats.blackbox.persistence.StockComponentHandler;
import sqlassist.bb.locations;

public class StockComponetHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			var req = new StockComponent.RegisterRequest();
			req.group_id = U.NULL_ID;
			req.name = "test";
			req.extension = Optional.of("{}");
			req.tags = Optional.of(new String[] { "tag1", "tag2" });

			UUID registered = StockComponentHandler.register(locations.$TABLE, req);

			System.out.print("registered id: " + registered);

			var updateReq = new StockComponent.UpdateRequest();
			updateReq.id = registered;
			updateReq.group_id = Optional.of(U.NULL_ID);
			updateReq.name = Optional.of("test");
			updateReq.extension = Optional.of("{}");
			updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
			updateReq.revision = 0;

			StockComponentHandler.update(locations.$TABLE, updateReq);

			t.rollback();
		});
	}
}
