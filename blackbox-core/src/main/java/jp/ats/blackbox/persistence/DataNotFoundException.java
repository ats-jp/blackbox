package jp.ats.blackbox.persistence;

import java.util.UUID;

import org.blendee.jdbc.TablePath;

@SuppressWarnings("serial")
public class DataNotFoundException extends BlackboxPersistenceException {

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
