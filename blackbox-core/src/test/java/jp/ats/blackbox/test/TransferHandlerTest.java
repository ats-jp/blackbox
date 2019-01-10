package jp.ats.blackbox.test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.model.InOut;
import jp.ats.blackbox.persistence.JobHandler;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.TransferComponent.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
import jp.ats.blackbox.persistence.TransferHandler;

public class TransferHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			var handler = new TransferHandler();
			IntStream.range(0, 10).forEach(i -> {
				handler.register(UUID.randomUUID(), SecurityValues.currentUserId(), createRequest());
				JobHandler.execute(LocalDateTime.now());

				t.commit(); //created_atを確定するために一件毎commit
			});
		});
	}

	static TransferRegisterRequest createRequest() {
		var out = new NodeRegisterRequest();
		out.group_id = U.NULL;
		out.item_id = U.NULL;
		out.owner_id = U.NULL;
		out.location_id = U.NULL;
		out.status_id = U.NULL;
		out.in_out = InOut.OUT;
		out.grants_infinity = Optional.of(true);
		out.quantity = BigDecimal.valueOf(100);

		var in = new NodeRegisterRequest();
		in.group_id = U.NULL;
		in.item_id = U.NULL;
		in.owner_id = U.NULL;
		in.location_id = U.NULL;
		in.status_id = U.NULL;
		in.in_out = InOut.IN;
		in.quantity = BigDecimal.valueOf(100);

		var bundle = new BundleRegisterRequest();

		//通常はout -> inだがMinusTotalExceptionとなるのでテストではin -> outとする
		//bundle.nodes = new NodeRegisterRequest[] { out, in };
		bundle.nodes = new NodeRegisterRequest[] { in, out };

		var transfer = new TransferRegisterRequest();
		transfer.group_id = U.NULL;
		transfer.transferred_at = new Timestamp(System.currentTimeMillis());

		transfer.tags = Optional.of(new String[] { "tag1", "tag2" });

		transfer.bundles = new BundleRegisterRequest[] { bundle };

		return transfer;
	}
}
