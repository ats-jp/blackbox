package jp.ats.blackbox.persistence;

import java.util.UUID;

import jp.ats.blackbox.common.BlackboxException;

@SuppressWarnings("serial")
public class MinusTotalException extends BlackboxException {

	private final UUID[] journalIds;

	public MinusTotalException(UUID[] journalIds) {
		this.journalIds = journalIds.clone();
	}

	/**
	 * この例外を引き起こしたsnapshotのtotalがマイナスとなる未来のjournalのIDを返す
	 * 配列が空の場合、登録したjournal自身がマイナスとなっている
	 */
	public UUID[] getMinusTotalJournalIds() {
		return journalIds.clone();
	}
}
