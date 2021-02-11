package jp.ats.blackbox.backend.api.request;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class SimpleJournalRegisterRequest {

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
	public Optional<Object> props = Optional.empty();

	/**
	 * 検索用タグ
	 */
	public Optional<String[]> tags = Optional.empty();

	/**
	 * 配下のdetail
	 * 必須
	 */
	public SimpleDetailRegisterRequest[] details;
}
