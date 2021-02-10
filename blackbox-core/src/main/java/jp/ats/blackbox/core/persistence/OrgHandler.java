package jp.ats.blackbox.core.persistence;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.common.U;
import sqlassist.bb.instances;
import sqlassist.bb.orgs;

public class OrgHandler {

	public static class RegisterRequest {

		public Optional<UUID> instanceId = Optional.empty();

		public String name;

		public Optional<String> code = Optional.empty();

		public Optional<String> description = Optional.empty();

		public Optional<String> props = Optional.empty();
	}

	public static UUID register(RegisterRequest request, UUID userId) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = orgs.$TABLE;
		seqRequest.dependsColumn = orgs.instance_id;

		var instanceId = request.instanceId.orElseGet(() -> new instances().SELECT(a -> a.id).WHERE(a -> a.principal.eq(true)).willUnique().get().getId());

		seqRequest.dependsId = instanceId;
		return SeqHandler.getInstance().nextSeqAndGet(seqRequest, seq -> {
			return registerInternal(seq, instanceId, request, userId);
		});
	}

	private static UUID registerInternal(long seq, UUID instanceId, RegisterRequest request, UUID userId) {
		var row = orgs.row();

		UUID id = UUID.randomUUID();

		row.setId(id);
		row.setInstance_id(instanceId);
		row.setSeq(seq);
		row.setName(request.name);
		//デフォルトコードはID
		row.setCode(request.code.orElse(id.toString()));
		request.description.ifPresent(v -> row.setDescription(v));
		request.props.ifPresent(v -> row.setProps(U.toPGObject(v)));
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		return id;
	}

	public static class UpdateRequest {

		public UUID id;

		public long revision;

		public Optional<String> name = Optional.empty();

		public Optional<String> code = Optional.empty();

		public Optional<String> description = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}

	public static void update(UpdateRequest request, UUID userId) {
		int result = new orgs().UPDATE(a -> {
			a.revision.set(request.revision + 1);
			request.name.ifPresent(v -> a.name.set(v));
			request.code.ifPresent(v -> a.code.set(v));
			request.description.ifPresent(v -> a.description.set(v));
			request.props.ifPresent(v -> a.props.set(U.toPGObject(v)));
			request.active.ifPresent(v -> a.active.set(v));
			a.updated_by.set(userId);
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(orgs.$TABLE, request.id);
	}

	public static void delete(UUID orgId, long revision) throws AlreadyUsedException {
		Utils.delete(orgs.$TABLE, orgId, revision);
	}

	public static orgs.Row get(UUID id) {
		return optional(id).get();
	}

	public static Optional<orgs.Row> optional(UUID id) {
		return new orgs().fetch(id);
	}
}
