package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;

import org.blendee.jdbc.TablePath;
import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.stock.StockComponent;
import jp.ats.blackbox.stock.StockComponentHandler;
import sqlassist.bb_stock.items;
import sqlassist.bb_stock.locations;
import sqlassist.bb_stock.owners;
import sqlassist.bb_stock.statuses;

public class StockComponetHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		SecurityValues.start(U.NULL_ID);

		Blendee.execute(t -> {
			execute(items.$TABLE);
			execute(locations.$TABLE);
			execute(owners.$TABLE);
			execute(statuses.$TABLE);

			t.rollback();
		});

		SecurityValues.end();
	}

	private static void execute(TablePath table) {
		var req = new StockComponent.RegisterRequest();
		req.group_id = U.NULL_ID;
		req.name = "test";
		req.props = Optional.of("{}");
		req.tags = Optional.of(new String[] { "tag1", "tag2" });

		UUID registered = StockComponentHandler.register(table, req);

		System.out.print("registered id: " + registered);

		var updateReq = new StockComponent.UpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
		updateReq.revision = 0;

		StockComponentHandler.update(table, updateReq);
	}
}
