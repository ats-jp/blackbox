package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$UUID;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.blendee.assist.AnonymousTable;
import org.blendee.jdbc.BResultSet;
import org.blendee.jdbc.BSQLException;
import org.blendee.jdbc.Result;
import org.blendee.sql.InsertDMLBuilder;
import org.blendee.sql.Recorder;
import org.blendee.sql.UpdateDMLBuilder;
import org.blendee.sql.Updater;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.TransferComponent.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
import sqlassist.bb.closed_stocks;
import sqlassist.bb.nodes;
import sqlassist.bb.transient_bundles;
import sqlassist.bb.transient_nodes;
import sqlassist.bb.transient_transfers;
import sqlassist.bb.transients;

public class TransientHandler {

	public static UUID register(RegisterRequest request) {
		UUID id = UUID.randomUUID();

		UUID userId = SecurityValues.currentUserId();

		var builder = new InsertDMLBuilder(transients.$TABLE);
		builder.add(transients.id, id);
		builder.add(transients.created_by, userId);
		builder.add(transients.updated_by, userId);

		request.owner_type.set(request.transient_owner_id, builder);

		builder.executeUpdate();

		return id;
	}

	public static void update(UpdateRequest request) {
		UUID userId = SecurityValues.currentUserId();

		var builder = new UpdateDMLBuilder(transients.$TABLE);
		builder.addSQLFragment(transients.updated_at, "now()");
		builder.add(transients.updated_by, userId);

		transients.Row[] cache = { null };
		Supplier<transients.Row> row = () -> {
			if (cache[0] == null)
				cache[0] = new transients().fetch(request.id).get();
			return cache[0];
		};

		OwnerType type = request.owner_type.orElseGet(() -> OwnerType.of(row.get().getOwner_type()));
		UUID ownerId = request.transient_owner_id.orElseGet(() -> type.getOwnerId(row.get()));

		type.set(ownerId, builder);

		if (builder.executeUpdate() != 1)
			throw Utils.decisionException(transients.$TABLE, request.id);
	}

	public static void delete(UUID transientId, long revision) {
		Utils.delete(transients.$TABLE, transientId, revision);
	}

	private static class Transfer extends transient_transfers.Row implements TransferPreparer.Transfer {

		@Override
		public void setDenied_id(UUID id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDeny_reason(String reason) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setGroup_extension(Object json) {}

		@Override
		public void setOrg_extension(Object json) {}

		@Override
		public void setUser_extension(Object json) {}

		@Override
		public void setInstance_id(UUID id) {}
	}

	public static void registerTransfer(UUID transientId, TransferRegisterRequest request) {
		var id = UUID.randomUUID();
		var transfer = new Transfer();
		var userId = SecurityValues.currentUserId();
		TransferPreparer.prepareTransfer(
			id,
			userId,
			U.NULL_ID,
			request,
			transfer,
			U.recorder);

		transfer.setTransient_id(transientId);
		transfer.setUpdated_by(userId);

		transfer.insert();

		for (int i = 0; i < request.bundles.length; i++) {
			registerBundle(userId, id, request.bundles[i], i + 1);
		}
	}

	public TransferRegisterRequest buildTransferRegisterRequest(UUID transientTransferId, UUID transferId, UUID userId, Recorder recorder) {
		var request = new TransferRegisterRequest();

		var bundles = new LinkedList<BundleRegisterRequest>();

		recorder.play(
			() -> new transient_nodes().selectClause(
				a -> {
					var bundlesAssist = a.$transient_bundles();
					var transfersAssist = bundlesAssist.$transient_transfers();
					var stocksAssist = a.$stocks();

					a.SELECT(
						transfersAssist.group_id,
						transfersAssist.transferred_at,
						transfersAssist.extension,
						transfersAssist.tags,
						bundlesAssist.extension,
						stocksAssist.group_id,
						stocksAssist.item_id,
						stocksAssist.owner_id,
						stocksAssist.location_id,
						stocksAssist.status_id,
						a.in_out,
						a.quantity,
						a.grants_unlimited,
						a.extension);
				})
				.WHERE(a -> a.$transient_bundles().transient_transfer_id.eq($UUID))
				.assist()
				.$transient_bundles()
				.$transient_transfers()
				.intercept(),
			transientTransferId)
			.forEach(transferOne -> {
				var transfer = transferOne.get();

				request.group_id = transfer.getGroup_id();
				request.transferred_at = transfer.getTransferred_at();
				request.restored_extension = Optional.of(transfer.getExtension());

				try {
					request.tags = Optional.of(Utils.restoreTags(transfer.getTags()));
				} catch (SQLException e) {
					throw new BSQLException(e);
				}

				transferOne.many().forEach(bundleOne -> {
					var nodes = new LinkedList<NodeRegisterRequest>();

					var bundleRequest = new BundleRegisterRequest();

					bundleRequest.restored_extension = Optional.of(bundleOne.get().getExtension());

					bundles.add(bundleRequest);

					bundleOne.many().forEach(nodeOne -> {
						var node = nodeOne.get();

						var nodeRequest = new NodeRegisterRequest();

						var stock = node.$stocks();

						nodeRequest.group_id = stock.getGroup_id();
						nodeRequest.item_id = stock.getItem_id();
						nodeRequest.owner_id = stock.getOwner_id();
						nodeRequest.location_id = stock.getLocation_id();
						nodeRequest.status_id = stock.getStatus_id();
						nodeRequest.in_out = InOut.of(node.getIn_out()).reverse();
						nodeRequest.quantity = node.getQuantity();
						nodeRequest.grants_unlimited = Optional.of(node.getGrants_unlimited());
						nodeRequest.restored_extension = Optional.of(node.getExtension());

						nodes.add(nodeRequest);
					});

					bundleRequest.nodes = nodes.toArray(new NodeRegisterRequest[nodes.size()]);
				});

				request.bundles = bundles.toArray(new BundleRegisterRequest[bundles.size()]);
			});

		return request;
	}

	private static class Bundle extends transient_bundles.Row implements TransferPreparer.Bundle {

		@Override
		public void setTransfer_id(UUID transferId) {
			super.setTransient_transfer_id(transferId);
		}
	}

	private static void registerBundle(UUID userId, UUID transferId, BundleRegisterRequest request, int seq) {
		var bundle = new Bundle();

		UUID id = UUID.randomUUID();

		TransferPreparer.prepareBundle(transferId, id, request, bundle);

		bundle.setSeq_in_transfer(seq);
		bundle.setCreated_by(userId);
		bundle.setUpdated_by(userId);

		bundle.insert();

		for (int i = 0; i < request.nodes.length; i++) {
			registerNode(userId, id, request.nodes[i], i + 1);
		}
	}

	private static class Node extends transient_nodes.Row implements TransferPreparer.Node {

		@Override
		public void setBundle_id(UUID bundleId) {
			super.setTransient_bundle_id(bundleId);
		}

		@Override
		public void setSeq(Integer seq) {}

		@Override
		public void setGroup_extension(Object json) {}

		@Override
		public void setItem_extension(Object json) {}

		@Override
		public void setOwner_extension(Object json) {}

		@Override
		public void setLocation_extension(Object json) {}

		@Override
		public void setStatus_extension(Object json) {}
	}

	private static void registerNode(UUID userId, UUID bundleId, NodeRegisterRequest request, int seq) {
		var node = new Node();

		UUID id = UUID.randomUUID();

		TransferPreparer.prepareNode(bundleId, id, userId, request, node, seq, U.recorder);

		node.setSeq_in_bundle(seq);
		node.setCreated_by(userId);
		node.setUpdated_by(userId);

		node.insert();
	}

	public static enum OwnerType {

		GROUP {

			@Override
			void set(UUID ownerId, Updater updater) {
				updater.add(transients.owner_type, Constant.GROUP);
				updater.add(transients.group_id, ownerId);
				updater.add(transients.user_id, U.NULL_ID);
			}

			@Override
			UUID getOwnerId(transients.Row row) {
				return row.getGroup_id();
			}
		},

		USER {

			@Override
			void set(UUID ownerId, Updater updater) {
				updater.add(transients.owner_type, Constant.USER);
				updater.add(transients.group_id, U.NULL_ID);
				updater.add(transients.user_id, ownerId);
			}

			@Override
			UUID getOwnerId(transients.Row row) {
				return row.getUser_id();
			}
		};

		private static class Constant {

			private static final String GROUP = "G";

			private static final String USER = "U";
		}

		private static OwnerType of(String value) {
			switch (value) {
			case Constant.GROUP:
				return GROUP;
			case Constant.USER:
				return USER;
			default:
				throw new Error();
			}
		}

		abstract void set(UUID ownerId, Updater updater);

		abstract UUID getOwnerId(transients.Row row);
	}

	public static class RegisterRequest {

		public UUID transient_owner_id;

		public OwnerType owner_type;
	}

	public static class UpdateRequest {

		public UUID id;

		public Optional<UUID> transient_owner_id;

		public Optional<OwnerType> owner_type;

		public long revision;
	}

	public static void check(UUID transientId, Recorder recorder) {
		recorder.play(() -> build(), transientId, transientId).aggregate(r -> checkInternal(r, recorder));
	}

	private static void checkInternal(BResultSet result, Recorder recorder) {
		UUID currentStockId = null;

		BigDecimal currentStockTotal = null;
		boolean currentStockUnlimited = false;

		while (result.next()) {
			var row = new ResultRow(result);

			if (row.closed_at != null && row.transferred_at.getTime() <= row.closed_at.getTime())
				throw new InvalidTransientException();

			if (!row.stock_id.equals(currentStockId)) {
				currentStockId = row.stock_id;

				//直近のsnapshotを取得
				var snapshot = TransferHandler.getJustBeforeSnapshot(currentStockId, row.transferred_at, recorder);
				currentStockTotal = snapshot.total;
				currentStockUnlimited = snapshot.unlimited;
			}

			//すでに無制限と判明していれば
			if (currentStockUnlimited) continue;

			currentStockUnlimited = currentStockUnlimited | row.grants_unlimited;

			currentStockTotal = row.in_out.calcurate(currentStockTotal, row.quantity);

			if (!currentStockUnlimited && currentStockTotal.compareTo(BigDecimal.ZERO) < 0)
				throw new InvalidTransientException();
		}
	}

	private static class ResultRow {

		private final UUID stock_id;

		private final boolean grants_unlimited;

		private final InOut in_out;

		private final BigDecimal quantity;

		private final Timestamp transferred_at;

		private final Timestamp closed_at;

		private ResultRow(Result result) {
			stock_id = UUID.fromString(result.getString("stock_id"));
			grants_unlimited = result.getBoolean("grants_unlimited");
			in_out = InOut.of(result.getString("in_out"));
			quantity = result.getBigDecimal("quantity");
			transferred_at = result.getTimestamp("transferred_at");
			closed_at = result.getTimestamp("closed_at");
		}
	}

	private static AnonymousTable build() {
		var inner = new nodes()
			.SELECT(
				a -> a.ls(
					a.any(0).AS("node_type"),
					a.stock_id,
					a.grants_unlimited,
					a.in_out,
					a.quantity,
					a.$bundles().$transfers().transferred_at,
					a.any(
						"RANK() OVER (ORDER BY {0}, {1}, {2})",
						a.$bundles().$transfers().transferred_at,
						a.$bundles().$transfers().created_at,
						a.seq).AS("seq")))
			.WHERE(
				a -> a.EXISTS(
					new transient_nodes()
						.SELECT(sa -> sa.any(0))
						.WHERE(
							sa -> sa.$transient_bundles().$transient_transfers().transient_id.eq($UUID),
							sa -> sa.stock_id.eq(a.stock_id),

							//同一時刻のnodeはsnapshotを後で取得した際、織込み済みなので取得しない
							sa -> sa.$transient_bundles().$transient_transfers().transferred_at.lt(a.$bundles().$transfers().transferred_at))))
			.UNION_ALL(
				new transient_nodes()
					.SELECT(
						a -> a.ls(
							a.any(1).AS("node_type"),
							a.stock_id,
							a.grants_unlimited,
							a.in_out,
							a.quantity,
							a.$transient_bundles().$transient_transfers().transferred_at,
							a.any(
								"RANK() OVER (ORDER BY {0}, {1})",
								a.$transient_bundles().$transient_transfers().transferred_at,
								a.$transient_bundles().$transient_transfers().seq) //transferred_atが同一であれば生成順
								.AS("seq")))
					.WHERE(a -> a.$transient_bundles().$transient_transfers().transient_id.eq($UUID)))
			.ORDER_BY(
				a -> a.ls(
					a.any("stock_id"),
					a.any("transferred_at"),
					a.any("node_type"), //transient_nodesよりnodesが先
					a.any("seq")));

		return new AnonymousTable(inner, "unioned_nodes")
			.LEFT_OUTER_JOIN(
				new closed_stocks().SELECT(a -> a.$closings().closed_at))
			.ON((l, r) -> l.col("stock_id").eq(r.id));
	}
}
