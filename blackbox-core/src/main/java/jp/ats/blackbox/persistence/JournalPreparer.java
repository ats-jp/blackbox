package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.common.U.toPGObject;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import org.blendee.sql.Recorder;

import jp.ats.blackbox.persistence.Requests.DetailRegisterRequest;
import jp.ats.blackbox.persistence.Requests.JournalRegisterRequest;
import jp.ats.blackbox.persistence.Requests.NodeRegisterRequest;
import sqlassist.bb.groups;
import sqlassist.bb.users;

class JournalPreparer {

	static void prepareJournal(
		UUID journalId,
		UUID instanceId,
		UUID batchId,
		UUID userId,
		JournalRegisterRequest request,
		Timestamp createdAt,
		Journal journal,
		Recorder recorder) {
		var group = recorder.play(() -> new groups().SELECT(a -> a.ls(a.props, a.$orgs().props, a.$orgs().revision, a.revision)))
			.fetch(request.group_id)
			.orElseThrow(() -> new DataNotFoundException(groups.$TABLE, request.group_id));

		var user = recorder.play(() -> new users().SELECT(r -> r.ls(r.props, r.revision)))
			.fetch(userId)
			.orElseThrow(() -> new DataNotFoundException(users.$TABLE, userId));

		journal.setId(journalId);
		journal.setGroup_id(request.group_id);

		journal.setJournal_batch_id(batchId);

		request.denied_id.ifPresent(v -> journal.setDenied_id(v));
		request.deny_reason.ifPresent(v -> journal.setDeny_reason(v));

		journal.setFixed_at(request.fixed_at);

		request.description.ifPresent(v -> journal.setDescription(v));

		journal.setCreated_at(createdAt);

		request.restored_props.ifPresentOrElse(
			v -> journal.setProps(v),
			() -> request.props.ifPresent(v -> journal.setProps(toPGObject(v))));

		journal.setGroup_props(group.getProps());
		journal.setOrg_props(group.$orgs().getProps());
		journal.setUser_props(user.getProps());

		journal.setGroup_revision(group.getRevision());
		journal.setOrg_revision(group.$orgs().getRevision());
		journal.setUser_revision(user.getRevision());

		request.tags.ifPresent(v -> journal.setTags(v));

		journal.setInstance_id(instanceId);

		journal.setCreated_by(userId);
	}

	static void prepareDetail(UUID journalId, UUID detailId, DetailRegisterRequest request, Detail detail) {
		detail.setId(detailId);
		detail.setJournal_id(journalId);
		request.restored_props
			.ifPresentOrElse(
				v -> detail.setProps(v),
				() -> request.props.ifPresent(v -> detail.setProps(toPGObject(v))));
	}

	static void prepareNode(
		UUID detailId,
		UUID nodeId,
		NodeRegisterRequest request,
		Node node,
		int nodeSeq,
		Recorder recorder) {
		node.setId(nodeId);
		node.setDetail_id(detailId);
		node.setUnit_id(request.unit_id);
		node.setIn_out(request.in_out.intValue);
		node.setSeq(nodeSeq);
		node.setQuantity(request.quantity);

		request.grants_unlimited.ifPresent(v -> node.setGrants_unlimited(v));

		request.restored_props
			.ifPresentOrElse(
				v -> node.setProps(v),
				() -> request.props.ifPresent(v -> node.setProps(toPGObject(v))));

		request.unit_props.ifPresent(v -> node.setUnit_props(toPGObject(v)));
	}

	static interface Journal {

		void setId(UUID id);

		void setGroup_id(UUID id);

		void setJournal_batch_id(UUID id);

		void setDenied_id(UUID id);

		void setDeny_reason(String reason);

		void setFixed_at(Timestamp fixedAt);

		void setDescription(String string);

		void setCreated_at(Timestamp createdAt);

		void setProps(Object json);

		void setGroup_props(Object json);

		void setOrg_props(Object json);

		void setUser_props(Object json);

		void setGroup_revision(Long revision);

		void setOrg_revision(Long revision);

		void setUser_revision(Long revision);

		void setTags(Object tags);

		void setInstance_id(UUID id);

		void setCreated_by(UUID userId);
	}

	static interface Detail {

		void setId(UUID id);

		void setJournal_id(UUID journalId);

		void setProps(Object json);
	}

	static interface Node {

		void setId(UUID id);

		void setDetail_id(UUID detailId);

		void setUnit_id(UUID unitId);

		void setIn_out(Integer value);

		void setSeq(Integer seq);

		void setQuantity(BigDecimal quantity);

		void setGrants_unlimited(Boolean value);

		void setProps(Object json);

		void setUnit_props(Object json);

		void setUnit_group_revision(Long revision);
	}
}
