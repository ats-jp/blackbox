package jp.ats.blackbox.test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.InOut;
import jp.ats.blackbox.persistence.JobHandler;
import jp.ats.blackbox.persistence.JournalHandler;
import jp.ats.blackbox.persistence.JournalHandler.DetailRegisterRequest;
import jp.ats.blackbox.persistence.JournalHandler.NodeRegisterRequest;
import jp.ats.blackbox.persistence.JournalHandler.JournalRegisterRequest;

public class JournalHandlerLazyTest {

	public static void main(String[] args) {
		Common.startWithLog();

		Blendee.execute(t -> {
			var handler = new JournalHandler(U.recorder, () -> {
			});
			IntStream.range(0, 10).forEach(i -> {
				handler.registerLazily(UUID.randomUUID(), U.NULL_ID, U.NULL_ID, createRequest(U.NULL_ID, U.NULL_ID));
				JobHandler.execute(LocalDateTime.now());

				t.commit(); //created_atを確定するために一件毎commit
			});
		});
	}

	static JournalRegisterRequest createRequest(UUID groupId, UUID unitId) {
		var out = new NodeRegisterRequest();
		out.unit_id = unitId;
		out.in_out = InOut.OUT;
		out.grants_unlimited = Optional.of(true);
		out.quantity = BigDecimal.valueOf(100);

		var in = new NodeRegisterRequest();
		in.unit_id = unitId;
		in.in_out = InOut.IN;
		in.quantity = BigDecimal.valueOf(100);

		var bundle = new DetailRegisterRequest();

		bundle.nodes = new NodeRegisterRequest[] { out, in };

		var transfer = new JournalRegisterRequest();
		transfer.group_id = groupId;
		transfer.fixed_at = new Timestamp(System.currentTimeMillis());

		transfer.tags = Optional.of(new String[] { "tag1", "tag2" });

		transfer.details = new DetailRegisterRequest[] { bundle };

		return transfer;
	}
}
