package jp.ats.blackbox.persistence;

import java.math.BigDecimal;

public enum InOut {

	IN(Constant.IN) {

		@Override
		public InOut reverse() {
			return OUT;
		}
	},

	OUT(Constant.OUT) {

		@Override
		public InOut reverse() {
			return IN;
		}
	};

	public final BigDecimal value;

	public final int intValue;

	private InOut(int value) {
		this.intValue = value;
		this.value = new BigDecimal(value);
	}

	private static class Constant {

		private static final int IN = 1;

		private static final int OUT = -1;
	}

	public static InOut of(int value) {
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
		return quantity.multiply(value);
	}

	public abstract InOut reverse();
}
