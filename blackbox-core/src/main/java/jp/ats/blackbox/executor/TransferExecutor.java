package jp.ats.blackbox.executor;

import java.time.LocalDateTime;

import org.blendee.util.Blendee;

import jp.ats.blackbox.persistence.TransferComponent;
import jp.ats.blackbox.persistence.TransferHandler;

public class TransferExecutor {

	public static void execute(TransferComponent.TransferRegisterRequest request) {
		try {
			Blendee.execute(t -> {
				LocalDateTime transferredAt = TransferHandler.register(request);

				//jobスレッドに更新が見えるようにcommit
				t.commit();

				JobExecutor.next(transferredAt);
			});
		} catch (Exception e) {
			//TODO 例外をlog
		}
	}
}
