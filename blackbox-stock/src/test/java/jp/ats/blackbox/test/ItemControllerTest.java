package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.SecurityValues;
import jp.ats.blackbox.stock.controller.ItemController;
import jp.ats.blackbox.stock.persistence.ItemHandler;

public class ItemControllerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		SecurityValues.start(U.PRIVILEGE_ID);

		try {
			Blendee.execute(t -> {
				execute(execute());
			});
		} finally {
			JournalExecutorMap.shutdown();
		}

		SecurityValues.end();
	}

	private static UUID execute() throws Exception {
		var req = new ItemHandler.ItemRegisterRequest();
		req.group_id = U.NULL_ID;
		req.name = "test";
		req.props = Optional.of("{}");
		req.tags = Optional.of(new String[] { "tag1", "tag2" });

		var registered = ItemController.register(req);

		System.out.print("registered id: " + registered);

		var updateReq = new ItemHandler.ItemUpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
		updateReq.revision = 0;

		ItemController.update(updateReq);

		return registered;
	}

	private static void execute(UUID itemId) throws Exception {
		var req = new ItemHandler.SkuRegisterRequest();
		req.item_id = itemId;
		req.name = "test";
		req.props = Optional.of("{}");
		req.tags = Optional.of(new String[] { "tag1", "tag2" });

		var registered = ItemController.register(req);

		System.out.print("registered id: " + registered);

		var updateReq = new ItemHandler.SkuUpdateRequest();
		updateReq.id = registered;
		updateReq.name = Optional.of("test");
		updateReq.props = Optional.of("{}");
		updateReq.tags = Optional.of(new String[] { "tag1", "tag2" });
		updateReq.revision = 0;

		ItemController.update(updateReq);

		ItemController.deleteSku(registered, 1);

		ItemController.deleteItem(itemId, 1);
	}
}
