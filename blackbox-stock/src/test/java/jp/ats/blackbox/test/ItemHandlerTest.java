package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.persistence.SecurityValues;
import jp.ats.blackbox.stock.persistence.ItemHandler;

public class ItemHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		SecurityValues.start(U.NULL_ID);

		Blendee.execute(t -> {
			execute(execute());
		});

		SecurityValues.end();
	}

	private static UUID execute() {
		var req = new ItemHandler.ItemRegisterRequest();
		req.group_id = U.NULL_ID;
		req.name = "test";
		req.owner_id = U.NULL_ID;
		req.props = Optional.of("{}");
		req.tags = Optional.of(new String[] { "tag1", "tag2" });

		UUID registered = ItemHandler.registerItem(req, U.NULL_ID);

		System.out.print("registered id: " + registered);

		var updateReq = new ItemHandler.ItemUpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
		updateReq.revision = 0;

		ItemHandler.updateItem(updateReq, U.NULL_ID);

		return registered;
	}

	private static void execute(UUID itemId) {
		var req = new ItemHandler.SkuRegisterRequest();
		req.item_id = itemId;
		req.name = "test";
		req.props = Optional.of("{}");
		req.tags = Optional.of(new String[] { "tag1", "tag2" });

		UUID registered = ItemHandler.registerSku(req, U.NULL_ID);

		System.out.print("registered id: " + registered);

		var updateReq = new ItemHandler.SkuUpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
		updateReq.revision = 0;

		ItemHandler.updateSku(updateReq, U.NULL_ID);
	}
}
