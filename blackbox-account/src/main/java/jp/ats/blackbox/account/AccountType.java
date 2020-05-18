package jp.ats.blackbox.account;

import static jp.ats.blackbox.persistence.InOut.IN;
import static jp.ats.blackbox.persistence.InOut.OUT;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

import jp.ats.blackbox.persistence.InOut;

public enum AccountType {

	/**
	 * 資産 (Assets)
	 */
	AS(dc -> {
		switch (dc) {
		case DEBIT:
			return IN;
		case CREDIT:
			return OUT;
		default:
			throw new Error();
		}
	}),

	/**
	 * 負債 (Liabilities)
	 */
	LI(dc -> {
		switch (dc) {
		case DEBIT:
			return OUT;
		case CREDIT:
			return IN;
		default:
			throw new Error();
		}
	}),

	/**
	 * 純資産 (Equity)
	 */
	EQ(dc -> {
		switch (dc) {
		case DEBIT:
			return OUT;
		case CREDIT:
			return IN;
		default:
			throw new Error();
		}
	}),

	/**
	 * 収益 (Revenue)
	 */
	RE(dc -> {
		switch (dc) {
		case DEBIT:
			return OUT;
		case CREDIT:
			return IN;
		default:
			throw new Error();
		}
	}),

	/**
	 * 費用 (Expenses)
	 */
	EX(dc -> {
		switch (dc) {
		case DEBIT:
			return IN;
		case CREDIT:
			return OUT;
		default:
			throw new Error();
		}
	});

	private final Function<DebitCredit, InOut> function;

	private AccountType(Function<DebitCredit, InOut> function) {
		this.function = function;
	}

	InOut inout(DebitCredit dc) {
		return function.apply(Objects.requireNonNull(dc));
	}

	BigDecimal normalize(DebitCredit dc, BigDecimal amount) {
		return inout(dc).normalize(amount);
	}
}
