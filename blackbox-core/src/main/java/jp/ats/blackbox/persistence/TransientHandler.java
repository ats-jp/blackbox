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
import jp.ats.blackbox.persistence.JournalHandler.DetailRegisterRequest;
import jp.ats.blackbox.persistence.JournalHandler.NodeRegisterRequest;
import jp.ats.blackbox.persistence.JournalHandler.JournalRegisterRequest;
import sqlassist.bb.closed_units;
import sqlassist.bb.nodes;
import sqlassist.bb.transient_details;
import sqlassist.bb.transient_journals;
import sqlassist.bb.transient_nodes;
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

	private static class Journal extends transient_journals.Row implements JournalPreparer.Journal {

		@Override
		public void setDenied_id(UUID id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDeny_reason(String reason) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setGroup_props(Object json) {}

		@Override
		public void setOrg_props(Object json) {}

		@Override
		public void setUser_props(Object json) {}

		@Override
		public void setInstance_id(UUID id) {}

		@Override
		public void setJournal_batch_id(UUID id) {}
	}

	public static UUID registerJournal(UUID transientId, JournalRegisterRequest request) {
		var id = UUID.randomUUID();

		var journal = new Journal();

		var userId = SecurityValues.currentUserId();

		JournalPreparer.prepareJournal(
			id,
			U.NULL_ID,
			U.NULL_ID,
			userId,
			request,
			new Timestamp(System.currentTimeMillis()),
			journal,
			U.recorder);

		journal.setTransient_id(transientId);
		journal.setUpdated_by(userId);

		journal.insert();

		request.tags.ifPresent(tags -> TagExecutor.stickTags(tags, id, transient_journals.$TABLE));

		for (int i = 0; i < request.details.length; i++) {
			registerDetail(userId, id, request.details[i], (i + 1) * sequenceIncrement);
		}

		return id;
	}

	public static void deleteJournal(UUID journalId) {
		U.recorder.play(() -> new transient_journals().DELETE().WHERE(a -> a.id.eq($UUID)), journalId).execute();
	}

	private static int computeNextSeq(int currentMaxSeq) {
		return currentMaxSeq - currentMaxSeq % sequenceIncrement + sequenceIncrement;
	}

	private static class Detail extends transient_details.Row implements JournalPreparer.Detail {

		@Override
		public void setJournal_id(UUID journalId) {
			super.setTransient_journal_id(journalId);
		}
	}

	public static UUID registerDetail(UUID journalId, DetailRegisterRequest request) {
		int seq = U.recorder.play(
			() -> new transient_details()
				.SELECT(a -> a.MAX(a.seq_in_journal))
				.WHERE(a -> a.transient_journal_id.eq($UUID)),
			journalId)
			.aggregateAndGet(r -> {
				r.next();
				return r.getInt(1);
			});

		return registerDetail(SecurityValues.currentUserId(), journalId, request, computeNextSeq(seq));
	}

	public static UUID registerDetail(UUID journalId, DetailRegisterRequest request, int seq) {
		return registerDetail(SecurityValues.currentUserId(), journalId, request, seq);
	}

	private static UUID registerDetail(UUID userId, UUID journalId, DetailRegisterRequest request, int seq) {
		var detail = new Detail();

		UUID id = UUID.randomUUID();

		JournalPreparer.prepareDetail(journalId, id, request, detail);

		detail.setSeq_in_journal(seq);
		detail.setCreated_by(userId);
		detail.setUpdated_by(userId);

		detail.insert();

		for (int i = 0; i < request.nodes.length; i++) {
			registerNode(userId, id, request.nodes[i], (i + 1) * sequenceIncrement);
		}

		return id;
	}

	public static void deleteDetail(UUID detailId) {
		U.recorder.play(() -> new transient_details().DELETE().WHERE(a -> a.id.eq($UUID)), detailId).execute();
	}

	private static class Node extends transient_nodes.Row implements JournalPreparer.Node {

		@Override
		public void setDetail_id(UUID detailId) {
			super.setTransient_detail_id(detailId);
		}

		@Override
		public void setSeq(Integer seq) {}

		@Override
		public void setUnit_props(Object json) {}
	}

	public static UUID registerNode(UUID detailId, NodeRegisterRequest request) {
		int seq = U.recorder.play(
			() -> new transient_nodes()
				.SELECT(a -> a.MAX(a.seq_in_detail))
				.WHERE(a -> a.transient_detail_id.eq($UUID)),
			detailId)
			.aggregateAndGet(r -> {
				r.next();
				return r.getInt(1);
			});

		return registerNode(SecurityValues.currentUserId(), detailId, request, computeNextSeq(seq));
	}

	public static UUID registerNode(UUID detailId, NodeRegisterRequest request, int seq) {
		return registerNode(SecurityValues.currentUserId(), detailId, request, seq);
	}

	private static UUID registerNode(UUID userId, UUID detailId, NodeRegisterRequest request, int seq) {
		var node = new Node();

		UUID id = UUID.randomUUID();

		JournalPreparer.prepareNode(detailId, id, userId, request, node, seq, U.recorder);

		node.setSeq_in_detail(seq);
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
		var journalHandler = new JournalHandler(recorder);

		var result = new TransientMoveResult();

		journalHandler.registerBatch(batchId, userId);

		buildJournalRegisterRequests(request.transient_id, recorder).forEach(r -> {
			var journalId = UUID.randomUUID();
			journalHandler.register(journalId, batchId, userId, r);

			result.journalIds.add(journalId);
			result.compareAndChange(r.fixed_at);
		});

		return result;
	}

	public static class TransientMoveResult {

		public final List<UUID> journalIds = new LinkedList<>();

		public Timestamp firstFixedAt;

		private void compareAndChange(Timestamp fixedAt) {
			if (firstFixedAt == null) {
				firstFixedAt = fixedAt;
			} else {
				firstFixedAt = firstFixedAt.getTime() < fixedAt.getTime() ? firstFixedAt : fixedAt;
			}
		}
	}

	private static List<JournalRegisterRequest> buildJournalRegisterRequests(UUID transientId, Recorder recorder) {
		var list = new LinkedList<JournalRegisterRequest>();

		var details = new LinkedList<DetailRegisterRequest>();

		recorder.play(
			() -> new transient_nodes().selectClause(
				a -> {
					var detailsAssist = a.$transient_details();
					var journalsAssist = detailsAssist.$transient_journals();

					a.SELECT(
						journalsAssist.group_id,
						journalsAssist.fixed_at,
						journalsAssist.props,
						journalsAssist.tags,
						detailsAssist.props,
						a.unit_id,
						a.in_out,
						a.quantity,
						a.grants_unlimited,
						a.props);
				})
				.WHERE(a -> a.$transient_details().$transient_journals().transient_id.eq($UUID))
				.ORDER_BY(
					a -> a.ls(
						a.$transient_details().$transient_journals().seq,
						a.$transient_details().seq_in_journal,
						a.seq_in_detail))
				.assist()
				.$transient_details()
				.$transient_journals()
				.intercept(),
			transientId)
			.forEach(journalOne -> {
				var journal = journalOne.get();

				var request = new JournalRegisterRequest();

				request.group_id = journal.getGroup_id();
				request.fixed_at = journal.getFixed_at();
				request.restored_props = Optional.of(journal.getProps());

				try {
					request.tags = Optional.of(Utils.restoreTags(journal.getTags()));
				} catch (SQLException e) {
					throw new BSQLException(e);
				}

				journalOne.many().forEach(detailOne -> {
					var nodes = new LinkedList<NodeRegisterRequest>();

					var detailRequest = new DetailRegisterRequest();

					detailRequest.restored_props = Optional.of(detailOne.get().getProps());

					details.add(detailRequest);

					detailOne.many().forEach(nodeOne -> {
						var node = nodeOne.get();

						var nodeRequest = new NodeRegisterRequest();

						nodeRequest.unit_id = node.getUnit_id();
						nodeRequest.in_out = InOut.of(node.getIn_out());
						nodeRequest.quantity = node.getQuantity();
						nodeRequest.grants_unlimited = Optional.of(node.getGrants_unlimited());
						nodeRequest.restored_props = Optional.of(node.getProps());

						nodes.add(nodeRequest);
					});

					detailRequest.nodes = nodes.toArray(new NodeRegisterRequest[nodes.size()]);
				});

				request.details = details.toArray(new DetailRegisterRequest[details.size()]);

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
				var snapshot = JournalHandler.getJustBeforeSnapshot(currentStockId, context.fixed_at, U.recorder);
				currentStockTotal = snapshot.total;
				currentStockUnlimited = snapshot.unlimited;
			}

			//次のstockまでスキップ
			if (skip) continue;

			if (context.closed_at != null && context.fixed_at.getTime() <= context.closed_at.getTime()) {
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

		public final Timestamp fixed_at;

		public final Timestamp closed_at;

		private CheckContext(Result result) {
			node_type = NodeType.of(result.getInt("node_type"));
			node_or_transient_node_id = U.uuid(result, "id");
			stock_id = U.uuid(result, "unit_id");
			grants_unlimited = result.getBoolean("grants_unlimited");
			in_out = InOut.of(result.getInt("in_out"));
			quantity = result.getBigDecimal("quantity");
			fixed_at = result.getTimestamp("fixed_at");
			closed_at = result.getTimestamp("closed_at");
		}
	}

	private static AnonymousTable build() {
		var inner = new nodes()
			.SELECT(
				a -> a.ls(
					a.any(NodeType.NODE.ordinal()).AS("node_type"),
					a.id,
					a.unit_id,
					a.grants_unlimited,
					a.in_out,
					a.quantity,
					a.$details().$journals().fixed_at,
					a.any(
						"RANK() OVER (ORDER BY {0}, {1}, {2})",
						a.$details().$journals().fixed_at,
						a.$details().$journals().created_at,
						a.seq).AS("seq")))
			.WHERE(
				a -> a.EXISTS(
					new transient_nodes()
						.SELECT(sa -> sa.any(0))
						.WHERE(
							sa -> sa.$transient_details().$transient_journals().transient_id.eq($UUID),
							sa -> sa.unit_id.eq(a.unit_id),

							//同一時刻のnodeはsnapshotを後で取得した際、織込み済みなので取得しない
							sa -> sa.$transient_details().$transient_journals().fixed_at.lt(a.$details().$journals().fixed_at))))
			.UNION_ALL(
				new transient_nodes()
					.SELECT(
						a -> a.ls(
							a.any(NodeType.TRANSIENT_NODE.ordinal()).AS("node_type"),
							a.id,
							a.unit_id,
							a.grants_unlimited,
							a.in_out,
							a.quantity,
							a.$transient_details().$transient_journals().fixed_at,
							a.any(
								"RANK() OVER (ORDER BY {0}, {1})",
								a.$transient_details().$transient_journals().fixed_at,
								a.$transient_details().$transient_journals().seq) //fixed_atが同一であれば生成順
								.AS("seq")))
					.WHERE(a -> a.$transient_details().$transient_journals().transient_id.eq($UUID)))
			.ORDER_BY(
				a -> a.ls(
					a.any("unit_id"),
					a.any("fixed_at"),
					a.any("node_type"), //transient_nodesよりnodesが先
					a.any("seq")));

		return new AnonymousTable(inner, "unioned_nodes")
			.LEFT_OUTER_JOIN(
				new closed_units().SELECT(a -> a.$closings().closed_at))
			.ON((l, r) -> l.col("unit_id").eq(r.id));
	}

	/**
	 * transient_更新に必要な情報クラス
	 */
	public static class JournalUpdateRequest {

		public UUID journal_id;

		public long revision;

		/**
		 * このが属するtransient<br>
		 * 指定することでtransient間を移動
		 */
		public Optional<UUID> transient_id = Optional.empty();

		/**
		 * このjournalが属するグループ
		 */
		public Optional<UUID> group_id = Optional.empty();

		/**
		 * 確定時刻
		 */
		public Optional<Timestamp> fixed_at = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> props = Optional.empty();

		/**
		 * 検索用タグ
		 */
		public Optional<String[]> tags = Optional.empty();

		/**
		 * 追加detail
		 */
		public DetailRegisterRequest[] registerDetails = {};

		/**
		 * 更新detail
		 */
		public DetailUpdateRequest[] updateDetails = {};
	}

	public static void updateJournal(JournalUpdateRequest request) {
		int result = new transient_journals().UPDATE(a -> {
			a.revision.set(request.revision + 1);

			request.transient_id.ifPresent(v -> a.transient_id.set(v));
			request.group_id.ifPresent(v -> a.group_id.set(v));
			request.fixed_at.ifPresent(v -> a.fixed_at.set(v));
			request.props.ifPresent(v -> a.props.set(JsonHelper.toJson(v)));
			request.tags.ifPresent(v -> a.tags.set((Object) v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.journal_id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(transient_journals.$TABLE, request.journal_id);

		Arrays.stream(request.registerDetails).forEach(r -> registerDetail(request.journal_id, r));

		Arrays.stream(request.updateDetails).forEach(r -> updateDetail(r));
	}

	/**
	 * transient_detail更新に必要な情報クラス
	 */
	public static class DetailUpdateRequest {

		public UUID detail_id;

		public long revision;

		/**
		 * このdetailが属するjournal<br>
		 * 指定することでjournal間を移動
		 */
		public Optional<UUID> journal_id = Optional.empty();

		public Optional<Integer> seq_in_journal = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> props = Optional.empty();

		/**
		 * 追加node
		 */
		public NodeRegisterRequest[] registerNodes = {};

		/**
		 * 更新node
		 */
		public NodeUpdateRequest[] updateNodes = {};
	}

	public static void updateDetail(DetailUpdateRequest request) {
		int result = new transient_details().UPDATE(a -> {
			a.revision.set(request.revision + 1);

			request.journal_id.ifPresent(v -> a.transient_journal_id.set(v));
			request.seq_in_journal.ifPresent(v -> a.seq_in_journal.set(v));
			request.props.ifPresent(v -> a.props.set(JsonHelper.toJson(v)));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.detail_id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(transient_details.$TABLE, request.detail_id);

		Arrays.stream(request.registerNodes).forEach(r -> registerNode(request.detail_id, r));

		Arrays.stream(request.updateNodes).forEach(r -> updateNode(r));
	}

	/**
	 * node更新に必要な情報クラス
	 */
	public static class NodeUpdateRequest {

		public UUID node_id;

		public long revision;

		/**
		 * このnodeが属するdetail<br>
		 * 指定することでdetail間を移動
		 */
		public Optional<UUID> detail_id = Optional.empty();

		public Optional<Integer> seq_in_detail = Optional.empty();

		public Optional<UUID> unit_id = Optional.empty();

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
		public Optional<String> props = Optional.empty();
	}

	public static void updateNode(NodeUpdateRequest request) {
		int result = new transient_nodes().UPDATE(a -> {
			a.revision.set(request.revision + 1);

			request.detail_id.ifPresent(v -> a.transient_detail_id.set(v));
			request.seq_in_detail.ifPresent(v -> a.seq_in_detail.set(v));

			request.unit_id.ifPresent(v -> a.unit_id.set(v));

			request.in_out.ifPresent(v -> a.in_out.set(v.intValue));
			request.quantity.ifPresent(v -> a.quantity.set(v));
			request.grants_unlimited.ifPresent(v -> a.grants_unlimited.set(v));
			request.props.ifPresent(v -> a.props.set(JsonHelper.toJson(v)));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.node_id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(transient_details.$TABLE, request.node_id);
	}
}
