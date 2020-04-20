package jp.ats.blackbox.test;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.SnapshotHandler;

public class SnapshotHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();
		SecurityValues.start(U.NULL_ID);

		JobExecutor.start();

		Blendee.execute(t -> {
			SnapshotHandler.refreshSnapshots(t, s -> {
				s.WHERE(a -> a.id.ne(U.NULL_ID));
			});
		});

		SecurityValues.end();
	}
}
