package jp.ats.blackbox.account;

import static org.blendee.sql.Placeholder.$UUID;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.blendee.sql.Recorder;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.Requests.DetailRegisterRequest;
import jp.ats.blackbox.persistence.Requests.NodeRegisterRequest;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.UnitHandler;
import sqlassist.bb_account.group_accounts;
import sqlassist.bb_account.subaccounts;

public class AccountingJournal {

	public static class Request {

		UUID groupId;

		DebitCredit debitCredit;

		UUID subaccountId;

		BigDecimal amount;

		Optional<String> nodeProps = Optional.empty();
	}

	private final List<Request> requests = new LinkedList<>();

	private final Recorder recorder;

	public AccountingJournal() {
		this.recorder = U.recorder;
	}

	public AccountingJournal(Recorder recorder) {
		this.recorder = recorder;
	}

	public void add(
		UUID groupId,
		UUID subaccountId,
		DebitCredit debitCredit,
		BigDecimal amount) {
		addInternal(groupId, subaccountId, debitCredit, amount, Optional.empty());
	}

	public void add(
		UUID groupId,
		UUID subaccountId,
		DebitCredit debitCredit,
		BigDecimal amount,
		String nodeProps) {
		addInternal(groupId, subaccountId, debitCredit, amount, Optional.of(nodeProps));
	}

	private void addInternal(
		UUID groupId,
		UUID subaccountId,
		DebitCredit debitCredit,
		BigDecimal amount,
		Optional<String> nodeProps) {
		var request = new Request();
		request.groupId = Objects.requireNonNull(groupId);
		request.subaccountId = Objects.requireNonNull(subaccountId);
		request.debitCredit = Objects.requireNonNull(debitCredit);
		request.amount = Objects.requireNonNull(amount);
		request.nodeProps = nodeProps;

		requests.add(request);
	}

	public DetailRegisterRequest extract() {
		return extractInternal(Optional.empty());
	}

	public DetailRegisterRequest extract(String detailProps) {
		return extractInternal(Optional.of(detailProps));
	}

	private DetailRegisterRequest extractInternal(Optional<String> detailProps) {
		var debits = BigDecimal.ZERO;
		var credits = BigDecimal.ZERO;
		for (var r : requests) {
			switch (r.debitCredit) {
			case DEBIT:
				debits = debits.add(r.amount);
				break;
			case CREDIT:
				credits = credits.add(r.amount);
				break;
			default:
				throw new Error();
			}
		}

		if (!debits.equals(credits)) throw new DebitsAndCreditsNotEqualException(debits, credits);

		var list = requests.stream().map(e -> createNodeRequest(e)).collect(Collectors.toList());

		var detailRequest = new DetailRegisterRequest();
		detailRequest.props = detailProps;

		detailRequest.nodes = list.toArray(new NodeRegisterRequest[list.size()]);

		return detailRequest;
	}

	private NodeRegisterRequest createNodeRequest(Request request) {
		var subaccount = recorder.play(
			() -> new subaccounts().SELECT(
				a -> a.ls(
					a.id,
					a.$accounts().type))
				.WHERE(a -> a.id.eq($UUID)),
			request.subaccountId)
			.willUnique()
			.get();

		var nodeRequest = new NodeRegisterRequest();

		nodeRequest.unit_id = getUnitId(request.groupId, subaccount.getId());
		nodeRequest.in_out = AccountType.valueOf(subaccount.$accounts().getType()).inout(request.debitCredit);
		nodeRequest.quantity = request.amount;
		nodeRequest.props = request.nodeProps;

		return nodeRequest;
	}

	private UUID getUnitId(UUID groupId, UUID subaccountId) {
		return recorder.play(
			() -> new group_accounts().WHERE(a -> a.group_id.eq($UUID).AND.subaccount_id.eq($UUID)),
			subaccountId,
			groupId)
			.willUnique()
			.map(v -> v.getId())
			.orElseGet(() -> register(groupId, subaccountId));
	}

	private UUID register(UUID groupId, UUID subaccountId) {
		var userId = SecurityValues.currentUserId();
		var unitId = UnitHandler.register(userId, groupId);

		recorder.play(
			() -> new group_accounts().insertStatement(
				a -> a.INSERT(
					a.id,
					a.group_id,
					a.subaccount_id,
					a.created_by)
					.VALUES($UUID, $UUID, $UUID, $UUID)),
			unitId,
			groupId,
			subaccountId,
			userId).execute();

		return unitId;
	}
}
