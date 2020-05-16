package jp.ats.blackbox.test;

import java.util.Optional;
import java.util.UUID;

import org.blendee.util.Blendee;

import jp.ats.blackbox.account.AccountHandler;
import jp.ats.blackbox.account.AccountHandler.AccountRegisterRequest;
import jp.ats.blackbox.account.AccountHandler.AccountType;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.SecurityValues;

public class AccountHandlerTest {

	public static void main(String[] args) {
		Common.startWithLog();

		SecurityValues.start(U.NULL_ID);

		register();
		SecurityValues.end();
	}

	public static UUID register() {
		var req = new AccountRegisterRequest();
		req.name = "name";
		req.code = "01";
		req.org_id = U.NULL_ID;
		req.type = AccountType.AS;
		req.props = Optional.of("{}");

		UUID[] id = { null };

		Blendee.execute(t -> {
			id[0] = AccountHandler.registerAccount(req);
			t.rollback();
		});

		return id[0];
	}

}
