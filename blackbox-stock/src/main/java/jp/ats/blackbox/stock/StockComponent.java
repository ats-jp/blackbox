package jp.ats.blackbox.stock;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class StockComponent {

	public static class RegisterRequest {

		public UUID group_id;

		public String name;

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();
	}

	public static class UpdateRequest {

		public UUID id;

		public Optional<UUID> group_id = Optional.empty();

		public Optional<String> name = Optional.empty();

		public long revision;

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}

	public static class ForcibleUpdateRequest {

		public UUID id;

		public Optional<UUID> group_id = Optional.empty();

		public Optional<String> name = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}

	public static class Response {

		public String type;

		public UUID id;

		public UUID group_id;

		public String name;

		public long revision;

		public String props;

		public String[] tags;

		public boolean active;

		public Timestamp created_at;

		public UUID created_by;

		public Timestamp updated_at;

		public UUID updated_by;
	}
}
