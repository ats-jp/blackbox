package jp.ats.blackbox.model;

import java.math.BigDecimal;

public enum InOut {

	IN("I", BigDecimal.ONE),

	OUT("O", BigDecimal.ONE.negate());

	public final String value;

	private final BigDecimal coefficient;

	private InOut(String value, BigDecimal coefficient) {
		this.value = value;
		this.coefficient = coefficient;
	}

	public BigDecimal calcurate(BigDecimal total, BigDecimal quantity) {
		return total.add(quantity.multiply(coefficient));
	}
}
