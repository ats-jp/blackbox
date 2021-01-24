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
import java.util.function.Consumer;

import org.blendee.assist.AnonymousTable;
import org.blendee.jdbc.BResultSet;
import org.blendee.jdbc.BSQLException;
import org.blendee.jdbc.Result;
import org.blendee.sql.Recorder;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.TagExecutor;
import jp.ats.blackbox.persistence.Requests.DetailRegisterRequest;
import jp.ats.blackbox.persistence.Requests.JournalRegisterRequest;
import jp.ats.blackbox.persistence.Requests.NodeRegisterRequest;
import jp.ats.blackbox.persistence.Requests.TransientMoveRequest;
import sqlassist.bb.closed_units;
import sqlassist.bb.nodes;
import sqlassist.bb.transient_details;
import sqlassist.bb.transient_journals;
import sqlassist.bb.transient_nodes;
import sqlassist.bb.transients;

public class TransientHandler {

	public static class RegisterRequest {

		public UUID group_id;

		public Optional<String> description = Optional.empty();

		public Optional<UUID> user_id = Optional.empty();
	}

	public static UUID register(RegisterRequest request) {
		var handler = SeqHandler.getInstance();

		var groupRequest = new SeqHandler.Request();
		groupRequest.table = transients.$TABLE;
		groupRequest.dependsColumn = transients.group_id;
		groupRequest.dependsId = request.group_id;
		groupRequest.seqColumn = transients.seq_in_group;

		return handler.nextSeqAndGet(groupRequest, seqInGroup -> {
			var userRequest = new SeqHandler.Request();
			userRequest.table = transients.$TABLE;
			userRequest.dependsColumn = transients.user_id;
			userRequest.dependsId = request.user_id.orElseGet(() -> SecurityValues.currentUserId());
			userRequest.seqColumn = transients.seq_in_user;

			return handler.nextSeqAndGet(userRequest, seqInUser -> registerInternal(request, seqInGroup, seqInUser));
		});
	}

	private static UUID registerInternal(RegisterRequest request, long seqInGroup, long seqInUser) {
		var id = UUID.randomUUID();

		var userId = SecurityValues.currentUserId();

		var row = transients.row();

		row.setId(id);
		row.setGroup_id(request.group_id);
		row.setSeq_in_group(seqInGroup);
		row.setUser_id(request.user_id.orElse(userId));
		row.setSeq_in_user(seqInUser);
		request.description.ifPresent(v -> row.setDescription(v));
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		return id;
	}

	public static void updateDescription(UUID transientId, String description, long revision) {
		int result = new transients().UPDATE(a -> {
			a.description.set(description);
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(transientId).AND.revision.eq(revision)).execute();

		if (result != 1) throw Utils.decisionException(transients.$TABLE, transientId);
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
		public void setGroup_props(Object json) {
		}

		@Override
		public void setOrg_props(Object json) {
		}

		@Override
		public void setUser_props(Object json) {
		}

		@Override
		public void setGroup_revision(Long revision) {
		}

		@Override
		public void setOrg_revision(Long revision) {
		}

		@Override
		public void setUser_revision(Long revision) {
		}

		@Override
		public void setGroup_tree_revision(Long revision) {
		}

		@Override
		public void setInstance_id(UUID id) {
		}

		@Override
		public void setJournal_batch_id(UUID id) {
		}
	}

	public static UUID registerJournal(long transientRevision, UUID transientId, JournalRegisterRequest request) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = transient_journals.$TABLE;
		seqRequest.dependsColumn = transient_journals.transient_id;
		seqRequest.dependsId = transientId;
		seqRequest.seqColumn = transient_journals.seq_in_transient;

		return SeqHandler.getInstance().nextSeqAndGet(seqRequest, seq -> registerJournalInternal(transientRevision, transientId, seq, request));
	}

	private static UUID registerJournalInternal(long transientRevision, UUID transientId, long seqInTransient, JournalRegisterRequest request) {
		Utils.updateRevision(transients.$TABLE, transientRevision, transientId);

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
		journal.setSeq_in_transient(seqInTransient);
		journal.setUpdated_by(userId);

		journal.insert();

		request.tags.ifPresent(tags -> TagExecutor.stickTags(tags, id, transient_journals.$TABLE));

		for (int i = 0; i < request.details.length; i++) {
			registerDetail(id, request.details[i], SeqInJournalUtils.compute(i));
		}

		return id;
	}

	public static void deleteJournal(long transientRevision, UUID journalId) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_journals().SELECT(a -> a.$transients().id).WHERE(a -> a.id.eq(journalId)));

		U.recorder.play(() -> new transient_journals().DELETE().WHERE(a -> a.id.eq($UUID)), journalId).execute();
	}

	private static class Detail extends transient_details.Row implements JournalPreparer.Detail {

		@Override
		public void setJournal_id(UUID journalId) {
			super.setTransient_journal_id(journalId);
		}
	}

	public static UUID registerDetail(long transientRevision, UUID journalId, DetailRegisterRequest request) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_journals().SELECT(a -> a.$transients().id).WHERE(a -> a.id.eq(journalId)));

		return registerDetail(journalId, request);
	}

	private static UUID registerDetail(UUID journalId, DetailRegisterRequest request) {
		int seq = U.recorder.play(
			() -> new transient_details()
				.SELECT(a -> a.MAX(a.seq_in_journal))
				.WHERE(a -> a.transient_journal_id.eq($UUID)),
			journalId)
			.executeAndGet(r -> {
				r.next();
				return r.getInt(1);
			});

		return registerDetail(journalId, request, SeqInJournalUtils.computeNextSeq(seq));
	}

	public static UUID registerDetail(long transientRevision, UUID journalId, DetailRegisterRequest request, int seq) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_journals().SELECT(a -> a.$transients().id).WHERE(a -> a.id.eq(journalId)));

		return registerDetail(journalId, request, seq);
	}

	private static UUID registerDetail(UUID journalId, DetailRegisterRequest request, int seq) {
		var detail = new Detail();

		var id = UUID.randomUUID();

		var userId = SecurityValues.currentUserId();

		JournalPreparer.prepareDetail(journalId, id, request, detail);

		detail.setSeq_in_journal(seq);
		detail.setCreated_by(userId);
		detail.setUpdated_by(userId);

		detail.insert();

		for (int i = 0; i < request.nodes.length; i++) {
			registerNode(id, request.nodes[i], SeqInJournalUtils.compute(i));
		}

		return id;
	}

	public static void deleteDetail(long transientRevision, UUID detailId) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_details().SELECT(a -> a.$transient_journals().$transients().id).WHERE(a -> a.id.eq(detailId)));

		deleteDetail(detailId);
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
		public void setSeq(Integer seq) {
		}

		@Override
		public void setUnit_props(Object json) {
		}

		@Override
		public void setUnit_group_revision(Long revision) {
		}
	}

	public static UUID registerNode(long transientRevision, UUID detailId, NodeRegisterRequest request) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_details().SELECT(a -> a.$transient_journals().$transients().id).WHERE(a -> a.id.eq(detailId)));

		return registerNode(detailId, request);
	}

	private static UUID registerNode(UUID detailId, NodeRegisterRequest request) {
		int seq = U.recorder.play(
			() -> new transient_nodes()
				.SELECT(a -> a.MAX(a.seq_in_detail))
				.WHERE(a -> a.transient_detail_id.eq($UUID)),
			detailId)
			.executeAndGet(r -> {
				r.next();
				return r.getInt(1);
			});

		return registerNode(detailId, request, SeqInJournalUtils.computeNextSeq(seq));
	}

	public static UUID registerNode(long transientRevision, UUID detailId, NodeRegisterRequest request, int seq) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_details().SELECT(a -> a.$transient_journals().$transients().id).WHERE(a -> a.id.eq(detailId)));

		return registerNode(detailId, request, seq);
	}

	private static UUID registerNode(UUID detailId, NodeRegisterRequest request, int seq) {
		var node = new Node();

		UUID id = UUID.randomUUID();

		JournalPreparer.prepareNode(detailId, id, request, node, seq, U.recorder);

		node.setSeq_in_detail(seq);

		UUID userId = SecurityValues.currentUserId();
		node.setCreated_by(userId);
		node.setUpdated_by(userId);

		node.insert();

		return id;
	}

	public static void deleteNode(long transientRevision, UUID nodeId) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_nodes().SELECT(a -> a.$transient_details().$transient_journals().$transients().id).WHERE(a -> a.id.eq(nodeId)));

		deleteNode(nodeId);
	}

	private static void deleteNode(UUID nodeId) {
		U.recorder.play(() -> new transient_nodes().DELETE().WHERE(a -> a.id.eq($UUID)), nodeId).execute();
	}

	public static TransientMoveResult move(
		UUID batchId,
		UUID userId,
		TransientMoveRequest request,
		Recorder recorder,
		Runnable currentUnitsUpdater) {
		return move(batchId, userId, request, recorder, currentUnitsUpdater, r -> {
		});
	}

	public static TransientMoveResult move(
		UUID batchId,
		UUID userId,
		TransientMoveRequest request,
		Recorder recorder,
		Runnable currentUnitsUpdater,
		Consumer<JournalRegisterRequest> checker) {
		var journalHandler = new JournalHandler(recorder, currentUnitsUpdater);

		var result = new TransientMoveResult();

		journalHandler.registerBatch(batchId, userId);

		var ids = new LinkedList<UUID>();
		var requests = new LinkedList<JournalRegisterRequest>();
		buildJournalRegisterRequests(request.transient_id, recorder).forEach(r -> {
			checker.accept(r);

			var journalId = UUID.randomUUID();

			ids.add(UUID.randomUUID());
			requests.add(r);

			result.journalIds.add(journalId);
			result.compareAndChange(r.fixed_at);
		});

		if (!request.lazy) {
			journalHandler.register(
				ids.toArray(new UUID[ids.size()]),
				batchId,
				userId,
				requests.toArray(new JournalRegisterRequest[requests.size()]));
		} else {
			journalHandler.registerLazily(
				ids.toArray(new UUID[ids.size()]),
				batchId,
				userId,
				requests.toArray(new JournalRegisterRequest[requests.size()]));
		}

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
						a.$transient_details().$transient_journals().seq_in_db,
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

				var details = new LinkedList<DetailRegisterRequest>();

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
		U.recorder.play(() -> build(), transientId, transientId).execute(r -> checkInternal(r, consumer));
	}

	private static void checkInternal(BResultSet result, BiConsumer<ErrorType, CheckContext> consumer) {
		UUID currentUnitId = null;

		BigDecimal currentUnitTotal = null;
		boolean currentUnitUnlimited = false;

		boolean skip = false;

		while (result.next()) {
			var context = new CheckContext(result);

			if (!context.unit_id.equals(currentUnitId)) {
				skip = false;

				currentUnitId = context.unit_id;

				//直近のsnapshotを取得
				var snapshot = JournalHandler.getJustBeforeSnapshot(currentUnitId, context.fixed_at, U.recorder);
				currentUnitTotal = snapshot.total;
				currentUnitUnlimited = snapshot.unlimited;
			}

			//次のunitまでスキップ
			if (skip) continue;

			if (context.closed_at != null && context.fixed_at.getTime() <= context.closed_at.getTime()) {
				consumer.accept(ErrorType.CLOSED_UNIT, context);
				skip = true;
				continue;
			}

			//すでに無制限と判明していれば
			if (currentUnitUnlimited) continue;

			currentUnitUnlimited = currentUnitUnlimited | context.grants_unlimited;

			currentUnitTotal = context.in_out.calcurate(currentUnitTotal, context.quantity);

			if (!currentUnitUnlimited && currentUnitTotal.compareTo(BigDecimal.ZERO) < 0) {
				consumer.accept(ErrorType.MINUS_TOTAL, context);
				skip = true;
			}
		}
	}

	public static enum ErrorType {

		CLOSED_UNIT,

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

		public final UUID unit_id;

		public final boolean grants_unlimited;

		public final InOut in_out;

		public final BigDecimal quantity;

		public final Timestamp fixed_at;

		public final Timestamp closed_at;

		private CheckContext(Result result) {
			node_type = NodeType.of(result.getInt("node_type"));
			node_or_transient_node_id = U.uuid(result, "id");
			unit_id = U.uuid(result, "unit_id");
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
								a.$transient_details().$transient_journals().seq_in_db) //fixed_atが同一であれば生成順
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

		public UUID id;

		/**
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
		 * 補足事項
		 */
		public Optional<String> description = Optional.empty();

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

		/**
		 * 削除details
		 */
		public UUID[] deleteDetails = {};
	}

	public static void updateJournal(long transientRevision, JournalUpdateRequest request) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_journals().SELECT(a -> a.$transients().id).WHERE(a -> a.id.eq(request.id)));

		//移動先があればそちらのrevisionも更新
		request.transient_id.ifPresent(v -> Utils.updateRevision(transients.$TABLE, transientRevision, v));

		int result = new transient_journals().UPDATE(a -> {
			request.transient_id.ifPresent(v -> a.transient_id.set(v));
			request.group_id.ifPresent(v -> a.group_id.set(v));
			request.fixed_at.ifPresent(v -> a.fixed_at.set(v));
			request.description.ifPresent(v -> a.description.set(v));
			request.props.ifPresent(v -> a.props.set(U.toPGObject(v)));
			request.tags.ifPresent(v -> a.tags.set((Object) v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id)).execute();

		if (result != 1) throw Utils.decisionException(transient_journals.$TABLE, request.id);

		request.tags.ifPresent(tags -> TagExecutor.stickTagsAgain(tags, request.id, transient_journals.$TABLE));

		Arrays.stream(request.registerDetails).forEach(r -> registerDetail(request.id, r));

		Arrays.stream(request.updateDetails).forEach(r -> updateDetail(r));

		Arrays.stream(request.deleteDetails).forEach(i -> deleteDetail(i));
	}

	/**
	 * transient_detail更新に必要な情報クラス
	 */
	public static class DetailUpdateRequest {

		public UUID id;

		public long revision;

		/**
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

		/**
		 * 削除node
		 */
		public UUID[] deleteNodes = {};
	}

	public static void updateDetail(long transientRevision, DetailUpdateRequest request) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_details().SELECT(a -> a.$transient_journals().$transients().id).WHERE(a -> a.id.eq(request.id)));

		//移動先があればそちらのrevisionも更新
		request.journal_id.ifPresent(
			v -> Utils.updateRevision(
				transients.$TABLE,
				transientRevision,
				new transient_journals().SELECT(a -> a.$transients().id).WHERE(a -> a.id.eq(v))));

		updateDetail(request);
	}

	private static void updateDetail(DetailUpdateRequest request) {
		int result = new transient_details().UPDATE(a -> {
			request.journal_id.ifPresent(v -> a.transient_journal_id.set(v));
			request.seq_in_journal.ifPresent(v -> a.seq_in_journal.set(v));
			request.props.ifPresent(v -> a.props.set(U.toPGObject(v)));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id)).execute();

		if (result != 1) throw Utils.decisionException(transient_details.$TABLE, request.id);

		Arrays.stream(request.registerNodes).forEach(r -> registerNode(request.id, r));

		Arrays.stream(request.updateNodes).forEach(r -> updateNode(r));

		Arrays.stream(request.deleteNodes).forEach(i -> deleteNode(i));
	}

	/**
	 * node更新に必要な情報クラス
	 */
	public static class NodeUpdateRequest {

		public UUID id;

		/**
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
		 * 在庫無制限の場合、通常はunit登録時からtrueにしておく
		 */
		public Optional<Boolean> grants_unlimited = Optional.empty();

		/**
		 * 移動数量
		 */
		public Optional<String> props = Optional.empty();
	}

	public static void updateNode(long transientRevision, NodeUpdateRequest request) {
		Utils.updateRevision(
			transients.$TABLE,
			transientRevision,
			new transient_nodes().SELECT(a -> a.$transient_details().$transient_journals().$transients().id).WHERE(a -> a.id.eq(request.id)));

		//移動先があればそちらのrevisionも更新
		request.detail_id.ifPresent(
			v -> Utils.updateRevision(
				transients.$TABLE,
				transientRevision,
				new transient_details().SELECT(a -> a.$transient_journals().$transients().id).WHERE(a -> a.id.eq(v))));

		updateNode(request);
	}

	private static void updateNode(NodeUpdateRequest request) {
		int result = new transient_nodes().UPDATE(a -> {

			request.detail_id.ifPresent(v -> a.transient_detail_id.set(v));
			request.seq_in_detail.ifPresent(v -> a.seq_in_detail.set(v));

			request.unit_id.ifPresent(v -> a.unit_id.set(v));

			request.in_out.ifPresent(v -> a.in_out.set(v.intValue));
			request.quantity.ifPresent(v -> a.quantity.set(v));
			request.grants_unlimited.ifPresent(v -> a.grants_unlimited.set(v));
			request.props.ifPresent(v -> a.props.set(U.toPGObject(v)));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id)).execute();

		if (result != 1) throw Utils.decisionException(transient_details.$TABLE, request.id);
	}
}
