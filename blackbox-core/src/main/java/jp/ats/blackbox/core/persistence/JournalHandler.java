package jp.ats.blackbox.core.persistence;

import static org.blendee.sql.Placeholder.$BIGDECIMAL;
import static org.blendee.sql.Placeholder.$BOOLEAN;
import static org.blendee.sql.Placeholder.$STRING;
import static org.blendee.sql.Placeholder.$TIMESTAMP;
import static org.blendee.sql.Placeholder.$UUID;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.blendee.assist.Vargs;
import org.blendee.jdbc.BSQLException;
import org.blendee.jdbc.exception.CheckConstraintViolationException;
import org.blendee.sql.Recorder;

import com.google.gson.Gson;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.executor.TagExecutor;
import jp.ats.blackbox.core.persistence.Requests.DetailRegisterRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalDenyRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalOverwriteRequest;
import jp.ats.blackbox.core.persistence.Requests.JournalRegisterRequest;
import jp.ats.blackbox.core.persistence.Requests.NodeRegisterRequest;
import sqlassist.bb.details;
import sqlassist.bb.jobs;
import sqlassist.bb.journal_batches;
import sqlassist.bb.journals;
import sqlassist.bb.journals_tags;
import sqlassist.bb.nodes;
import sqlassist.bb.snapshots;

/**
 * journal操作クラス
 */
public class JournalHandler {

	private final Recorder recorder;

	private final List<Runnable> plusUpdaterList = new LinkedList<>();

	private final List<Runnable> minusUpdaterList = new LinkedList<>();

	private final SeqHandler.Request seqRequest = new SeqHandler.Request();

	private final Runnable currentUnitsUpdater;

	/**
	 * 処理内で最終的に数量の整合性が取れているのであれば、一時的に数量がマイナスになっても許容するモードを表すフラグ
	 */
	private boolean lazyRegisterMode = false;

	/**
	 * @param recorder
	 * @param currentUnitsUpdater Journalの登録によってそのJournalの先の時点のJournalの総数が更新された場合の処理
	 */
	public JournalHandler(Recorder recorder, Runnable currentUnitsUpdater) {
		this.recorder = recorder;
		this.currentUnitsUpdater = currentUnitsUpdater;
		seqRequest.table = journals.$TABLE;
		seqRequest.dependsColumn = journals.group_id;
	}

	private long time = System.currentTimeMillis();

	private Timestamp uniqueTime() {
		while (true) {
			var current = System.currentTimeMillis();
			if (current == time) {
				Thread.yield();
				continue;
			}

			time = current;

			return new Timestamp(time);
		}
	}

	private UUID instanceId;

	private UUID instanceId() {
		//Blendee初期化前にインスタンスされる場合のための遅延処置
		if (instanceId == null) instanceId = SecurityValues.currentInstanceId();
		return instanceId;
	}

	private int detailSeq;

	private int nodeSeq;

	private boolean updateDifferentTotalCurrentUnits;

	private class Journal extends journals.Row implements JournalPreparer.Journal {
	}

	/**
	 * journal登録処理
	 */
	public void register(UUID journalId, UUID batchId, UUID userId, JournalRegisterRequest request) {
		registerInternal(journalId, batchId, userId, request);
	}

	/**
	 * journal複数件登録処理
	 */
	public void register(UUID[] journalIds, UUID batchId, UUID userId, JournalRegisterRequest[] requests) {
		assert journalIds.length == requests.length;

		for (int i = 0; i < journalIds.length; i++) {
			registerInternal(journalIds[i], batchId, userId, requests[i]);
		}
	}

	/**
	 * journal登録処理
	 * nodeの実行順序で一旦マイナスになっても最終的に数量の整合性が取れるのであれば登録可能とするモード
	 */
	public void registerLazily(UUID journalId, UUID batchId, UUID userId, JournalRegisterRequest request) {
		try {
			lazyRegisterMode = true;

			registerInternal(journalId, batchId, userId, request);
		} finally {
			lazyRegisterMode = false;
			finishSnapshotUpdateProcess();
		}
	}

	/**
	 * journal複数件登録処理
	 * nodeの実行順序で一旦マイナスになっても最終的に数量の整合性が取れるのであれば登録可能とするモード
	 */
	public void registerLazily(UUID[] journalIds, UUID batchId, UUID userId, JournalRegisterRequest[] requests) {
		assert journalIds.length == requests.length;

		try {
			lazyRegisterMode = true;

			for (int i = 0; i < journalIds.length; i++) {
				registerInternal(journalIds[i], batchId, userId, requests[i]);
			}
		} finally {
			lazyRegisterMode = false;
			finishSnapshotUpdateProcess();
		}
	}

	private void registerInternal(UUID journalId, UUID batchId, UUID userId, JournalRegisterRequest request) {
		seqRequest.dependsId = request.group_id;
		SeqHandler.getInstance().nextSeq(seqRequest, seq -> registerInternal(journalId, batchId, userId, request, seq));
	}

	private void registerInternal(UUID journalId, UUID batchId, UUID userId, JournalRegisterRequest request, long seq) {
		//初期化
		detailSeq = 0;
		nodeSeq = 0;
		updateDifferentTotalCurrentUnits = false;

		var journal = new Journal();

		var createdAt = uniqueTime();

		JournalPreparer.prepareJournal(journalId, instanceId(), batchId, userId, request, createdAt, journal, recorder);

		journal.setSeq(seq);

		journal.setCode(
			request.code.orElseGet(() -> DefaultCodeGenerator.generate(recorder, request.group_id, seq)));

		try {
			journal.insert();
		} catch (BSQLException e) {
			//既に締められているグループの場合
			var matcher = Pattern.compile("closed_check\\(\\): (\\{[^\\}]+\\})").matcher(e.getMessage());

			if (!matcher.find()) throw e;

			ClosedCheckError error = new Gson().fromJson(matcher.group(1), ClosedCheckError.class);
			throw new AlreadyClosedGroupException(error, e);
		}

		request.tags.ifPresent(tags -> TagExecutor.stickTags(tags, tagId -> {
			recorder.play(() -> new journals_tags().INSERT().VALUES($UUID, $UUID), journalId, tagId).execute();
		}));

		Arrays.stream(request.details)
			.forEach(
				r -> registerDetail(
					userId,
					journalId,
					request.group_id,
					request.fixed_at,
					createdAt,
					r));

		//jobを登録し、別プロセスで現在数量を更新させる
		recorder.play(() -> new jobs().INSERT(a -> a.id).VALUES($UUID), journalId).execute();

		//このJournalの先のJournalの数量を更新した場合
		if (updateDifferentTotalCurrentUnits) currentUnitsUpdater.run();
	}

	public static class ClosedCheckError {

		public String id;

		public String group_id;

		public String fixed_at;

		public String closed_at;
	}

	private class Detail extends details.Row implements JournalPreparer.Detail {
	}

	/**
	 * detail登録処理
	 */
	private void registerDetail(
		UUID userId,
		UUID journalId,
		UUID groupId,
		Timestamp fixedAt,
		Timestamp createdAt,
		DetailRegisterRequest request) {
		var detail = new Detail();

		var detailId = UUID.randomUUID();

		JournalPreparer.prepareDetail(journalId, detailId, request, detail);

		detail.setSeq_in_journal(++detailSeq);

		detail.insert();

		Arrays.stream(request.nodes)
			.forEach(
				r -> registerNode(
					userId,
					detailId,
					groupId,
					fixedAt,
					createdAt,
					r));
	}

	private class Node extends nodes.Row implements JournalPreparer.Node {
	}

	/**
	 * node登録処理
	 */
	private void registerNode(
		UUID userId,
		UUID detailId,
		UUID groupId,
		Timestamp fixedAt,
		Timestamp createdAt,
		NodeRegisterRequest request) {
		UUID nodeId = UUID.randomUUID();
		var node = new Node();

		var nodeSeq = ++this.nodeSeq;

		JournalPreparer.prepareNode(detailId, nodeId, request, node, nodeSeq, recorder);

		node.insert();

		var grantsUnlimited = request.grants_unlimited.orElse(false);

		if (!lazyRegisterMode) {
			updateDifferentTotalCurrentUnits |= storeSnapshot(
				recorder,
				nodeId,
				nodeSeq,
				userId,
				groupId,
				fixedAt,
				createdAt,
				request.unit_id,
				grantsUnlimited,
				request.in_out,
				request.quantity);

			return;
		}

		Runnable snapshotProcess = () -> {
			updateDifferentTotalCurrentUnits |= storeSnapshot(
				recorder,
				nodeId,
				nodeSeq,
				userId,
				groupId,
				fixedAt,
				createdAt,
				request.unit_id,
				grantsUnlimited,
				request.in_out,
				request.quantity);
		};

		if (request.in_out.normalize(request.quantity).compareTo(BigDecimal.ZERO) < 0) {
			minusUpdaterList.add(snapshotProcess);
		} else {
			plusUpdaterList.add(snapshotProcess);
		}
	}

	static boolean storeSnapshot(
		Recorder recorder,
		UUID nodeId,
		int nodeSeq,
		UUID userId,
		UUID groupId,
		Timestamp fixedAt,
		Timestamp createdAt,
		UUID unitId,
		boolean grantsUnlimited,
		InOut inOut,
		BigDecimal quantity) {
		var seq = createSnapshotSeq(fixedAt, createdAt, nodeSeq);

		//この在庫の数量と無制限タイプの在庫かを知るため、直近のsnapshotを取得
		var justBefore = getJustBeforeSnapshot(unitId, seq, recorder);

		//直前のsnapshotが無制限の場合、以降すべてのsnapshotが無制限になるので引き継ぐ
		//そうでなければ今回のリクエストに従う
		var unlimited = justBefore.unlimited ? true : grantsUnlimited;

		var total = inOut.calcurate(justBefore.total, quantity);

		//移動した結果数量がマイナスになる場合エラー
		//ただし無制限設定がされていればOK
		if (!unlimited && total.compareTo(BigDecimal.ZERO) < 0) throw new MinusTotalException(new UUID[] {});

		recorder.play(
			() -> new snapshots().insertStatement(
				a -> a
					.INSERT(
						a.id,
						a.unlimited,
						a.total,
						a.unit_id,
						a.journal_group_id,
						a.fixed_at,
						a.combined_seq,
						a.updated_by)
					.VALUES(
						$UUID,
						$BOOLEAN,
						$BIGDECIMAL,
						$UUID,
						$UUID,
						$TIMESTAMP,
						$STRING,
						$UUID)),
			nodeId,
			unlimited,
			total,
			unitId,
			groupId,
			fixedAt,
			seq,
			userId)
			.execute();

		//登録以降のsnapshotの数量と無制限設定を更新
		try {
			int result = recorder.play(
				() -> new snapshots().updateStatement(
					a -> a.UPDATE(
						//一度trueになったらずっとそのままtrue
						a.unlimited.set("{0} OR ?", Vargs.of(a.unlimited), Vargs.of($BOOLEAN)),
						//自身の数に今回の移動数量を正規化してプラス
						a.total.set("{0} + ?", Vargs.of(a.total), Vargs.of($BIGDECIMAL)),
						a.updated_at.setAny("now()"))
						.WHERE(
							wa -> wa.EXISTS(
								new snapshots()
									.SELECT(sa -> sa.any(1))
									//fixed_atが等しいものの最新は自分なので、それ以降のものに対して処理を行う
									.WHERE(swa -> swa.id.eq(wa.id).AND.unit_id.eq($UUID).AND.combined_seq.gt($STRING))))),
				unlimited,
				inOut.normalize(quantity),
				unitId,
				seq)
				.execute();

			return result > 0;
		} catch (CheckConstraintViolationException e) {
			//未来のsnapshotで数量がマイナスになった

			List<UUID> journalIds = new LinkedList<>();

			//マイナスを引き起こしたsnapshotを含む、journalを取得
			recorder.play(
				() -> new snapshots().SELECT(a -> a.$nodes().$details().journal_id)
					.WHERE(
						a -> a.EXISTS(
							new snapshots()
								.SELECT(sa -> sa.any(1))
								.WHERE(sa -> sa.id.eq(a.id).AND.unit_id.eq($UUID).AND.combined_seq.gt($STRING).AND.expr("{0} + ? < 0", Vargs.of(sa.total), $BIGDECIMAL))))
					.ORDER_BY(a -> a.fixed_at),
				unitId,
				seq,
				inOut.normalize(quantity)).forEach(r -> journalIds.add(r.$nodes().$details().getJournal_id()));

			throw new MinusTotalException(journalIds.toArray(new UUID[journalIds.size()]));
		}
	}

	private static final String timestampFormat;

	private static final String intFormat;

	static {
		timestampFormat = "%015d";
		intFormat = "%06d";
	}

	private static String createSnapshotSeq(Timestamp fixedAt, Timestamp createdAt, int nodeSeq) {
		return String.format(timestampFormat, fixedAt.getTime())
			+ String.format(timestampFormat, createdAt.getTime())
			+ String.format(intFormat, nodeSeq);
	}

	private static final String maxSuffix = "999999999999999"//最大値33658-09-27
		+ "999999";//nodeのseq最大値

	static JustBeforeSnapshot getJustBeforeSnapshot(UUID unitId, Timestamp fixedAt, Recorder recorder) {
		var seq = String.format(timestampFormat, fixedAt.getTime()) + maxSuffix;
		return getJustBeforeSnapshot(unitId, seq, recorder);
	}

	//直近のsnapshotを取得
	private static JustBeforeSnapshot getJustBeforeSnapshot(UUID unitId, String seq, Recorder recorder) {
		return recorder.play(
			() -> new snapshots()
				.SELECT(a -> a.ls(a.total, a.unlimited))
				.WHERE(
					a -> a.unit_id.eq($UUID).AND.combined_seq.eq(
						new snapshots()
							.SELECT(sa -> sa.MAX(sa.combined_seq))
							.WHERE(sa -> sa.unit_id.eq($UUID).AND.in_search_scope.eq(true).AND.combined_seq.lt($STRING)))),
			unitId,
			unitId,
			seq).executeAndGet(r -> {
				var container = new JustBeforeSnapshot();
				while (r.next()) {
					container.total = r.getBigDecimal(snapshots.total);
					container.unlimited = r.getBoolean(snapshots.unlimited);
					return container;
				}

				container.total = BigDecimal.ZERO;
				container.unlimited = false;

				return container;
			});
	}

	/**
	 * 直前のsnapshotの情報を保持するコンテナ
	 */
	static class JustBeforeSnapshot {

		BigDecimal total;

		boolean unlimited;
	}

	public Timestamp deny(UUID journalId, UUID userId, JournalDenyRequest denyRequest) {
		return deny(journalId, userId, denyRequest, r -> {
		});
	}

	public Timestamp deny(UUID journalId, UUID userId, JournalDenyRequest denyRequest, Consumer<JournalRegisterRequest> checker) {
		var request = pickup(denyRequest.deny_id, r -> {
			r.denied_id = Optional.of(denyRequest.deny_id);
			r.deny_reason = denyRequest.deny_reason;
		}, true);

		checker.accept(request);

		request.group_tree_revision = denyRequest.group_tree_revision;

		register(journalId, U.NULL_ID, userId, request);

		return request.fixed_at;
	}

	/**
	 * 数量を強制的に上書きする処理
	 */
	public void overwrite(UUID journalId, UUID userId, JournalOverwriteRequest request) {
		overwrite(journalId, userId, request, r -> {
		});
	}

	/**
	 * 数量を強制的に上書きする処理
	 */
	public void overwrite(UUID journalId, UUID userId, JournalOverwriteRequest request, Consumer<JournalRegisterRequest> checker) {
		var snapshot = getJustBeforeSnapshot(request.unit_id, request.fixed_at, recorder);

		var out = new NodeRegisterRequest();
		out.unit_id = request.unit_id;
		out.in_out = InOut.OUT;
		out.quantity = snapshot.total;
		out.grants_unlimited = Optional.of(snapshot.unlimited);

		var in = new NodeRegisterRequest();
		in.unit_id = request.unit_id;
		in.in_out = InOut.IN;
		in.quantity = request.total;
		in.grants_unlimited = Optional.of(snapshot.unlimited);

		var detailRequest = new DetailRegisterRequest();
		detailRequest.nodes = new NodeRegisterRequest[] { out, in };
		detailRequest.props = request.detail_props;

		var journalRequest = new JournalRegisterRequest();
		journalRequest.group_id = request.group_id;
		journalRequest.fixed_at = request.fixed_at;
		journalRequest.description = request.description;
		journalRequest.details = new DetailRegisterRequest[] { detailRequest };
		journalRequest.tags = request.tags;
		journalRequest.props = request.journal_props;

		checker.accept(journalRequest);

		register(journalId, U.NULL_ID, userId, journalRequest);
	}

	public JournalRegisterRequest pickup(UUID journalId) {
		return pickup(journalId, r -> {
		}, false);
	}

	private JournalRegisterRequest pickup(
		UUID journalId,
		Consumer<JournalRegisterRequest> journalRequestDecorator,
		boolean reverse) {
		var request = new JournalRegisterRequest();

		var details = new LinkedList<DetailRegisterRequest>();

		recorder.play(
			() -> new nodes().selectClause(
				a -> {
					var detailsAssist = a.$details();
					var journalsAssist = detailsAssist.$journals();

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
				.WHERE(a -> a.$details().journal_id.eq($UUID))
				.assist()
				.$details()
				.$journals()
				.intercept(),
			journalId)
			.forEach(journalOne -> {
				var journal = journalOne.get();

				request.group_id = journal.getGroup_id();

				journalRequestDecorator.accept(request);

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

						var inOut = InOut.of(node.getIn_out());

						nodeRequest.in_out = reverse ? inOut.reverse() : inOut;
						nodeRequest.quantity = node.getQuantity();
						nodeRequest.grants_unlimited = Optional.of(node.getGrants_unlimited());
						nodeRequest.restored_props = Optional.of(node.getProps());

						nodes.add(nodeRequest);
					});

					detailRequest.nodes = nodes.toArray(new NodeRegisterRequest[nodes.size()]);
				});

				request.details = details.toArray(new DetailRegisterRequest[details.size()]);
			});

		return request;
	}

	public void registerBatch(UUID batchId, UUID userId) {
		recorder.play(
			() -> new journal_batches().insertStatement(
				a -> a.INSERT(a.id, a.created_by).VALUES($UUID, $UUID)),
			batchId,
			userId).execute();
	}

	private void finishSnapshotUpdateProcess() {
		//追加を先に行い、この処理時点以降の数量がマイナスにならないようにする
		plusUpdaterList.forEach(r -> r.run());
		minusUpdaterList.forEach(r -> r.run());

		plusUpdaterList.clear();
		minusUpdaterList.clear();
	}
}
