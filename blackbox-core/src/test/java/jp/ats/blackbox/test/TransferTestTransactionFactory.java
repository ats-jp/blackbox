package jp.ats.blackbox.test;

import java.sql.Connection;

import org.blendee.jdbc.Transaction;
import org.blendee.jdbc.impl.JDBCTransaction;
import org.blendee.util.DriverTransactionFactory;

public class TransferTestTransactionFactory extends DriverTransactionFactory {

	public TransferTestTransactionFactory() throws Exception {
		super();
	}

	private ThreadLocal<Connection> connection = new ThreadLocal<>();

	@Override
	public Transaction createTransaction() {
		if (connection.get() == null) connection.set(super.getJDBCConnection());
		return new TestTransaction(connection.get());
	}

	private static class TestTransaction extends JDBCTransaction {

		private TestTransaction(Connection connection) {
			super(connection);
		}

		@Override
		protected void closeInternal() {}
	}
}
