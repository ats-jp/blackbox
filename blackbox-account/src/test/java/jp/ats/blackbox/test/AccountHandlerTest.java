package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.account.AccountHandler;
import jp.ats.blackbox.account.AccountHandler.AccountRegisterRequest;
import jp.ats.blackbox.account.AccountHandler.SubaccountRegisterRequest;
import jp.ats.blackbox.account.AccountType;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.persistence.SecurityValues;

public class AccountHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		SecurityValues.start(U.NULL_ID);

		Blendee.execute(t -> {
			var accountId = registerAccount("01", AccountType.AS);
			registerSubaccount(accountId, "01");

			t.rollback();
		});

		SecurityValues.end();
	}

	public static UUID registerAccount(String code, AccountType type) {
		var req = new AccountRegisterRequest();
		req.name = "name";
		req.code = code;
		req.org_id = U.NULL_ID;
		req.type = type;
		req.props = Optional.of("{}");

		return AccountHandler.registerAccount(req);
	}

	public static UUID registerSubaccount(UUID accountId, String code) {
		var req = new SubaccountRegisterRequest();
		req.name = "name";
		req.code = code;
		req.account_id = accountId;
		req.props = Optional.of("{}");

		return AccountHandler.registerSubaccount(req);
	}
}
