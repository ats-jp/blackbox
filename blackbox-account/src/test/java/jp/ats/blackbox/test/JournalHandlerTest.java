package jp.ats.blackbox.test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.IntStream;

import org.blendee.util.Blendee;

import jp.ats.blackbox.account.AccountType;
import jp.ats.blackbox.account.AccountingJournal;
import jp.ats.blackbox.account.DebitCredit;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.GroupHandler;
import jp.ats.blackbox.persistence.GroupHandler.RegisterRequest;
import jp.ats.blackbox.persistence.JobHandler;
import jp.ats.blackbox.persistence.JournalHandler;
import jp.ats.blackbox.persistence.JournalHandler.DetailRegisterRequest;
import jp.ats.blackbox.persistence.JournalHandler.JournalRegisterRequest;
import jp.ats.blackbox.persistence.SecurityValues;

public class JournalHandlerTest {

	public static void main(String[] args) throws Exception {
		Common.startWithLog();

		SecurityValues.start(U.NULL_ID);

		Blendee.execute(t -> {
			var handler = new JournalHandler(U.recorder);
			IntStream.range(0, 10).forEach(i -> {
				handler.register(UUID.randomUUID(), U.NULL_ID, U.NULL_ID, createRequest(i, registerGroup()));
				JobHandler.execute(LocalDateTime.now());

				t.commit(); //created_atを確定するために一件毎commit
			});
		});
	}

	static JournalRegisterRequest createRequest(int i, UUID groupId) {
		var journal = new AccountingJournal();

		journal.add(groupId, registerSubaccount(AccountType.AS, UUID.randomUUID().toString(), "01"), DebitCredit.DEBIT, new BigDecimal(10000));
		journal.add(groupId, registerSubaccount(AccountType.RE, UUID.randomUUID().toString(), "02"), DebitCredit.CREDIT, new BigDecimal(10000));

		var r = new JournalRegisterRequest();

		r.group_id = groupId;
		r.fixed_at = new Timestamp(System.currentTimeMillis());

		r.details = new DetailRegisterRequest[] { journal.extract() };

		return r;
	}

	public static UUID registerSubaccount(AccountType type, String code, String subcode) {
		return AccountHandlerTest.registerSubaccount(
			AccountHandlerTest.registerAccount(code, type),
			subcode);
	}

	public static UUID registerGroup() {
		var req = new RegisterRequest();
		req.name = "test group";
		req.parent_id = U.NULL_ID;
		req.org_id = U.NULL_ID;

		return GroupHandler.register(req, U.NULL_ID);
	}
}
