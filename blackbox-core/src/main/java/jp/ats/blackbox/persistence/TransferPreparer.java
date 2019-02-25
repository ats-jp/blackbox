package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import org.blendee.sql.Recorder;

import jp.ats.blackbox.persistence.TransferHandler.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferHandler.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferHandler.TransferRegisterRequest;
import sqlassist.bb.groups;
import sqlassist.bb.units;
import sqlassist.bb.users;

class TransferPreparer {

	static void prepareTransfer(
		UUID transferId,
		UUID instanceId,
		UUID batchId,
		UUID userId,
		TransferRegisterRequest request,
		Timestamp createdAt,
		Transfer transfer,
		Recorder recorder) {
		var group = recorder.play(() -> new groups().SELECT(a -> a.ls(a.extension, a.$orgs().extension)))
			.fetch(request.group_id)
			.orElseThrow(() -> new DataNotFoundException(groups.$TABLE, request.group_id));

		var user = recorder.play(() -> new users().SELECT(r -> r.extension))
			.fetch(userId)
			.orElseThrow(() -> new DataNotFoundException(users.$TABLE, userId));

		transfer.setId(transferId);
		transfer.setGroup_id(request.group_id);

		transfer.setJournal_batch_id(batchId);

		request.denied_id.ifPresent(v -> transfer.setDenied_id(v));
		request.deny_reason.ifPresent(v -> transfer.setDeny_reason(v));

		transfer.setFixed_at(request.transferred_at);

		transfer.setCreated_at(createdAt);

		request.restored_extension.ifPresentOrElse(
			v -> transfer.setExtension(v),
			() -> request.extension.ifPresent(v -> transfer.setExtension(toJson(v))));

		transfer.setGroup_extension(group.getExtension());
		transfer.setOrg_extension(group.$orgs().getExtension());
		transfer.setUser_extension(user.getExtension());

		request.tags.ifPresent(v -> transfer.setTags(v));

		transfer.setInstance_id(instanceId);

		transfer.setCreated_by(userId);
	}

	static void prepareBundle(UUID transferId, UUID bundleId, BundleRegisterRequest request, Bundle bundle) {
		bundle.setId(bundleId);
		bundle.setJournal_id(transferId);
		request.restored_extension
			.ifPresentOrElse(
				v -> bundle.setExtension(v),
				() -> request.extension.ifPresent(v -> bundle.setExtension(toJson(v))));
	}

	static void prepareNode(
		UUID bundleId,
		UUID nodeId,
		UUID userId,
		NodeRegisterRequest request,
		Node node,
		int nodeSeq,
		Recorder recorder) {
		node.setId(nodeId);
		node.setDetail_id(bundleId);
		//nodeではgroup_idを持たないが、requestが持つgroup_idはstockに格納しており、それが在庫の所属グループを表す
		node.setUnit_id(request.unit_id);
		node.setIn_out(request.in_out.intValue);
		node.setSeq(nodeSeq);
		node.setQuantity(request.quantity);

		request.grants_unlimited.ifPresent(v -> node.setGrants_unlimited(v));

		request.restored_extension
			.ifPresentOrElse(
				v -> node.setExtension(v),
				() -> request.extension.ifPresent(v -> node.setExtension(toJson(v))));

		node.setGroup_extension(
			recorder.play(() -> new units().SELECT(a -> a.$groups().extension)).fetch(request.unit_id).get().$groups().getExtension());

		request.unit_extension.ifPresent(v -> node.setUnit_extension(JsonHelper.toJson(v)));
	}

	static interface Transfer {

		void setId(UUID id);

		void setGroup_id(UUID id);

		void setJournal_batch_id(UUID id);

		void setDenied_id(UUID id);

		void setDeny_reason(String reason);

		void setFixed_at(Timestamp transferredAt);

		void setCreated_at(Timestamp createdAt);

		void setExtension(Object json);

		void setGroup_extension(Object json);

		void setOrg_extension(Object json);

		void setUser_extension(Object json);

		void setTags(Object tags);

		void setInstance_id(UUID id);

		void setCreated_by(UUID userId);
	}

	static interface Bundle {

		void setId(UUID id);

		void setJournal_id(UUID transferId);

		void setExtension(Object json);
	}

	static interface Node {

		void setId(UUID id);

		void setDetail_id(UUID bundleId);

		void setUnit_id(UUID stockId);

		void setIn_out(Integer value);

		void setSeq(Integer seq);

		void setQuantity(BigDecimal quantity);

		void setGrants_unlimited(Boolean value);

		void setExtension(Object json);

		void setGroup_extension(Object json);

		void setUnit_extension(Object json);
	}
}
