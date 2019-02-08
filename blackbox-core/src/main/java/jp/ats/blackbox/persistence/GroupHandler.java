package jp.ats.blackbox.persistence;

import static org.blendee.sql.Placeholder.$UUID;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.blendee.dialect.postgresql.ReturningUtilities;
import org.blendee.jdbc.BlendeeManager;
import org.blendee.jdbc.exception.UniqueConstraintViolationException;
import org.blendee.sql.Recorder;

import jp.ats.blackbox.common.U;
import sqlassist.bb.groups;
import sqlassist.bb.locking_groups;
import sqlassist.bb.relationships;

public class GroupHandler {

	private static final Recorder recorder = new Recorder();

	public static void lockParents(UUID groupId) {
		lockParentsInternal(UUID.randomUUID(), groupId);
	}

	public static void lockChildren(UUID groupId) {
		lockChildrenInternal(UUID.randomUUID(), groupId);
	}

	private static void lockParentsInternal(UUID transactionId, UUID groupId) {
		if (groupId.equals(U.NULL_ID)) return;
		Set<UUID> parents = new LinkedHashSet<>();
		collectParents(groupId, parents);
		lock(transactionId, groupId, parents);
	}

	private static void lockChildrenInternal(UUID transactionId, UUID groupId) {
		Set<UUID> children = new LinkedHashSet<>();
		collectChildren(groupId, children);
		lock(transactionId, groupId, children);
	}

	public static void unlock(UUID groupId) {
		recorder.play(() -> new locking_groups().DELETE().WHERE(a -> a.cascade_id.eq($UUID)), groupId).execute();
	}

	private static void collectParents(UUID groupId, Set<UUID> parents) {
		recorder.play(
			() -> new groups().SELECT(a -> a.parent_id).WHERE(a -> a.id.eq($UUID).AND.parent_id.ne(U.NULL_ID)),
			groupId)
			.willUnique()
			.ifPresent(r -> {
				var parent = r.getParent_id();
				parents.add(parent);
				collectParents(parent, parents);
			});
	}

	private static void collectChildren(UUID groupId, Set<UUID> children) {
		recorder.play(() -> new groups().SELECT(a -> a.id).WHERE(a -> a.parent_id.eq($UUID)), groupId)
			.forEach(r -> {
				var child = r.getId();
				children.add(child);
				collectChildren(child, children);
			});
	}

	private static void lock(UUID transactionId, UUID groupId, Set<UUID> groups) {
		try {
			UUID userId = SecurityValues.currentUserId();
			var player = recorder.play(
				() -> new locking_groups().insertStatement(
					a -> a
						.INSERT(
							a.id,
							a.cascade_id,
							a.locking_transaction_id,
							a.user_id)
						.VALUES(
							$UUID,
							$UUID,
							$UUID,
							$UUID)),
				groupId,
				groupId,
				transactionId,
				userId);

			player.execute();

			var batch = BlendeeManager.getConnection().getBatchStatement();

			groups.forEach(id -> player.reproduce(id, groupId, transactionId, userId).execute(batch));

			batch.executeBatch();
		} catch (UniqueConstraintViolationException e) {
			groups.forEach(id -> {});//TODO groupIdがぶつかったtransactionId探し

			throw new GroupLockingException(groupId);
		}
	}

	public static UUID register(RegisterRequest request) {
		lockParents(request.parent_id);
		try {
			var row = groups.row();

			UUID id = UUID.randomUUID();

			row.setId(id);
			row.setOrg_id(SecurityValues.currentOrgId());
			row.setParent_id(request.parent_id);
			row.setName(request.name);
			request.extension.ifPresent(v -> row.setExtension(v));
			request.tags.ifPresent(v -> row.setTags(v));

			UUID userId = SecurityValues.currentUserId();

			row.setCreated_by(userId);
			row.setUpdated_by(userId);

			row.insert();

			request.tags.ifPresent(v -> TagHandler.stickTags(v, groups.$TABLE, id));

			registerRelationships(id, id, U.LONG_NULL_ID);

			return id;
		} finally {
			unlock(request.parent_id);
		}
	}

	private static void registerRelationships(UUID parentId, UUID groupId, long cascadeId) {
		long id = new relationships()
			.SELECT(a -> a.id)
			.WHERE(a -> a.parent_id.eq(parentId).AND.child_id.eq(groupId))
			.willUnique()
			.map(r -> r.getId())
			.orElseGet(
				() -> ReturningUtilities.insertAndReturn(relationships.$TABLE, u -> {
					u.add(relationships.parent_id, parentId);
					u.add(relationships.child_id, groupId);
					u.add(relationships.cascade_id, cascadeId);
				}, r -> r.getLong(relationships.id), relationships.id));

		//active=falseでも一応relationshipは構築しておくために除外しない
		recorder.play(
			() -> new groups().SELECT(a -> a.parent_id).WHERE(a -> a.id.eq($UUID).AND.parent_id.ne(U.NULL_ID)),
			parentId).forEach(r -> {
				//parent_idがNULLではない（親がある）場合、再帰的に登録
				registerRelationships(r.getParent_id(), groupId, id);
			});
	}

	public static void update(UpdateRequest request) {
		var transactionId = UUID.randomUUID();
		try {
			request.parent_id.ifPresent(v -> lockParentsInternal(transactionId, v));
			lockChildrenInternal(transactionId, request.id);
		} catch (GroupLockingException e) {
			//循環のチェック
			//登録時は、存在しているIDのみparentとして指定するため、循環は発生しないが、
			//更新時はparent_idを自信を参照するgroupに書き換えることで循環が発生するため
			//ロックを利用して循環を検出する

			//TODO transactionIdをチェックしてロックがぶつかったか循環化を検出すること

			throw new CycleGroupException(request.id);
		}

		try {
			int result = new groups()
				.UPDATE(a -> {
					request.name.ifPresent(v -> a.name.set(v));
					request.parent_id.ifPresent(v -> a.parent_id.set(v));
					request.extension.ifPresent(v -> a.extension.set(JsonHelper.toJson(v)));
					request.tags.ifPresent(v -> a.tags.set((Object) v));
					request.active.ifPresent(v -> a.active.set(v));
				})
				.WHERE(a -> a.id.eq(request.id).AND.revision.eq(request.revision))
				.execute();

			if (result != 1) throw Utils.decisionException(groups.$TABLE, request.id);

			request.parent_id.ifPresent(id -> {
				List<UUID> groups = new LinkedList<>();
				groups.add(request.id);

				//parent_idが指定された、つまり変更されたとみなしrelationを更新する
				updateRelationships(request.id, groups);
			});
		} finally {
			unlock(request.id);
		}
	}

	private static void updateRelationships(UUID groupId, List<UUID> groups) {
		//DBのカスケードを利用して自身の関連する全relationを削除
		recorder.play(
			() -> new relationships().DELETE().WHERE(a -> a.parent_id.eq($UUID).OR.child_id.eq($UUID)),
			groupId,
			groupId).execute();

		//末端を収集
		walkAndCollectRelationships(groupId, groups);

		//末端から再登録
		groups.forEach(id -> registerRelationships(id, id, U.LONG_NULL_ID));
	}

	/**
	 * 末端のグループのみを再帰的に収集する
	 */
	private static void walkAndCollectRelationships(UUID groupId, List<UUID> groups) {
		//active=falseでも一応relationshipは構築しておくために除外しない
		recorder.play(
			() -> new groups().SELECT(a -> a.ls(a.id, a.parent_id)).WHERE(a -> a.parent_id.eq($UUID)),
			groupId).forEach(r -> {
				var id = r.getId();

				groups.add(id);

				//再帰
				walkAndCollectRelationships(id, groups);
			});
	}

	public static class RegisterRequest {

		public String name;

		public UUID parent_id;

		public Optional<String> extension = Optional.empty();

		public Optional<String[]> tags = Optional.empty();
	}

	public static class UpdateRequest {

		public UUID id;

		public Optional<String> name = Optional.empty();

		public Optional<UUID> parent_id = Optional.empty();

		public long revision;

		public Optional<String> extension = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}
}
