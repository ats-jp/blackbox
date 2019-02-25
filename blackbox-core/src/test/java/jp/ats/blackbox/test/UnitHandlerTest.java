package jp.ats.blackbox.test;

import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.UnitHandler;
import sqlassist.bb.units;

public class UnitHandlerTest {

	public static void main(String[] args) throws Exception {
		TransferCommon.start();
		//TransferCommon.startWithLog();

		SecurityValues.start(U.NULL_ID);
		register(GroupHandlerTest.register());
		SecurityValues.end();
	}

	static UUID register(UUID groupId) {
		return Blendee.executeAndGet(t -> {
			return UnitHandler.register(groupId, U.NULL_ID, () -> new units().SELECT(a -> a.id)).getId();
		});
	}
}
