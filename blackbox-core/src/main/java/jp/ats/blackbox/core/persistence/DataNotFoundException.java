package jp.ats.blackbox.core.persistence;

import java.util.UUID;

import org.blendee.jdbc.TablePath;

import jp.ats.blackbox.common.BlackboxException;

@SuppressWarnings("serial")
public class DataNotFoundException extends BlackboxException {

	private final TablePath table;

	private final UUID id;

	DataNotFoundException(TablePath table, UUID id) {
		this.table = table;
		this.id = id;
	}

	@Override
	public String getMessage() {
		return table + " " + id + " not found.";
	}
}
