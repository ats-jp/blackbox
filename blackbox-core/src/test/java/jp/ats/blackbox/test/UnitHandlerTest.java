package jp.ats.blackbox.test;

import java.util.UUID;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.UnitHandler;
import sqlassist.bb.units;

public class UnitHandlerTest {

	public static void main(String[] args) throws Exception {
		JournalCommon.start();
		//TransferCommon.startWithLog();

		SecurityValues.start(U.NULL_ID);

		IntStream.range(0, 9).forEach(i -> System.out.println(register()));

		SecurityValues.end();
	}

	static UUID register() {
		return Blendee.executeAndGet(t -> {
			return UnitHandler.register(U.NULL_ID, U.NULL_ID, () -> new units().SELECT(a -> a.id)).getId();
		});
	}
}
