package jp.ats.blackbox.persistence;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class Requests {

	/**
	 * journal登録に必要な情報クラス
	 */
	public static class JournalRegisterRequest {

		/**
		 * このjournalが属するグループ
		 * 必須
		 */
		public UUID group_id;

		public Optional<String> code = Optional.empty();

		/**
		 * 移動時刻
		 * 必須
		 */
		public Timestamp fixed_at;

		/**
		 * 補足事項
		 */
		public Optional<String> description = Optional.empty();

		/**
		 * 打消し元のjournal_id
		 */
		public Optional<UUID> denied_id = Optional.empty();

		public Optional<String> deny_reason = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> props = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_props = Optional.empty();

		/**
		 * 検索用タグ
		 */
		public Optional<String[]> tags = Optional.empty();

		/**
		 * 権限検査時のgroup_tree_revision
		 */
		public Optional<Long> group_tree_revision = Optional.empty();

		/**
		 * 配下のdetail
		 * 必須
		 */
		public DetailRegisterRequest[] details;
	}

	/**
	 * detail登録に必要な情報クラス
	 */
	public static class DetailRegisterRequest {

		/**
		 * 追加情報JSON
		 */
		public Optional<String> props = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_props = Optional.empty();

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
		 * unit
		 * 必須
		 */
		public UUID unit_id;

		/**
		 * 入出力タイプ
		 */
		public InOut in_out;

		/**
		 * 数量
		 */
		public BigDecimal quantity;

		/**
		 * これ以降数量無制限を設定するか
		 * 数量無制限の場合、通常はunit登録時からtrueにしておく
		 * デフォルトはfalse
		 */
		public Optional<Boolean> grants_unlimited = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> props = Optional.empty();

		/**
		 * DBから復元した追加情報JSON
		 */
		Optional<Object> restored_props = Optional.empty();
	}

	/**
	 * journal打消し
	 *
	 */
	public static class JournalDenyRequest {

		public UUID deny_id;

		public Optional<String> deny_reason = Optional.empty();

		/**
		 * 権限検査時のgroup_tree_revision
		 */
		public Optional<Long> group_tree_revision = Optional.empty();
	}

	/**
	 * 数量上書きに必要な情報クラス
	 */
	public static class JournalOverwriteRequest {

		/**
		 * このjournalが属するグループ
		 * 必須
		 */
		public UUID group_id;

		/**
		 * 移動時刻
		 * 必須
		 */
		public Timestamp fixed_at;

		/**
		 * 補足事項
		 */
		public Optional<String> description = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> journal_props = Optional.empty();

		/**
		 * 検索用タグ
		 */
		public Optional<String[]> tags = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> detail_props = Optional.empty();

		/**
		 * unit
		 * 必須
		 */
		public UUID unit_id;

		/**
		 * 数量
		 */
		public BigDecimal total;

		/**
		 * これ以降数量無制限を設定するか
		 * 数量無制限の場合、通常はunit登録時からtrueにしておく
		 */
		public Optional<Boolean> grants_unlimited = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> nodep_props = Optional.empty();

		/**
		 * 権限検査時のgroup_tree_revision
		 */
		public Optional<Long> group_tree_revision = Optional.empty();
	}

	/**
	 * グループの仮締めに必要な情報クラス
	 */

	public static class GroupPauseRequest {

		public UUID group_id;

		public Timestamp will_close_at;
	}

	/**
	 * グループの操作に必要な情報クラス
	 */

	public static class GroupProcessRequest {

		public UUID group_id;
	}

	/**
	 * 締め処理に必要な情報クラス
	 */
	public static class ClosingRequest {

		public UUID group_id;

		public Timestamp closed_at;

		public Optional<String> description = Optional.empty();

		/**
		 * 追加情報JSON
		 */
		public Optional<String> props = Optional.empty();

		/**
		 * 権限検査時のgroup_tree_revision
		 */
		public Optional<Long> group_tree_revision = Optional.empty();
	}

	/**
	 * 一時作業の確定に必要な情報クラス
	 */
	public static class TransientMoveRequest {

		public UUID transient_id;

		public boolean lazy = false;

		/**
		 * 権限検査時のgroup_tree_revision
		 */
		public Optional<Long> group_tree_revision = Optional.empty();
	}
}
