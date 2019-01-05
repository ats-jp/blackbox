package jp.ats.blackbox.test;

import org.blendee.jdbc.BConnection;
import org.blendee.jdbc.Transaction;
import org.blendee.util.DriverTransactionFactory;

public class TransferTestTransactionFactory extends DriverTransactionFactory {

	public TransferTestTransactionFactory() throws Exception {
		super();
	}

	private Transaction t;

	@Override
	public Transaction createTransaction() {
		if (t == null)
			t = new TestTtansaction(super.createTransaction());
		return t;
	}

	private static class TestTtansaction extends Transaction {

		private final Transaction original;

		private TestTtansaction(Transaction original) {
			this.original = original;
		}

		@Override
		protected BConnection getConnectionInternal() {
			return original.getConnection();
		}

		@Override
		protected void commitInternal() {
			original.commit();
		}

		@Override
		protected void rollbackInternal() {
			original.rollback();
		}

		@Override
		protected void closeInternal() {}
	}
}
