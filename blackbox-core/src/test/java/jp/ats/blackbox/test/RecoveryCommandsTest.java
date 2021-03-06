package jp.ats.blackbox.test;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.executor.JobExecutor;
import jp.ats.blackbox.core.persistence.RecoveryCommands;
import jp.ats.blackbox.core.persistence.SecurityValues;

public class RecoveryCommandsTest {

	public static void main(String[] args) {
		Common.startWithLog();
		SecurityValues.start(U.NULL_ID);

		JobExecutor.start();

		Blendee.execute(t -> {
			RecoveryCommands.recreateSnapshots(t);
			RecoveryCommands.recreateCurrentUnits();

			RecoveryCommands.linkCurrentUnitsToSnapshots(a -> {
			});
		});

		SecurityValues.end();
	}
}
