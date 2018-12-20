package jp.ats.blackbox.persistence;

import java.sql.Timestamp;
import java.util.Optional;

public class StockComponent {

	public static class InsertRequest {

		public long group_id;

		public String name;

		public Optional<String> extension;

		public Optional<String[]> tags;
	}

	public static class UpdateRequest {
		
		public long id;

		public long group_id;

		public Optional<String> name;

		public long revision;

		public Optional<String> extension;

		public Optional<String[]> tags;

		public Optional<Boolean> active;
	}

	public static class Response {

		public String type;
		
		public long id;

		public long group_id;

		public String name;

		public long revision;

		public String extension;

		public String[] tags;

		public boolean active;

		public Timestamp created_at;

		public long created_by;

		public Timestamp updated_at;

		public long updated_by;
	}
}
