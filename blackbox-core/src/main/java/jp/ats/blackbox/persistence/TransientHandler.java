package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$UUID;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
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
import jp.ats.blackbox.executor.TagExecutor;
import jp.ats.blackbox.persistence.StockHandler.StockComponents;
import jp.ats.blackbox.persistence.TransferHandler.BundleRegisterRequest;
import jp.ats.blackbox.persistence.TransferHandler.NodeRegisterRequest;
import jp.ats.blackbox.persistence.TransferHandler.TransferRegisterRequest;
import sqlassist.bb.closed_stocks;
import sqlassist.bb.nodes;
import sqlassist.bb.stocks;
import sqlassist.bb.transient_bundles;
import sqlassist.bb.transient_nodes;
import sqlassist.bb.transient_transfers;
import sqlassist.bb.transients;

public class TransientHandler {

	private static final int sequenceIncrement = 100;

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

		@Override
		public void setTransfer_batch_id(UUID id) {}
	}

	public static UUID registerTransfer(UUID transientId, TransferRegisterRequest request) {
		var id = UUID.randomUUID();

		var transfer = new Transfer();

		var userId = SecurityValues.currentUserId();

		TransferPreparer.prepareTransfer(
			id,
			U.NULL_ID,
			U.NULL_ID,
			userId,
			request,
			new Timestamp(System.currentTimeMillis()),
			transfer,
			U.recorder);

		transfer.setTransient_id(transientId);
		transfer.setUpdated_by(userId);

		transfer.insert();

		request.tags.ifPresent(tags -> TagExecutor.stickTags(tags, id, transient_transfers.$TABLE));

		for (int i = 0; i < request.bundles.length; i++) {
			registerBundle(userId, id, request.bundles[i], (i + 1) * sequenceIncrement);
		}

		return id;
	}

	public static void deleteTransfer(UUID transferId) {
		U.recorder.play(() -> new transient_transfers().DELETE().WHERE(a -> a.id.eq($UUID)), transferId).execute();
	}

	private static int computeNextSeq(int currentMaxSeq) {
		return currentMaxSeq - currentMaxSeq % sequenceIncrement + sequenceIncrement;
	}

	private static class Bundle extends transient_bundles.Row implements TransferPreparer.Bundle {

		@Override
		public void setTransfer_id(UUID transferId) {
			super.setTransient_transfer_id(transferId);
		}
	}

	public static UUID registerBundle(UUID transferId, BundleRegisterRequest request) {
		int seq = U.recorder.play(
			() -> new transient_bundles()
				.SELECT(a -> a.MAX(a.seq_in_transfer))
				.WHERE(a -> a.transient_transfer_id.eq($UUID)),
			transferId)
			.aggregateAndGet(r -> {
				r.next();
				return r.getInt(1);
			});

		return registerBundle(SecurityValues.currentUserId(), transferId, request, computeNextSeq(seq));
	}

	public static UUID registerBundle(UUID transferId, BundleRegisterRequest request, int seq) {
		return registerBundle(SecurityValues.currentUserId(), transferId, request, seq);
	}

	private static UUID registerBundle(UUID userId, UUID transferId, BundleRegisterRequest request, int seq) {
		var bundle = new Bundle();

		UUID id = UUID.randomUUID();

		TransferPreparer.prepareBundle(transferId, id, request, bundle);

		bundle.setSeq_in_transfer(seq);
		bundle.setCreated_by(userId);
		bundle.setUpdated_by(userId);

		bundle.insert();

		for (int i = 0; i < request.nodes.length; i++) {
			registerNode(userId, id, request.nodes[i], (i + 1) * sequenceIncrement);
		}

		return id;
	}

	public static void deleteBundle(UUID bundleId) {
		U.recorder.play(() -> new transient_bundles().DELETE().WHERE(a -> a.id.eq($UUID)), bundleId).execute();
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

	public static UUID registerNode(UUID bundleId, NodeRegisterRequest request) {
		int seq = U.recorder.play(
			() -> new transient_nodes()
				.SELECT(a -> a.MAX(a.seq_in_bundle))
				.WHERE(a -> a.transient_bundle_id.eq($UUID)),
			bundleId)
			.aggregateAndGet(r -> {
				r.next();
				return r.getInt(1);
			});

		return registerNode(SecurityValues.currentUserId(), bundleId, request, computeNextSeq(seq));
	}

	public static UUID registerNode(UUID bundleId, NodeRegisterRequest request, int seq) {
		return registerNode(SecurityValues.currentUserId(), bundleId, request, seq);
	}

	private static UUID registerNode(UUID userId, UUID bundleId, NodeRegisterRequest request, int seq) {
		var node = new Node();

		UUID id = UUID.randomUUID();

		TransferPreparer.prepareNode(bundleId, id, userId, request, node, seq, U.recorder);

		node.setSeq_in_bundle(seq);
		node.setCreated_by(userId);
		node.setUpdated_by(userId);

		node.insert();

		return id;
	}

	public static void deleteNode(UUID nodeId) {
		U.recorder.play(() -> new transient_nodes().DELETE().WHERE(a -> a.id.eq($UUID)), nodeId).execute();
	}

	public static class TransientMoveRequest {

		public UUID transient_id;
	}

	public static TransientMoveResult move(UUID batchId, UUID userId, TransientMoveRequest request, Recorder recorder) {
		var transferHandler = new TransferHandler(recorder);

		var result = new TransientMoveResult();

		transferHandler.registerBatch(batchId, userId);

		buildTransferRegisterRequests(request.transient_id, recorder).forEach(r -> {
			var transferId = UUID.randomUUID();
			transferHandler.register(transferId, batchId, userId, r);

			result.transferIds.add(transferId);
			result.compareAndChange(r.transferred_at);
		});

		return result;
	}

	public static class TransientMoveResult {

		public final List<UUID> transferIds = new LinkedList<>();

		public Timestamp firstTransferredAt;

		private void compareAndChange(Timestamp transferredAt) {
			if (firstTransferredAt == null) {
				firstTransferredAt = transferredAt;
			} else {
				firstTransferredAt = firstTransferredAt.getTime() < transferredAt.getTime() ? firstTransferredAt : transferredAt;
			}
		}
	}

	private static List<TransferRegisterRequest> buildTransferRegisterRequests(UUID transientId, Recorder recorder) {
		var list = new LinkedList<TransferRegisterRequest>();

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
				.WHERE(a -> a.$transient_bundles().$transient_transfers().transient_id.eq($UUID))
				.ORDER_BY(
					a -> a.ls(
						a.$transient_bundles().$transient_transfers().seq,
						a.$transient_bundles().seq_in_transfer,
						a.seq_in_bundle))
				.assist()
				.$transient_bundles()
				.$transient_transfers()
				.intercept(),
			transientId)
			.forEach(transferOne -> {
				var transfer = transferOne.get();

				var request = new TransferRegisterRequest();

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
						nodeRequest.in_out = InOut.of(node.getIn_out());
						nodeRequest.quantity = node.getQuantity();
						nodeRequest.grants_unlimited = Optional.of(node.getGrants_unlimited());
						nodeRequest.restored_extension = Optional.of(node.getExtension());

						nodes.add(nodeRequest);
					});

					bundleRequest.nodes = nodes.toArray(new NodeRegisterRequest[nodes.size()]);
				});

				request.bundles = bundles.toArray(new BundleRegisterRequest[bundles.size()]);

				list.add(request);
			});

		return list;
	}

	public static void check(UUID transientId, BiConsumer<ErrorType, CheckContext> consumer) {
		U.recorder.play(() -> build(), transientId, transientId).aggregate(r -> checkInternal(r, consumer));
	}

	private static void checkInternal(BResultSet result, BiConsumer<ErrorType, CheckContext> consumer) {
		UUID currentStockId = null;

		BigDecimal currentStockTotal = null;
		boolean currentStockUnlimited = false;

		boolean skip = false;

		while (result.next()) {
			var context = new CheckContext(result);

			if (!context.stock_id.equals(currentStockId)) {
				skip = false;

				currentStockId = context.stock_id;

				//直近のsnapshotを取得
				var snapshot = TransferHandler.getJustBeforeSnapshot(currentStockId, context.transferred_at, U.recorder);
				currentStockTotal = snapshot.total;
				currentStockUnlimited = snapshot.unlimited;
			}

			//次のstockまでスキップ
			if (skip) continue;

			if (context.closed_at != null && context.transferred_at.getTime() <= context.closed_at.getTime()) {
				consumer.accept(ErrorType.CLOSED_STOCK, context);
				skip = true;
				continue;
			}

			//すでに無制限と判明していれば
			if (currentStockUnlimited) continue;

			currentStockUnlimited = currentStockUnlimited | context.grants_unlimited;

			currentStockTotal = context.in_out.calcurate(currentStockTotal, context.quantity);

			if (!currentStockUnlimited && currentStockTotal.compareTo(BigDecimal.ZERO) < 0) {
				consumer.accept(ErrorType.MINUS_TOTAL, context);
				skip = true;
			}
		}
	}

	public static enum ErrorType {

		CLOSED_STOCK,

		MINUS_TOTAL;
	}

	public static enum NodeType {

		NODE,

		TRANSIENT_NODE;

		public static NodeType of(int type) {
			return NodeType.values()[type];
		}
	}

	public static class CheckContext {

		public final NodeType node_type;

		public final UUID node_or_transient_node_id;

		public final UUID stock_id;

		public final boolean grants_unlimited;

		public final InOut in_out;

		public final BigDecimal quantity;

		public final Timestamp transferred_at;

		public final Timestamp closed_at;

		private CheckContext(Result result) {
			node_type = NodeType.of(result.getInt("node_type"));
			node_or_transient_node_id = U.uuid(result, "id");
			stock_id = U.uuid(result, "stock_id");
			grants_unlimited = result.getBoolean("grants_unlimited");
			in_out = InOut.of(result.getInt("in_out"));
			quantity = result.getBigDecimal("quantity");
			transferred_at = result.getTimestamp("transferred_at");
			closed_at = result.getTimestamp("closed_at");
		}
	}

	private static AnonymousTable build() {
		var inner = new nodes()
			.SELECT(
				a -> a.ls(
					a.any(NodeType.NODE.ordinal()).AS("node_type"),
					a.id,
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
							a.any(NodeType.TRANSIENT_NODE.ordinal()).AS("node_type"),
							a.id,
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

	/**
	 * transient_transfer更新に必要な情報クラス
	 */
	public static class TransferUpdateRequest {

		public UUID transfer_id;

		public long revision;

		/**
		 * このtransferが属するtransient<br>
		 * 指定することでtransient間を移動
		 */
		public Optional<UUID> transient_id = Optional.empty();

		/**
		 * このtransferが属するグループ
		 */
		public Optional<UUID> group_id = Optional.empty();

		/**
		 * 移動時刻
		 */
		public Optional<Timestamp> transferred_at = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> extension = Optional.empty();

		/**
		 * 検索用タグ
		 */
		public Optional<String[]> tags = Optional.empty();

		/**
		 * 追加bundle
		 */
		public BundleRegisterRequest[] registerBundles = {};

		/**
		 * 更新bundle
		 */
		public BundleUpdateRequest[] updateBundles = {};
	}

	public static void updateTransfer(TransferUpdateRequest request) {
		int result = new transient_transfers().UPDATE(a -> {
			a.revision.set(request.revision + 1);

			request.transient_id.ifPresent(v -> a.transient_id.set(v));
			request.group_id.ifPresent(v -> a.group_id.set(v));
			request.transferred_at.ifPresent(v -> a.transferred_at.set(v));
			request.extension.ifPresent(v -> a.extension.set(JsonHelper.toJson(v)));
			request.tags.ifPresent(v -> a.tags.set((Object) v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.transfer_id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(transient_transfers.$TABLE, request.transfer_id);

		Arrays.stream(request.registerBundles).forEach(r -> registerBundle(request.transfer_id, r));

		Arrays.stream(request.updateBundles).forEach(r -> updateBundle(r));
	}

	/**
	 * transient_bundle更新に必要な情報クラス
	 */
	public static class BundleUpdateRequest {

		public UUID bundle_id;

		public long revision;

		/**
		 * このbundleが属するtransfer<br>
		 * 指定することでtransfer間を移動
		 */
		public Optional<UUID> transfer_id = Optional.empty();

		public Optional<Integer> seq_in_transfer = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> extension = Optional.empty();

		/**
		 * 追加node
		 */
		public NodeRegisterRequest[] registerNodes = {};

		/**
		 * 更新node
		 */
		public NodeUpdateRequest[] updateNodes = {};
	}

	public static void updateBundle(BundleUpdateRequest request) {
		int result = new transient_bundles().UPDATE(a -> {
			a.revision.set(request.revision + 1);

			request.transfer_id.ifPresent(v -> a.transient_transfer_id.set(v));
			request.seq_in_transfer.ifPresent(v -> a.seq_in_transfer.set(v));
			request.extension.ifPresent(v -> a.extension.set(JsonHelper.toJson(v)));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.bundle_id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(transient_bundles.$TABLE, request.bundle_id);

		Arrays.stream(request.registerNodes).forEach(r -> registerNode(request.bundle_id, r));

		Arrays.stream(request.updateNodes).forEach(r -> updateNode(r));
	}

	/**
	 * node更新に必要な情報クラス
	 */
	public static class NodeUpdateRequest {

		public UUID node_id;

		public long revision;

		/**
		 * このnodeが属するbundle<br>
		 * 指定することでbundle間を移動
		 */
		public Optional<UUID> bundle_id = Optional.empty();

		public Optional<Integer> seq_in_bundle = Optional.empty();

		/**
		 * stockの所属するグループ
		 * stockに格納される
		 */
		public Optional<UUID> group_id = Optional.empty();

		/**
		 * stockのitem
		 * stockに格納される
		 */
		public Optional<UUID> item_id = Optional.empty();

		/**
		 * stockのowner
		 * stockに格納される
		 */
		public Optional<UUID> owner_id = Optional.empty();

		/**
		 * stockのlocation
		 * stockに格納される
		 */
		public Optional<UUID> location_id = Optional.empty();

		/**
		 * stockのstatus
		 * stockに格納される
		 */
		public Optional<UUID> status_id = Optional.empty();

		/**
		 * 入出庫タイプ
		 */
		public Optional<InOut> in_out = Optional.empty();

		/**
		 * 移動数量
		 */
		public Optional<BigDecimal> quantity = Optional.empty();

		/**
		 * これ以降在庫無制限を設定するか
		 * 在庫無制限の場合、通常はstock登録時からtrueにしておく
		 */
		public Optional<Boolean> grants_unlimited = Optional.empty();

		/**
		 * 移動数量
		 */
		public Optional<String> extension = Optional.empty();
	}

	public static void updateNode(NodeUpdateRequest request) {
		UUID stockId = null;
		if (request.group_id.isPresent()
			|| request.item_id.isPresent()
			|| request.owner_id.isPresent()
			|| request.location_id.isPresent()
			|| request.status_id.isPresent()) {
			var stock = U.recorder.play(
				() -> new nodes().SELECT(
					a -> a.ls(
						a.$stocks().group_id,
						a.$stocks().item_id,
						a.$stocks().owner_id,
						a.$stocks().location_id,
						a.$stocks().status_id)))
				.fetch(request.node_id)
				.get()
				.$stocks();

			stockId = StockHandler.prepareStock(
				() -> new stocks().SELECT(a -> a.id),
				SecurityValues.currentUserId(),
				new StockComponents() {

					@Override
					public UUID groupId() {
						return request.group_id.orElseGet(() -> stock.getGroup_id());
					}

					@Override
					public UUID itemId() {
						return request.item_id.orElseGet(() -> stock.getItem_id());
					}

					@Override
					public UUID ownerId() {
						return request.owner_id.orElseGet(() -> stock.getOwner_id());
					}

					@Override
					public UUID locationId() {
						return request.location_id.orElseGet(() -> stock.getLocation_id());
					}

					@Override
					public UUID statusId() {
						return request.status_id.orElseGet(() -> stock.getStatus_id());
					}
				},
				U.recorder).getId();
		}

		final var safeStockId = stockId;

		int result = new transient_nodes().UPDATE(a -> {
			a.revision.set(request.revision + 1);

			request.bundle_id.ifPresent(v -> a.transient_bundle_id.set(v));
			request.seq_in_bundle.ifPresent(v -> a.seq_in_bundle.set(v));

			if (safeStockId != null) a.stock_id.set(safeStockId);

			request.in_out.ifPresent(v -> a.in_out.set(v.intValue));
			request.quantity.ifPresent(v -> a.quantity.set(v));
			request.grants_unlimited.ifPresent(v -> a.grants_unlimited.set(v));
			request.extension.ifPresent(v -> a.extension.set(JsonHelper.toJson(v)));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.node_id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(transient_bundles.$TABLE, request.node_id);
	}
}
