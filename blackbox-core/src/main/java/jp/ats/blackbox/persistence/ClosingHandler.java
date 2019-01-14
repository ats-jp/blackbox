package jp.ats.blackbox.persistence;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;
import static org.blendee.sql.Placeholder.$TIMESTAMP;
import static org.blendee.sql.Placeholder.$UUID;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import org.blendee.assist.AnonymousTable;
import org.blendee.sql.Recorder;

import sqlassist.bb.closings;
import sqlassist.bb.last_closings;
import sqlassist.bb.snapshots;

public class ClosingHandler {

	private static final Recorder recorder = new Recorder();

	public static UUID close(UUID userId, ClosingRequest request) {
		var closing = closings.row();

		var id = UUID.randomUUID();

		closing.setId(id);
		closing.setGroup_id(request.group_id);
		closing.setClosed_at(request.closed_at);
		request.extension.ifPresent(v -> closing.setExtension(toJson(v)));
		closing.setCreated_by(userId);

		closing.insert();

		var lastClosing = new last_closings().fetch(request.group_id)
			.orElseGet(() -> {
				var row = last_closings.row();
				row.setId(request.group_id);
				return row;
			});

		lastClosing.setClosed_at(request.closed_at);
		lastClosing.setClosing_id(id);

		//insertかupdateかを実行
		lastClosing.save();

		//snapshots検索高速化のために、締め対象の中の最終在庫を残し、その他は検索対象外とするためin_search_scopeをfalseにする
		recorder.play(
			() -> new snapshots().updateStatement(
				a -> a
					.UPDATE(a.in_search_scope.set(false))
					.WHERE(
						wa -> wa.in_search_scope.eq(true).AND.id.IN(
							new AnonymousTable(
								new snapshots()
									.SELECT(
										sa -> sa.ls(
											sa.id,
											sa.any(
												"RANK() OVER (ORDER BY {0} DESC, {1} DESC, {2} DESC)",
												sa.transferred_at,
												sa.created_at,
												sa.node_seq).AS("rank")))
									.WHERE(swa -> swa.transfer_group_id.eq($UUID).AND.transferred_at.le($TIMESTAMP)),
								"ranked")
									.SELECT(aa -> aa.col("id"))
									.WHERE(aa -> aa.col("rank").ge(2))))),
			request.group_id,
			request.closed_at).execute();

		return id;
	}

	public static class ClosingRequest {

		public UUID group_id;

		public Timestamp closed_at;

		/**
		 * 追加情報JSON
		 */
		public Optional<String> extension = Optional.empty();
	}
}
