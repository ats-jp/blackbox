package jp.ats.blackbox.persistence;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

import jp.ats.blackbox.model.InOut;

public class TransferComponent {

	public static class TransferRegisterRequest {

		public long group_id;

		public Optional<Long> denied_id = Optional.empty();

		public Timestamp transferred_at;

		public Optional<String> extension = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public BundleRegisterRequest[] bundles;
	}

	public static class BundleRegisterRequest {

		public Optional<String> extension = Optional.empty();

		public NodeRegisterRequest[] nodes;
	}

	public static class NodeRegisterRequest {

		public long group_id;

		public long item_id;

		public long owner_id;

		public long location_id;

		public long status_id;

		public InOut in_out;

		public BigDecimal quantity;

		public Optional<Boolean> grants_infinity = Optional.empty();

		public Optional<String> extension = Optional.empty();
	}
}
