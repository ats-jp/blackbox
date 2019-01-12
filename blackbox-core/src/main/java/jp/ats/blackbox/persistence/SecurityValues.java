package jp.ats.blackbox.persistence;

import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import sqlassist.bb.instances;

public class SecurityValues {

	public static UUID currentInstanceId() {
		UUID[] id = { null };
		Blendee.execute(t -> {
			id[0] = new instances().SELECT(a -> a.id).WHERE(a -> a.principal.eq(true)).willUnique().get().getId();
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
