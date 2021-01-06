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

import org.blendee.util.Blendee;

import com.google.gson.Gson;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.GroupHandler;
import jp.ats.blackbox.persistence.GroupHandler.RegisterRequest;
import jp.ats.blackbox.persistence.InOut;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.Requests.DetailRegisterRequest;
import jp.ats.blackbox.persistence.Requests.JournalRegisterRequest;
import jp.ats.blackbox.persistence.Requests.NodeRegisterRequest;
import jp.ats.blackbox.persistence.UnitHandler;

public class JournalRegisterTest {

	//実行前にApplication起動のこと
	public static void main(String[] args) throws Exception {
		Common.startWithLog();

		SecurityValues.start(U.NULL_ID);

		var json = Blendee.executeAndGet(t -> {
			return new Gson().toJson(createRequest(registerGroup()));
		});

		var c = HttpClient.newHttpClient();

		var req = HttpRequest.newBuilder()
			.POST(BodyPublishers.ofString(json))
			.uri(URI.create("http://localhost:8080/api/journals/register"))
			.build();

		System.out.println(c.send(req, BodyHandlers.ofString()).body());
	}

	private static JournalRegisterRequest createRequest(UUID groupId) {
		var out = new NodeRegisterRequest();
		var in = new NodeRegisterRequest();

		var unit = registerUnit();

		out.in_out = InOut.OUT;
		out.quantity = BigDecimal.ONE;
		out.unit_id = unit;

		in.in_out = InOut.IN;
		in.quantity = BigDecimal.ONE;
		in.unit_id = unit;

		var bundle = new DetailRegisterRequest();

		bundle.nodes = new NodeRegisterRequest[] { in, out };

		var journal = new JournalRegisterRequest();
		journal.group_id = groupId;
		journal.fixed_at = new Timestamp(System.currentTimeMillis());

		journal.tags = Optional.of(new String[] { "tag1", "tag2" });

		journal.details = new DetailRegisterRequest[] { bundle };

		return journal;
	}

	public static UUID registerGroup() {
		var req = new RegisterRequest();
		req.name = "test group";
		req.parent_id = U.NULL_ID;
		req.org_id = U.NULL_ID;

		return GroupHandler.register(req, U.NULL_ID);
	}

	public static UUID registerUnit() {
		return UnitHandler.register(U.NULL_ID);
	}
}
