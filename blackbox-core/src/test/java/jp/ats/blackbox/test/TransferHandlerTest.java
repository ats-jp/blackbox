package jp.ats.blackbox.test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.model.InOut;
import jp.ats.blackbox.persistence.JobHandler;
import jp.ats.blackbox.persistence.TransferComponent.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
import jp.ats.blackbox.persistence.TransferHandler;

public class TransferHandlerTest {

	public static void main(String[] args) throws Exception {
		Common.start();

		var out = new NodeRegisterRequest();
		out.group_id = 0;
		out.item_id = 0;
		out.owner_id = 0;
		out.location_id = 0;
		out.status_id = 0;
		out.in_out = InOut.OUT;
		out.quantity = BigDecimal.valueOf(100);

		var in = new NodeRegisterRequest();
		in.group_id = 0;
		in.item_id = 0;
		in.owner_id = 0;
		in.location_id = 0;
		in.status_id = 0;
		in.in_out = InOut.IN;
		in.quantity = BigDecimal.valueOf(100);

		var bundle = new BundleRegisterRequest();

		//通常はout -> inだがMinusTotalExceptionとなるのでテストではin -> outとする
		//bundle.nodes = new NodeRegisterRequest[] { out, in };
		bundle.nodes = new NodeRegisterRequest[] { in, out };

		var transfer = new TransferRegisterRequest();
		transfer.group_id = 0;
		transfer.transferred_at = new Timestamp(System.currentTimeMillis());
		transfer.bundles = new BundleRegisterRequest[] { bundle };

		Blendee.execute(t -> {
			IntStream.range(0, 10).forEach(i -> {
				TransferHandler.register(transfer);
				JobHandler.execute(LocalDateTime.now());
			});

			t.rollback();
		});
	}
}
