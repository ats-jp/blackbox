package jp.ats.blackbox.persistence;

import java.util.Arrays;

import org.blendee.dialect.postgresql.ReturningUtilities;

import sqlassist.bb.bundles;
import sqlassist.bb.groups;
import sqlassist.bb.nodes;
import sqlassist.bb.stocks;
import sqlassist.bb.transfers;
import sqlassist.bb.users;

public class TransferHandler {

	public static void register(TransferComponent.TransferRegisterRequest request) {
		var group = new groups().selectClause(
			a -> a.SELECT(a.extension, a.$orgs().extension))
			.fetch(request.group_id)
			.orElseThrow(() -> new DataNotFoundException());

		var user = new users().SELECT(r -> r.extension)
			.fetch(User.currentUserId())
			.orElseThrow(() -> new DataNotFoundException());;

		long transferId = ReturningUtilities.insertAndReturn(
			transfers.$TABLE,
			u -> {
				u.add(transfers.group_id, request.group_id);
				request.denied_id.ifPresent(v -> u.add(transfers.denied_id, v));
				u.add(transfers.transferred_at, request.transferred_at);
				request.extension.ifPresent(v -> u.add(transfers.extension, v));
				u.add(transfers.group_extension, group.getExtension());
				u.add(transfers.org_extension, group.$orgs().getExtension());
				u.add(transfers.user_extension, user.getExtension());
			},
			r -> r.getLong(transfers.id),
			transfers.id);

		Arrays.stream(request.bundles).forEach(r -> registerBundle(transferId, r));
	}

	private static void registerBundle(
		long transferId,
		TransferComponent.BundleRegisterRequest request) {
		long bundleId = ReturningUtilities.insertAndReturn(
			bundles.$TABLE,
			u -> {
				u.add(bundles.transfer_id, transferId);
				request.extension.ifPresent(v -> u.add(bundles.extension, v));
			},
			r -> r.getLong(bundles.id),
			bundles.id);

		Arrays.stream(request.nodes).forEach(r -> registerNode(bundleId, r));
	}

	private static void registerNode(
		long bundleId,
		TransferComponent.NodeRegisterRequest request) {
		var stock = selectedStocks()
			.WHERE(a -> a.group_id.eq(request.group_id).AND.item_id.eq(request.item_id).AND.owner_id.eq(request.owner_id).AND.location_id.eq(request.location_id).AND.status_id.eq(request.status_id))
			.willUnique()
			.orElse(registerStock(request));

		long nodeId = ReturningUtilities.insertAndReturn(
			nodes.$TABLE,
			u -> {
				u.add(nodes.bundle_id, bundleId);
				request.extension.ifPresent(v -> u.add(bundles.extension, v));
				//TODO
			},
			r -> r.getLong(bundles.id),
			bundles.id);
	}

	private static stocks.Row registerStock(
		TransferComponent.NodeRegisterRequest request) {
		long stockId = ReturningUtilities.insertAndReturn(
			stocks.$TABLE,
			u -> {
				u.add(stocks.item_id, request.item_id);
				u.add(stocks.owner_id, request.owner_id);
				u.add(stocks.location_id, request.location_id);
				u.add(stocks.status_id, request.status_id);
			},
			r -> r.getLong(transfers.id),
			transfers.id);

		//関連情報取得のため改めて検索
		return selectedStocks().fetch(stockId).get();
	}

	private static stocks selectedStocks() {
		return new stocks().selectClause(
			a -> a.SELECT(
				a.id,
				a.$groups().extension,
				a.$items().extension,
				a.$owners().extension,
				a.$locations().extension,
				a.$statuses().extension));
	}
}
