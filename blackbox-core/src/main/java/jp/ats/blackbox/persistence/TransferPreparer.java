package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.function.Supplier;

import org.blendee.sql.Recorder;

import jp.ats.blackbox.persistence.TransferHandler.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferHandler.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferHandler.TransferRegisterRequest;
import sqlassist.bb.groups;
import sqlassist.bb.stocks;
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

		transfer.setTransfer_batch_id(batchId);

		request.denied_id.ifPresent(v -> transfer.setDenied_id(v));
		request.deny_reason.ifPresent(v -> transfer.setDeny_reason(v));

		transfer.setTransferred_at(request.transferred_at);

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
		bundle.setTransfer_id(transferId);
		request.restored_extension
			.ifPresentOrElse(
				v -> bundle.setExtension(v),
				() -> request.extension.ifPresent(v -> bundle.setExtension(toJson(v))));
	}

	private static final Supplier<stocks> selectStocks = () -> new stocks().SELECT(
		a -> a.ls(
			a.id,
			a.$groups().extension,
			a.$items().extension,
			a.$owners().extension,
			a.$locations().extension,
			a.$statuses().extension));

	static UUID prepareNode(
		UUID bundleId,
		UUID nodeId,
		UUID userId,
		NodeRegisterRequest request,
		Node node,
		int nodeSeq,
		Recorder recorder) {
		//stockが既に存在すればそれを使う
		//なければ新たに登録
		var stock = StockHandler.prepareStock(selectStocks, userId, request, recorder);

		UUID stockId = stock.getId();

		node.setId(nodeId);
		node.setBundle_id(bundleId);
		//nodeではgroup_idを持たないが、requestが持つgroup_idはstockに格納しており、それが在庫の所属グループを表す
		node.setStock_id(stockId);
		node.setIn_out(request.in_out.intValue);
		node.setSeq(nodeSeq);
		node.setQuantity(request.quantity);

		request.grants_unlimited.ifPresent(v -> node.setGrants_unlimited(v));

		request.restored_extension
			.ifPresentOrElse(
				v -> node.setExtension(v),
				() -> request.extension.ifPresent(v -> node.setExtension(toJson(v))));

		node.setGroup_extension(stock.$groups().getExtension());
		node.setItem_extension(stock.$items().getExtension());
		node.setOwner_extension(stock.$owners().getExtension());
		node.setLocation_extension(stock.$locations().getExtension());
		node.setStatus_extension(stock.$statuses().getExtension());

		return stockId;
	}

	static interface Transfer {

		void setId(UUID id);

		void setGroup_id(UUID id);

		void setTransfer_batch_id(UUID id);

		void setDenied_id(UUID id);

		void setDeny_reason(String reason);

		void setTransferred_at(Timestamp transferredAt);

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

		void setTransfer_id(UUID transferId);

		void setExtension(Object json);
	}

	static interface Node {

		void setId(UUID id);

		void setBundle_id(UUID bundleId);

		void setStock_id(UUID stockId);

		void setIn_out(Integer value);

		void setSeq(Integer seq);

		void setQuantity(BigDecimal quantity);

		void setGrants_unlimited(Boolean value);

		void setExtension(Object json);

		void setGroup_extension(Object json);

		void setItem_extension(Object json);

		void setOwner_extension(Object json);

		void setLocation_extension(Object json);

		void setStatus_extension(Object json);
	}
}
