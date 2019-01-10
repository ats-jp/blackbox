package jp.ats.blackbox.persistence;

import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import sqlassist.bb.dbs;

public class SecurityValues {

	public static UUID currentDBId() {
		UUID[] id = { null };
		Blendee.execute(t -> {
			id[0] = new dbs().SELECT(a -> a.id).WHERE(a -> a.principal.eq(true)).willUnique().get().getId();
		});

		return id[0];
	}

	public static UUID currentOrgId() {
		return U.NULL;
	}

	public static UUID currentUserId() {
		return U.NULL;
	}
}
