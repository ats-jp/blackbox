package jp.ats.blackbox.persistence;

import java.math.BigDecimal;

public enum InOut {

	IN(Constant.IN, BigDecimal.ONE) {

		@Override
		public InOut reverse() {
			return OUT;
		}
	},

	OUT(Constant.OUT, BigDecimal.ONE.negate()) {

		@Override
		public InOut reverse() {
			return IN;
		}
	};

	public final String value;

	private final BigDecimal coefficient;

	private InOut(String value, BigDecimal coefficient) {
		this.value = value;
		this.coefficient = coefficient;
	}

	private static class Constant {

		private static final String IN = "I";

		private static final String OUT = "O";
	}

	public static InOut of(String value) {
		switch (value) {
		case Constant.IN:
			return IN;
		case Constant.OUT:
			return OUT;
		default:
			throw new Error();
		}
	}

	public BigDecimal calcurate(BigDecimal total, BigDecimal quantity) {
		return total.add(normalize(quantity));
	}

	public BigDecimal normalize(BigDecimal quantity) {
		return quantity.multiply(coefficient);
	}

	public abstract InOut reverse();
}
