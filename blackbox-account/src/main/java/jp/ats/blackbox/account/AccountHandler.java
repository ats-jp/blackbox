package jp.ats.blackbox.account;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.executor.TagExecutor;
import jp.ats.blackbox.core.persistence.SecurityValues;
import jp.ats.blackbox.core.persistence.SeqHandler;
import jp.ats.blackbox.core.persistence.Utils;
import sqlassist.bb_account.accounts;
import sqlassist.bb_account.subaccounts;

public class AccountHandler {

	public static class AccountRegisterRequest {

		public UUID org_id;

		public String code;

		public String name;

		public Optional<String> description = Optional.empty();

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
		request.description.ifPresent(v -> row.setDescription(v));
		row.setType(request.type.name());
		request.props.ifPresent(v -> row.setProps(U.toPGObject(v)));
		request.tags.ifPresent(v -> row.setTags(v));
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		request.tags.ifPresent(v -> TagExecutor.stickTags(v, id, accounts.$TABLE));

		return id;
	}

	public static class UpdateAccountRequest {

		public UUID id;

		public long revision;

		public Optional<String> code = Optional.empty();

		public Optional<String> name = Optional.empty();

		public Optional<String> description = Optional.empty();

		public Optional<AccountType> type = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}

	public static void updateAccount(UpdateAccountRequest request) {
		int result = new accounts().UPDATE(a -> {
			a.revision.set(request.revision + 1);
			request.code.ifPresent(v -> a.code.set(v));
			request.name.ifPresent(v -> a.name.set(v));
			request.description.ifPresent(v -> a.name.set(v));
			request.type.ifPresent(v -> a.type.set(v.name()));
			request.props.ifPresent(v -> a.props.set(U.toPGObject(v)));
			request.tags.ifPresent(v -> a.tags.set((Object) v));
			request.active.ifPresent(v -> a.active.set(v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(accounts.$TABLE, request.id);

		request.tags.ifPresent(v -> TagExecutor.stickTagsAgain(v, request.id, accounts.$TABLE));
	}

	public static void deleteAccount(UUID accountId, long revision) {
		Utils.delete(accounts.$TABLE, accountId, revision);
	}

	public static class SubaccountRegisterRequest {

		public UUID account_id;

		public String code;

		public String name;

		public Optional<String> description = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();
	}

	public static UUID registerSubaccount(SubaccountRegisterRequest request) {
		var seqRequest = new SeqHandler.Request();
		seqRequest.table = subaccounts.$TABLE;
		seqRequest.dependsColumn = subaccounts.account_id;
		seqRequest.dependsId = request.account_id;
		seqRequest.seqColumn = subaccounts.seq_in_account;
		return SeqHandler.getInstance().nextSeqAndGet(seqRequest, seq -> {
			return registerSubaccountInternal(request, seq);
		});
	}

	private static UUID registerSubaccountInternal(SubaccountRegisterRequest request, long seq) {
		var row = subaccounts.row();

		UUID id = UUID.randomUUID();

		UUID userId = SecurityValues.currentUserId();

		row.setId(id);
		row.setAccount_id(request.account_id);
		row.setSeq_in_account(seq);
		row.setCode(request.code);
		row.setName(request.name);
		request.description.ifPresent(v -> row.setDescription(v));
		request.props.ifPresent(v -> row.setProps(U.toPGObject(v)));
		request.tags.ifPresent(v -> row.setTags(v));
		row.setCreated_by(userId);
		row.setUpdated_by(userId);

		row.insert();

		request.tags.ifPresent(v -> TagExecutor.stickTags(v, id, subaccounts.$TABLE));

		return id;
	}

	public static class UpdateSubaccountRequest {

		public UUID id;

		public long revision;

		public Optional<String> code = Optional.empty();

		public Optional<String> name = Optional.empty();

		public Optional<String> description = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public Optional<Boolean> active = Optional.empty();
	}

	public static void updateSubaccount(UpdateSubaccountRequest request) {
		int result = new subaccounts().UPDATE(a -> {
			a.revision.set(request.revision + 1);
			request.code.ifPresent(v -> a.code.set(v));
			request.name.ifPresent(v -> a.name.set(v));
			request.description.ifPresent(v -> a.name.set(v));
			request.props.ifPresent(v -> a.props.set(U.toPGObject(v)));
			request.tags.ifPresent(v -> a.tags.set((Object) v));
			request.active.ifPresent(v -> a.active.set(v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(subaccounts.$TABLE, request.id);

		request.tags.ifPresent(v -> TagExecutor.stickTagsAgain(v, request.id, subaccounts.$TABLE));
	}

	public static void deleteSubaccount(UUID accountId, long revision) {
		Utils.delete(subaccounts.$TABLE, accountId, revision);
	}
}
