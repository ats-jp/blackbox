package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;
import static org.blendee.sql.Placeholder.$BIGDECIMAL;
import static org.blendee.sql.Placeholder.$BOOLEAN;
import static org.blendee.sql.Placeholder.$TIMESTAMP;
import static org.blendee.sql.Placeholder.$UUID;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.blendee.assist.AnonymousTable;
import org.blendee.jdbc.Batch;
import org.blendee.jdbc.BlendeeManager;
import org.blendee.sql.Recorder;

import jp.ats.blackbox.common.U;
import sqlassist.bb.closed_journals;
import sqlassist.bb.closed_units;
import sqlassist.bb.closings;
import sqlassist.bb.journals;
import sqlassist.bb.last_closings;
import sqlassist.bb.relationships;
import sqlassist.bb.snapshots;

public class ClosingHandler {

	private static final Recorder recorder = U.recorder;

	public static void close(UUID closingId, UUID userId, ClosingRequest request) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = closings.$TABLE;
		seqRequest.dependsColumn = closings.group_id;
		seqRequest.dependsId = request.group_id;
		SeqHandler.getInstance().nextSeq(seqRequest, seq -> closeInternal(closingId, userId, request, seq));
	}

	private static void closeInternal(UUID closingId, UUID userId, ClosingRequest request, long seq) {
		var closing = closings.row();

		closing.setId(closingId);
		closing.setGroup_id(request.group_id);
		closing.setSeq(seq);
		closing.setClosed_at(request.closed_at);
		request.props.ifPresent(v -> closing.setProps(toJson(v)));
		closing.setCreated_by(userId);

		closing.insert();

		var batch = BlendeeManager.getConnection().getBatch();

		//全ての子グループも対象
		recorder.play(
			() -> new relationships()
				.SELECT(a -> a.child_id)
				.WHERE(a -> a.parent_id.eq($UUID)),
			request.group_id)
			.execute(r -> {
				while (r.next()) {
					UUID groupId = U.uuid(r, relationships.child_id);

					closeGroup(groupId, request.closed_at, closingId, userId, batch);
				}
			});

		//journalとclosingを紐づけ
		recorder.play(
			() -> new closed_journals()
				.INSERT(
					new journals()
						.SELECT(a -> a.ls(a.id, a.any(a.expr($UUID))))
						.WHERE(
							a -> a.fixed_at.lt(request.closed_at),
							a -> a.EXISTS(new relationships().SELECT(sa -> sa.any(0)).WHERE(sa -> sa.parent_id.eq($UUID).AND.child_id.eq(a.group_id))),
							a -> a.NOT_EXISTS(new closed_journals().SELECT(sa -> sa.any(0)).WHERE(sa -> sa.id.eq(a.id))))),
			closingId,
			request.group_id)
			.execute(batch);

		batch.execute();
	}

	private static void closeGroup(
		UUID groupId,
		Timestamp closedAt,
		UUID closingId,
		UUID userId,
		Batch batch) {
		var lastClosing = recorder.play(() -> new last_closings())
			.fetch(groupId)
			.orElseGet(() -> {
				var row = last_closings.row();
				row.setId(groupId);
				return row;
			});

		lastClosing.setClosed_at(closedAt);
		lastClosing.setClosing_id(closingId);

		//insertかupdateかを実行
		lastClosing.save();

		//snapshots検索高速化のために、締め対象の中の最終在庫を残し、その他は検索対象外とするためin_search_scopeをfalseにする
		recorder.play(
			() -> new snapshots().updateStatement(
				a -> a
					.UPDATE(
						a.in_search_scope.set(false),
						a.updated_at.setAny("now()"))
					.WHERE(
						wa -> wa.in_search_scope.eq(true).AND.id.IN(
							createRankedQuery(base -> base.SELECT(sa -> sa.id))
								.SELECT(aa -> aa.col("id"))
								.WHERE(aa -> aa.col("rank").ge(2))))),
			groupId,
			closedAt).execute();

		//closed_unitsの更新

		//closed_unitsに存在するものを更新
		recorder.play(
			() -> createRankedQuery(
				base -> base.SELECT(
					a -> a.ls(
						a.unit_id,
						a.unlimited,
						a.total))
					.WHERE(
						a -> a.EXISTS(
							new closed_units()
								.SELECT(sa -> sa.any(0))
								.WHERE(sa -> sa.id.eq(a.unit_id)))))
									.WHERE(a -> a.col("rank").eq(1)),
			groupId,
			closedAt)
			.execute(r -> {
				while (r.next()) {
					recorder.play(
						() -> new closed_units().updateStatement(
							a -> a.UPDATE(
								a.closing_id.set($UUID),
								a.unlimited.set($BOOLEAN),
								a.total.set($BIGDECIMAL),
								a.updated_at.setAny("now()"),
								a.updated_by.set($UUID))
								.WHERE(sa -> sa.id.eq($UUID))),
						closingId,
						r.getBoolean(snapshots.unlimited),
						r.getBigDecimal(snapshots.total),
						userId,
						U.uuid(r, snapshots.unit_id))
						.execute(batch);
				}
			});

		batch.execute();

		//closed_unitsに存在しないものを追加
		recorder.play(() -> {
			var subquery = createRankedQuery(
				base -> base.SELECT(
					a -> a.ls(
						a.unit_id,
						a.unlimited,
						a.total))
					.WHERE(
						a -> a.NOT_EXISTS(
							new closed_units()
								.SELECT(sa -> sa.any(0))
								.WHERE(sa -> sa.id.eq(a.unit_id)))));

			return new closed_units()
				.INSERT(
					a -> a.ls(
						a.id,
						a.closing_id,
						a.unlimited,
						a.total,
						a.updated_by),
					subquery
						.SELECT(
							a -> a.ls(
								a.col(snapshots.unit_id),
								a.any(a.expr($UUID)), //1
								a.col(snapshots.unlimited),
								a.col(snapshots.total),
								a.any(a.expr($UUID)))) //2
						.WHERE(a -> a.col("rank").eq(1)));
		},
			closingId, //1
			userId, //2
			groupId,
			closedAt).execute();
	}

	private static AnonymousTable createRankedQuery(Consumer<snapshots> applyer) {
		var query = new snapshots()
			.SELECT(
				a -> a.ls(
					a.any("RANK() OVER (ORDER BY {0} DESC)", a.seq).AS("rank")))
			.WHERE(a -> a.journal_group_id.eq($UUID).AND.fixed_at.le($TIMESTAMP));

		applyer.accept(query);

		return new AnonymousTable(
			query,
			"ranked");
	}

	public static class ClosingRequest {

		public UUID group_id;

		public Timestamp closed_at;

		/**
		 * 追加情報JSON
		 */
		public Optional<String> props = Optional.empty();
	}
}
