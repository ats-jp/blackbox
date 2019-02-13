package jp.ats.blackbox.persistence;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class TransferComponent {

	/**
	 * transfer登録に必要な情報クラス
	 */
	public static class TransferRegisterRequest {

		/**
		 * このtransferが属するグループ
		 * 必須
		 */
		public UUID group_id;

		/**
		 * 移動時刻
		 * 必須
		 */
		public Timestamp transferred_at;

		/**
		 * 打消し元のtransfer_id
		 */
		public Optional<UUID> denied_id = Optional.empty();

		public Optional<String> deny_reason = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> extension = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_extension = Optional.empty();

		/**
		 * 検索用タグ
		 */
		public Optional<String[]> tags = Optional.empty();

		/**
		 * 配下のbundle
		 * 必須
		 */
		public BundleRegisterRequest[] bundles;
	}

	/**
	 * bundle登録に必要な情報クラス
	 */
	public static class BundleRegisterRequest {

		/**
		 * 追加情報JSON
		 */
		public Optional<String> extension = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_extension = Optional.empty();

		/**
		 * 配下のnode
		 * 必須
		 */
		public NodeRegisterRequest[] nodes;
	}

	/**
	 * node登録に必要な情報クラス
	 */
	public static class NodeRegisterRequest {

		/**
		 * stockの所属するグループ
		 * stockに格納される
		 * 必須
		 */
		public UUID group_id;

		/**
		 * stockのitem
		 * stockに格納される
		 * 必須
		 */
		public UUID item_id;

		/**
		 * stockのowner
		 * stockに格納される
		 * 必須
		 */
		public UUID owner_id;

		/**
		 * stockのlocation
		 * stockに格納される
		 * 必須
		 */
		public UUID location_id;

		/**
		 * stockのstatus
		 * stockに格納される
		 * 必須
		 */
		public UUID status_id;

		/**
		 * 入出庫タイプ
		 */
		public InOut in_out;

		/**
		 * 移動数量
		 */
		public BigDecimal quantity;

		/**
		 * これ以降在庫無制限を設定するか
		 * 在庫無制限の場合、通常はstock登録時からtrueにしておく
		 */
		public Optional<Boolean> grants_unlimited = Optional.empty();

		/**
		 * 移動数量
		 */
		public Optional<String> extension = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_extension = Optional.empty();
	}

	/**
	 * transfer打消し
	 *
	 */
	public static class TransferDenyRequest {

		public UUID denyId;

		public Optional<String> denyReason = Optional.empty();
	}
}
