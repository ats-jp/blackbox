package jp.ats.blackbox.test;

import java.util.Optional;

import org.blendee.util.Blendee;

import jp.ats.blackbox.persistence.StockComponent;
import jp.ats.blackbox.persistence.StockComponentHandler;
import sqlassist.bb.locations;

public class StockComponetHandlerTest2 {

	public static void main(String[] args) throws Exception {
		Common.startWithLog();

		Blendee.execute(t -> {
			var req = new StockComponent.UpdateRequest();
			req.group_id = Optional.of(0L);
			req.name = Optional.of("test");
			req.extension = Optional.of("{}");
			req.tags = Optional.of(new String[] { "tag1", "tag2" });
			req.revision = 0;

			StockComponentHandler.update(locations.$TABLE, req);

			t.rollback();
		});
	}
}
