package jp.ats.blackbox.core.controller;

import java.util.UUID;

import org.blendee.jdbc.BlendeeManager;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.controller.JournalController.PrivilegeException;
import jp.ats.blackbox.core.executor.JournalExecutorMap;
import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.core.persistence.OrgHandler;
import jp.ats.blackbox.core.persistence.OrgHandler.RegisterRequest;
import jp.ats.blackbox.core.persistence.OrgHandler.UpdateRequest;
import jp.ats.blackbox.core.persistence.Privilege;
import jp.ats.blackbox.core.persistence.SecurityValues;
import sqlassist.bb.users;

public class OrgController {

	public static UUID register(RegisterRequest request) throws PrivilegeException {
		var userId = SecurityValues.currentUserId();

		if (new users().SELECT(a -> a.privilege).WHERE(a -> a.active.eq(true)).fetch(userId).get().getPrivilege() != Privilege.SYSTEM.value) throw new PrivilegeException();

		var result = OrgHandler.register(request, userId);

		BlendeeManager.get().getCurrentTransaction().commit();
		JournalExecutorMap.reloadOrg(result);

		return result;
	}

	public static void update(UpdateRequest request) throws PrivilegeException {
		var userId = SecurityValues.currentUserId();

		if (!hasPrivilege(userId, request.id)) throw new PrivilegeException();

		OrgHandler.update(request, userId);

		BlendeeManager.get().getCurrentTransaction().commit();
		JournalExecutorMap.reloadOrg(request.id);
	}

	public static void delete(UUID orgId, long revision) throws PrivilegeException, AlreadyUsedException {
		var userId = SecurityValues.currentUserId();

		if (!hasPrivilege(userId, orgId)) throw new PrivilegeException();

		OrgHandler.delete(orgId, revision);

		BlendeeManager.get().getCurrentTransaction().commit();
		JournalExecutorMap.removeOrg(orgId);
	}

	private static boolean hasPrivilege(UUID userId, UUID orgId) {
		//自身を検索
		return U.recorder.play(
			() -> new users().SELECT(a -> a.ls(a.$groups().org_id, a.privilege))
				.WHERE(a -> a.active.eq(true)))
			.fetch(userId)
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
}
