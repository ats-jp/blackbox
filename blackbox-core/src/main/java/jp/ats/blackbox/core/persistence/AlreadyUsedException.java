package jp.ats.blackbox.core.persistence;

import java.util.UUID;

import org.blendee.jdbc.TablePath;
import org.blendee.jdbc.exception.ForeignKeyConstraintViolationException;

@SuppressWarnings("serial")
public class AlreadyUsedException extends Exception {

	private final TablePath table;

	private final UUID id;

	AlreadyUsedException(TablePath table, UUID id, ForeignKeyConstraintViolationException e) {
		super(e);
		this.table = table;
		this.id = id;
	}

	public TablePath table() {
		return table;
	}

	public UUID id() {
		return id;
	}
}
