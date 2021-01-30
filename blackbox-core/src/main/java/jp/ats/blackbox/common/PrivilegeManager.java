package jp.ats.blackbox.common;

import static org.blendee.sql.Placeholder.$UUID;

import java.util.Set;
import java.util.UUID;

import jp.ats.blackbox.persistence.Privilege;
import sqlassist.bb.orgs;
import sqlassist.bb.relationships;
import sqlassist.bb.units;
import sqlassist.bb.users;

public class PrivilegeManager {

	public static boolean hasPrivilegeOfOrg(UUID userId, UUID orgId) {
		//自身を検索
		return U.recorder.play(
			() -> new users().SELECT(a -> a.ls(a.$groups().org_id, a.privilege))
				.WHERE(a -> a.active.eq(true).AND.id.eq($UUID)),
			userId)
			.willUnique()
			.map(
				r -> {
					var myOrgId = r.$groups().getOrg_id();
					var myPrivilege = r.getPrivilege().intValue();

					//自身の組織の場合、自身の権限がORG以上か
					if (myOrgId.equals(orgId)) return myPrivilege < Privilege.ORG.value;

					//自身の組織ではない場合、自身の権限がSYSTEMか
					return myPrivilege <= Privilege.SYSTEM.value;
				})
			.orElse(false);
	}

	/**
	 * groupIdがgrousIdsすべての親であることを検査
	 */
	public static boolean hasPrivilegeOfUnits(UUID groupId, Set<UUID> unitIds) {
		//INの要素が可変のためRecorderは使用しない
		return new units()
			.SELECT(a -> a.COUNT())
			.LEFT_OUTER_JOIN(new relationships().WHERE(a -> a.id.IS_NULL()))
			.ON(
				(l, r) -> r.child_id.eq(l.group_id).AND.parent_id.eq(groupId),
				(l, r) -> l.id.IN(unitIds.toArray(new UUID[unitIds.size()])))
			.WHERE(a -> a.OR.$groups().active.eq(false))
			.executeAndGet(r -> {
				r.next();
				return r.getInt(1) == 0;
			});
	}

	public static boolean hasPrivilegeOfUnit(UUID groupId, UUID unitId) {
		return U.recorder.play(
			() -> new units()
				.SELECT(a -> a.COUNT())
				.LEFT_OUTER_JOIN(new relationships().WHERE(a -> a.id.IS_NULL()))
				.ON(
					(l, r) -> r.child_id.eq(l.group_id).AND.parent_id.eq($UUID),
					(l, r) -> l.id.eq($UUID))
				.WHERE(a -> a.OR.$groups().active.eq(false)),
			groupId,
			unitId).executeAndGet(r -> {
				r.next();
				return r.getInt(1) == 0;
			});
	}

	public static class PrivilegeResult {

		public boolean success;

		public long groupTreeRevision;
	}

	public static PrivilegeResult hasPrivilegeOfGroup(UUID userId, UUID groupId, Privilege privilege) {
		var result = new PrivilegeResult();

		//自身の属するグループ及びその下部グループに対象のグループがあるか
		return U.recorder.play(
			() -> new users()
				.SELECT(a -> a.ls(a.privilege, a.$groups().$orgs().group_tree_revision))
				.LEFT_OUTER_JOIN(new relationships().SELECT(a -> a.id))
				.ON((l, r) -> r.parent_id.eq(l.group_id).AND.child_id.eq($UUID))
				.WHERE(a -> a.active.eq(true).AND.id.eq($UUID)),
			groupId,
			userId).executeAndGet(r -> {
				if (!r.next()) {
					//ユーザーが非アクティブまたはIDが一致しない
					return result;
				}

				var myPrivilege = Privilege.of(r.getBigDecimal(users.privilege).intValue()).value;

				result.groupTreeRevision = r.getLong(orgs.group_tree_revision);

				r.getBigDecimal(relationships.id);
				if (r.wasNull()) {//グループに関連づかないユーザー
					//組織権限以上を持っていれば操作可能
					result.success = myPrivilege <= Privilege.ORG.value;
					return result;
				}

				//指定された権限以上を持っていれば操作可能
				result.success = myPrivilege <= privilege.value;
				return result;
			});
	}

	public static boolean hasPrivilegeOfUser(UUID userId, UUID subjectUserId) {
		//対象者のグループを取得
		return U.recorder.play(
			() -> new users()
				.SELECT(a -> a.group_id)
				.WHERE(a -> a.active.eq(true).AND.id.eq($UUID)),
			subjectUserId).willUnique().map(r ->
		//対象者のグループの操作権限を持つか
		hasPrivilegeOfGroup(userId, r.getGroup_id(), Privilege.GROUP).success)
			.orElse(false);//対象者がいない
	}
}
