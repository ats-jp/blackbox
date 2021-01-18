package jp.ats.blackbox.account;

import java.math.BigDecimal;

import jp.ats.blackbox.common.BlackboxException;

@SuppressWarnings("serial")
public class DebitsAndCreditsNotEqualException extends BlackboxException {

	DebitsAndCreditsNotEqualException(BigDecimal debits, BigDecimal credits) {
		super("debits: " + debits + ", credits: " + credits);
	}
}
