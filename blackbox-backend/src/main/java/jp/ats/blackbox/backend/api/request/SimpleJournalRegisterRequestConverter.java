package jp.ats.blackbox.backend.api.request;

import java.util.Arrays;
import java.util.Objects;

import org.blendee.jdbc.BlendeeManager;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.persistence.Requests.DetailRegisterRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalRegisterRequest;
import jp.ats.blackbox.core.persistence.Requests.NodeRegisterRequest;

public class SimpleJournalRegisterRequestConverter {

	public static JournalRegisterRequest convert(SimpleJournalRegisterRequest request) {
		var result = new JournalRegisterRequest();

		result.group_id = Objects.requireNonNull(request.group_id);
		result.fixed_at = Objects.requireNonNull(request.fixed_at);
		result.description = request.description;
		result.props = request.props.map(p -> U.toJson(p));
		result.tags = request.tags;

		result.details = Arrays.stream(request.details).map(d -> convert(d)).toArray(DetailRegisterRequest[]::new);

		BlendeeManager.get().getCurrentTransaction().commit();

		return result;
	}

	private static DetailRegisterRequest convert(SimpleDetailRegisterRequest request) {
		var result = new DetailRegisterRequest();

		result.props = request.props.map(p -> U.toJson(p));
		result.nodes = Arrays.stream(request.nodes).map(n -> convert(n)).toArray(NodeRegisterRequest[]::new);

		return result;
	}

	private static NodeRegisterRequest convert(SimpleNodeRegisterRequest request) {
		var result = new NodeRegisterRequest();

		result.unit_id = request.unit_id;
		result.in_out = request.in_out;
		result.quantity = request.quantity;
		result.grants_unlimited = request.grants_unlimited;
		result.props = request.props.map(p -> U.toJson(p));

		return result;
	}
}
