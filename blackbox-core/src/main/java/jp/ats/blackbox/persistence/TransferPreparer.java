package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;
import static org.blendee.sql.Placeholder.$BOOLEAN;
import static org.blendee.sql.Placeholder.$INT;
import static org.blendee.sql.Placeholder.$UUID;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import org.blendee.sql.Recorder;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.TransferComponent.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
import sqlassist.bb.current_stocks;
import sqlassist.bb.groups;
import sqlassist.bb.stocks;
import sqlassist.bb.users;

class TransferPreparer {

	static void prepareTransfer(
		UUID transferId,
		UUID userId,
		UUID instanceId,
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
		var stock = recorder.play(
			() -> selectedStocks()
				.WHERE(a -> a.group_id.eq($UUID).AND.item_id.eq($UUID).AND.owner_id.eq($UUID).AND.location_id.eq($UUID).AND.status_id.eq($UUID)),
			request.group_id,
			request.item_id,
			request.owner_id,
			request.location_id,
			request.status_id)
			.willUnique()
			.orElseGet(() -> registerStock(userId, request, recorder));

		UUID stockId = stock.getId();

		node.setId(nodeId);
		node.setBundle_id(bundleId);
		//nodeではgroup_idを持たないが、requestが持つgroup_idはstockに格納しており、それが在庫の所属グループを表す
		node.setStock_id(stockId);
		node.setIn_out(request.in_out.value);
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

	/**
	 * stock登録処理
	 */
	private static stocks.Row registerStock(UUID userId, NodeRegisterRequest request, Recorder recorder) {
		UUID stockId = UUID.randomUUID();

		recorder.play(
			() -> new stocks().insertStatement(
				a -> a
					.INSERT(
						a.id,
						a.group_id,
						a.item_id,
						a.owner_id,
						a.location_id,
						a.status_id,
						a.created_by)
					.VALUES(
						$UUID,
						$UUID,
						$UUID,
						$UUID,
						$UUID,
						$UUID,
						$UUID)),
			stockId,
			request.group_id,
			request.item_id,
			request.owner_id,
			request.location_id,
			request.status_id,
			userId)
			.execute();

		recorder.play(
			() -> new current_stocks().insertStatement(
				//後でjobから更新されるのでunlimitedはとりあえずfalse、totalは0
				a -> a.INSERT(a.id, a.unlimited, a.total, a.snapshot_id).VALUES($UUID, $BOOLEAN, $INT, $UUID)),
			stockId,
			false,
			0,
			U.NULL_ID).execute();

		//関連情報取得のため改めて検索
		return recorder.play(() -> selectedStocks()).fetch(stockId).get();
	}

	private static stocks selectedStocks() {
		return new stocks().SELECT(
			a -> a.ls(
				a.id,
				a.$groups().extension,
				a.$items().extension,
				a.$owners().extension,
				a.$locations().extension,
				a.$statuses().extension));
	}

	static interface Transfer {

		void setId(UUID id);

		void setGroup_id(UUID id);

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

		void setIn_out(String value);

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
