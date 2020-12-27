package jp.ats.blackbox.executor;

import java.util.UUID;

public class OverwritePromise extends JournalPromise {

	private UUID[] deniedJournalIds;

	/**
	 * {@link JournalPromise#waitUntilFinished()}で、処理を終了を待たない場合NullPointerExceptionが発生する可能性がある
	 * @return 上書きしたことによって、未来の不正になったjournalたちのID
	 */
	public UUID[] deniedJournalIds() {
		lock();
		try {
			return deniedJournalIds.clone();
		} finally {
			unlock();
		}
	}

	void setDeniedJournalIds(UUID[] deniedJournalIds) {
		lock();
		try {
			this.deniedJournalIds = deniedJournalIds;
		} finally {
			unlock();
		}
	}
}
