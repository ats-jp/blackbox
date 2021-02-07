package jp.ats.blackbox.test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.persistence.GroupHandler;
import jp.ats.blackbox.core.persistence.GroupHandler.RegisterRequest;
import jp.ats.blackbox.core.persistence.InOut;
import jp.ats.blackbox.core.persistence.JobHandler;
import jp.ats.blackbox.core.persistence.JournalHandler;
import jp.ats.blackbox.core.persistence.Requests.DetailRegisterRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalRegisterRequest;
import jp.ats.blackbox.core.persistence.Requests.NodeRegisterRequest;
import jp.ats.blackbox.stock.persistence.StockHandler;
import jp.ats.blackbox.stock.persistence.StockHandler.StockComponents;

public class JournalHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			var handler = new JournalHandler(U.recorder, () -> {
			});
			IntStream.range(0, 10).forEach(i -> {
				handler.register(UUID.randomUUID(), U.NULL_ID, U.NULL_ID, createRequest(registerGroup()));
				JobHandler.execute(LocalDateTime.now());

				t.commit(); //created_atを確定するために一件毎commit
			});
		});
	}

	static JournalRegisterRequest createRequest(UUID groupId) {
		var com = new StockComponents() {

			@Override
			public UUID groupId() {
				return groupId;
			}

			@Override
			public UUID skuId() {
				return U.NULL_ID;
			}

			@Override
			public UUID ownerId() {
				return U.NULL_ID;
			}

			@Override
			public UUID locationId() {
				return U.NULL_ID;
			}

			@Override
			public UUID statusId() {
				return U.NULL_ID;
			}
		};

		var out = StockHandler.buildNodeRegisterRequest(U.NULL_ID, com, InOut.OUT, BigDecimal.valueOf(100), Optional.of(true), Optional.empty(), Optional.empty());
		var in = StockHandler.buildNodeRegisterRequest(U.NULL_ID, com, InOut.IN, BigDecimal.valueOf(100), Optional.of(true), Optional.empty(), Optional.empty());

		var bundle = new DetailRegisterRequest();

		//通常はout -> inだがMinusTotalExceptionとなるのでテストではin -> outとする
		//bundle.nodes = new NodeRegisterRequest[] { out, in };
		bundle.nodes = new NodeRegisterRequest[] { in, out };

		var transfer = new JournalRegisterRequest();
		transfer.group_id = groupId;
		transfer.fixed_at = new Timestamp(System.currentTimeMillis());

		transfer.tags = Optional.of(new String[] { "tag1", "tag2" });

		transfer.details = new DetailRegisterRequest[] { bundle };

		return transfer;
	}

	public static UUID registerGroup() {
		var req = new RegisterRequest();
		req.name = "test group";
		req.parent_id = U.NULL_ID;
		req.org_id = U.NULL_ID;

		return GroupHandler.register(req, U.NULL_ID);
	}
}
