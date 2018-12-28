package jp.ats.blackbox.test;

import java.util.Optional;

import org.blendee.util.Blendee;

import jp.ats.blackbox.persistence.StockComponent;
import jp.ats.blackbox.persistence.StockComponentHandler;
import sqlassist.bb.locations;

public class StockComponetHandlerTest {

	public static void main(String[] args) throws Exception {
		Common.startWithLog();

		Blendee.execute(t -> {
			var req = new StockComponent.RegisterRequest();
			req.group_id = 0;
			req.name = "test";
			req.extension = Optional.of("{}");
			req.tags = Optional.of(new String[] { "tag1", "tag2" });

			long registered = StockComponentHandler.register(locations.$TABLE, req);

			System.out.print("registered id: " + registered);

			t.rollback();
		});
	}
}
