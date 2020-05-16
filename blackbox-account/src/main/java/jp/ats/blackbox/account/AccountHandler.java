package jp.ats.blackbox.account;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.executor.TagExecutor;
import jp.ats.blackbox.persistence.JsonHelper;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.SeqHandler;
import sqlassist.bb_account.accounts;

public class AccountHandler {

	public static enum AccountType {

		/**
		 * 資産 (Assets)
		 */
		AS,

		/**
		 * 負債 (Liabilities)
		 */
		LI,

		/**
		 * 純資産 (Equity)
		 */
		EQ,

		/**
		 * 収益 (Revenue)
		 */
		RE,

		/**
		 * 費用 (Expenses)
		 */
		EX;
	}

	public static class AccountRegisterRequest {

		public UUID org_id;

		public String code;

		public String name;

		public AccountType type;

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();
	}

	public static UUID registerAccount(AccountRegisterRequest request) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = accounts.$TABLE;
		seqRequest.dependsColumn = accounts.org_id;
		seqRequest.dependsId = request.org_id;
		return SeqHandler.getInstance().nextSeqAndGet(seqRequest, seq -> {
			return registerAccountInternal(request, seq);
		});
	}

	private static UUID registerAccountInternal(AccountRegisterRequest request, long seq) {
		var row = accounts.row();

		UUID id = UUID.randomUUID();

		UUID userId = SecurityValues.currentUserId();

		row.setId(id);
		row.setOrg_id(request.org_id);
		row.setSeq(seq);
		row.setCode(request.code);
		row.setName(request.name);
		row.setType(request.type.name());
		request.props.ifPresent(v -> row.setProps(JsonHelper.toJson(v)));
		request.tags.ifPresent(v -> row.setTags(v));
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		request.tags.ifPresent(v -> TagExecutor.stickTags(v, id, accounts.$TABLE));

		return id;
	}
}
