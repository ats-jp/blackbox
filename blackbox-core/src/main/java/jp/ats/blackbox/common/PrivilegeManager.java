package jp.ats.blackbox.common;

import static org.blendee.sql.Placeholder.$UUID;

import java.util.UUID;

import jp.ats.blackbox.persistence.Privilege;
import sqlassist.bb.relationships;
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

	public static boolean hasPrivilegeOfGroup(UUID userId, UUID groupId) {
		//自身の属するグループ及びその下部グループに対象のグループがあるか
		U.recorder.play(
			() -> new users()
				.SELECT(a -> a.privilege)
				.LEFT_OUTER_JOIN(new relationships().SELECT(a -> a.id))
				.ON((l, r) -> r.parent_id.eq(l.group_id).AND.child_id.eq($UUID))
				.WHERE(a -> a.active.eq(true).AND.id.eq($UUID)),
			groupId,
			userId).executeAndGet(r -> {
				if (!r.next()) {
					//ユーザーが非アクティブまたはIDが一致しない
					return false;
				}

				var myPrivilege = Privilege.of(r.getBigDecimal(users.privilege).intValue()).value;

				r.getBigDecimal(relationships.id);
				if (r.wasNull()) {//グループに関連づかないユーザー
					//組織権限以上を持っていれば操作可能
					return myPrivilege <= Privilege.ORG.value;
				}

				//グループ権限以上を持っていれば操作可能
				return myPrivilege <= Privilege.GROUP.value;
			});

		return false;
	}

	public static boolean hasPrivilegeOfUser(UUID userId, UUID subjectUserId) {
		//対象者のグループを取得
		return U.recorder.play(
			() -> new users()
				.SELECT(a -> a.group_id)
				.WHERE(a -> a.active.eq(true).AND.id.eq($UUID)),
			subjectUserId).willUnique().map(r ->
		//対象者のグループの操作権限を持つか
		hasPrivilegeOfGroup(userId, r.getGroup_id()))
			.orElse(false);//対象者がいない
	}
}
