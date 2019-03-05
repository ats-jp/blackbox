package jp.ats.blackbox.model;

import java.sql.Timestamp;

public class Org {

	final String type = Org.class.getSimpleName().toLowerCase();

	long id;

	String name;

	long revision;

	String props;

	boolean active;

	Timestamp createdAt;

	long createdBy;

	Timestamp updatedAt;

	long updatedBy;
}
