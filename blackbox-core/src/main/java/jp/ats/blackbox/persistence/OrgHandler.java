package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.common.U;
import sqlassist.bb.instances;
import sqlassist.bb.orgs;

public class OrgHandler {

	public static class RegisterRequest {

		public Optional<UUID> instanceId = Optional.empty();

		public String name;

		public Optional<String> description = Optional.empty();

		public Optional<String> props = Optional.empty();
	}

	public static UUID register(RegisterRequest request) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = orgs.$TABLE;
		seqRequest.dependsColumn = orgs.instance_id;

		var instanceId = request.instanceId.orElseGet(() -> new instances().SELECT(a -> a.id).WHERE(a -> a.principal.eq(true)).willUnique().get().getId());

		seqRequest.dependsId = instanceId;
		return SeqHandler.getInstance().nextSeqAndGet(seqRequest, seq -> {
			return registerInternal(seq, instanceId, request.name, request.description, request.props);
		});
	}

	private static UUID registerInternal(long seq, UUID instanceId, String name, Optional<String> description, Optional<String> props) {
		var row = orgs.row();

		UUID id = UUID.randomUUID();

		UUID userId = SecurityValues.currentUserId();

		row.setId(id);
		row.setInstance_id(instanceId);
		row.setSeq(seq);
		row.setName(name);
		description.ifPresent(v -> row.setDescription(v));
		props.ifPresent(v -> row.setProps(U.toPGObject(v)));
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		return id;
	}

	public static class UpdateRequest {

		public UUID id;

		public long revision;

		public Optional<String> name = Optional.empty();

		public Optional<String> description = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}

	public static void update(UpdateRequest request) {
		int result = new orgs().UPDATE(a -> {
			a.revision.set(request.revision + 1);
			request.name.ifPresent(v -> a.name.set(v));
			request.description.ifPresent(v -> a.description.set(v));
			request.props.ifPresent(v -> a.props.set(v));
			request.active.ifPresent(v -> a.active.set(v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(orgs.$TABLE, request.id);
	}

	public static void delete(UUID orgId, long revision) {
		Utils.delete(orgs.$TABLE, orgId, revision);
	}

	public static orgs.Row get(UUID id) {
		return optional(id).get();
	}

	public static Optional<orgs.Row> optional(UUID id) {
		return new orgs().fetch(id);
	}
}
