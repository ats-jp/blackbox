package jp.ats.blackbox.test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import com.google.gson.Gson;

import jp.ats.blackbox.backend.api.request.SimpleDetailRegisterRequest;
import jp.ats.blackbox.backend.api.request.SimpleJournalRegisterRequest;
import jp.ats.blackbox.backend.api.request.SimpleNodeRegisterRequest;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.persistence.InOut;
import jp.ats.blackbox.core.persistence.SecurityValues;
import jp.ats.blackbox.core.persistence.UnitHandler;

public class JournalRegisterTest {

	//実行前にApplication起動のこと
	public static void main(String[] args) throws Exception {
		Common.startWithLog();

		SecurityValues.start(U.PRIVILEGE_ID);

		var json = Blendee.executeAndGet(t -> {
			return new Gson().toJson(createRequest(U.PRIVILEGE_ID));
		});

		var c = HttpClient.newHttpClient();

		var req = HttpRequest.newBuilder()
			.POST(BodyPublishers.ofString(json))
			.uri(URI.create("http://localhost:8080/api/journals/register"))
			.build();

		var start = System.currentTimeMillis();
		IntStream.range(0, 100).forEach(i -> {
			try {
				System.out.println(i + "\t" + c.send(req, BodyHandlers.ofString()).body());
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});

		System.out.println(System.currentTimeMillis() - start);
	}

	private static SimpleJournalRegisterRequest createRequest(UUID groupId) {
		var out = new SimpleNodeRegisterRequest();
		var in = new SimpleNodeRegisterRequest();

		var unit = UnitHandler.register(U.PRIVILEGE_ID, U.PRIVILEGE_ID);

		out.in_out = InOut.OUT;
		out.quantity = BigDecimal.ONE;
		out.unit_id = unit;

		in.in_out = InOut.IN;
		in.quantity = BigDecimal.ONE;
		in.unit_id = unit;

		var bundle = new SimpleDetailRegisterRequest();

		bundle.nodes = new SimpleNodeRegisterRequest[] { in, out };

		var journal = new SimpleJournalRegisterRequest();
		journal.group_id = groupId;
		journal.fixed_at = new Timestamp(System.currentTimeMillis());

		journal.tags = Optional.of(new String[] { "tag1", "tag2" });

		journal.details = new SimpleDetailRegisterRequest[] { bundle };

		return journal;
	}
}
