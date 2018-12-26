package jp.ats.blackbox.persistence;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

import jp.ats.blackbox.model.InOut;

public class TransferComponent {

	/**
	 * transfer登録に必要な情報クラス
	 */
	public static class TransferRegisterRequest {

		/**
		 * このtransferが属するグループ
		 * 必須
		 */
		public long group_id;

		/**
		 * 打消し元のtransfer_id
		 */
		public Optional<Long> denied_id = Optional.empty();

		/**
		 * 移動時刻
		 * 必須
		 */
		public Timestamp transferred_at;

		/**
		 * 追加情報JSON
		 */
		public Optional<String> extension = Optional.empty();

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
		public long group_id;

		/**
		 * stockのitem
		 * stockに格納される
		 * 必須
		 */
		public long item_id;

		/**
		 * stockのowner
		 * stockに格納される
		 * 必須
		 */
		public long owner_id;

		/**
		 * stockのlocation
		 * stockに格納される
		 * 必須
		 */
		public long location_id;

		/**
		 * stockのstatus
		 * stockに格納される
		 * 必須
		 */
		public long status_id;

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
		public Optional<Boolean> grants_infinity = Optional.empty();

		/**
		 * 移動数量
		 */
		public Optional<String> extension = Optional.empty();
	}
}
