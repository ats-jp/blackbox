package jp.ats.blackbox.executor;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blendee.util.Blendee;

import com.google.gson.Gson;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import jp.ats.blackbox.common.BlackboxException;
import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.ClosingHandler;
import jp.ats.blackbox.persistence.ClosingHandler.ClosingRequest;
import jp.ats.blackbox.persistence.JsonHelper;
import jp.ats.blackbox.persistence.Retry;
import jp.ats.blackbox.persistence.TooMatchRetryException;
import jp.ats.blackbox.persistence.TransferComponent.TransferDenyRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
import jp.ats.blackbox.persistence.TransferHandler;
import sqlassist.bb.transfer_errors;

public class TransferExecutor {

	private static final Logger logger = LogManager.getLogger(TransferExecutor.class);

	private static final int bufferSize = 256;

	private final Disruptor<Event> disruptor;

	private final RingBuffer<Event> ringBuffer;

	private final TransferHandler handler;

	public TransferExecutor() {
		disruptor = new Disruptor<>(Event::new, bufferSize, DaemonThreadFactory.INSTANCE);

		disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
			execute(event);
		});

		ringBuffer = disruptor.getRingBuffer();

		handler = new TransferHandler();
	}

	public void start() {
		disruptor.start();

	}

	public void stop() {
		disruptor.shutdown();
	}

	public TransferPromise registerTransfer(UUID userId, Supplier<TransferRegisterRequest> requestSupplier) {
		var promise = new TransferPromise();

		var command = new TransferRegisterCommand(promise.getId(), userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		//バッチなど、単一の処理が大量に登録しても、他のスレッドが割り込めるように
		Thread.yield();

		return promise;
	}

	public TransferPromise denyTransfer(UUID userId, Supplier<TransferDenyRequest> requestSupplier) {
		var promise = new TransferPromise();

		var command = new TransferDenyCommand(promise.getId(), userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		return promise;
	}

	public TransferPromise close(UUID userId, Supplier<ClosingRequest> requestSupplier) {
		var promise = new TransferPromise();

		var command = new ClosingCommand(promise.getId(), userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		return promise;
	}

	private static final int RETRY = 1;

	private static void execute(Event event) {
		try {
			Blendee.execute(t -> {
				int counter = 0;
				while (true) {
					try {
						event.command.execute();
					} catch (Retry r) {
						logger.warn(r.getMessage(), r);

						if (counter++ >= RETRY) {
							throw new TooMatchRetryException();
						}

						t.rollback();
						continue;
					}

					break;
				}

				//他スレッドに更新が見えるようにcommit
				t.commit();

				event.command.doAfterCommit();

				//publishスレッドに新IDを通知
				event.promise.notifyFinished();
			});
		} catch (Throwable error) {
			logger.fatal(error.getMessage(), error);

			try {
				Blendee.execute(t -> {
					event.insertErrorLog(error);
				});
			} catch (Throwable errorsError) {
				logger.fatal(errorsError.getMessage(), errorsError);
			}

			event.promise.notifyError(error);
		}
	}

	private class Event {

		private UUID userId;

		private Command command;

		private TransferPromise promise;

		private void set(UUID userId, Command command, TransferPromise promise) {
			this.userId = userId;
			this.command = command;
			this.promise = promise;
		}

		private void insertErrorLog(Throwable error) {
			new transfer_errors().insertStatement(
				a -> a
					.INSERT(
						a.abandoned_id,
						a.command_type,
						a.error_type,
						a.message,
						a.stack_trace,
						a.sql_state,
						a.user_id,
						a.request)
					.VALUES(
						promise.getId(),
						command.type().value,
						errorType(error),
						error.getMessage(),
						U.getStackTrace(error),
						U.getSQLState(error).orElse(""),
						userId,
						JsonHelper.toJson(new Gson().toJson(command.request()))))
				.execute();
		}
	}

	private static String errorType(Throwable t) {
		if (t instanceof BlackboxException) return t.getClass().getName();

		var cause = t.getCause();

		if (cause == null) return t.getClass().getName();

		return errorType(t.getCause());
	}

	private interface Command {

		void execute();

		void doAfterCommit();

		Object request();

		CommandType type();
	}

	private class TransferRegisterCommand implements Command {

		private final UUID transferId;

		private final UUID userId;

		private final TransferRegisterRequest request;

		private TransferRegisterCommand(UUID transferId, UUID userId, TransferRegisterRequest request) {
			this.transferId = transferId;
			this.userId = userId;
			this.request = request;
		}

		@Override
		public void execute() {
			handler.register(transferId, userId, request);
		}

		@Override
		public void doAfterCommit() {
			//移動時刻を通知
			JobExecutor.next(U.convert(request.transferred_at));
		}

		@Override
		public Object request() {
			return request;
		}

		@Override
		public CommandType type() {
			return CommandType.TRANSFER_REGISTER;
		}
	}

	private class TransferDenyCommand implements Command {

		private final UUID transferId;

		private final UUID userId;

		private final TransferDenyRequest request;

		private Timestamp transferredAt;

		private TransferDenyCommand(UUID transferId, UUID userId, TransferDenyRequest request) {
			this.transferId = transferId;
			this.userId = userId;
			this.request = request;
		}

		@Override
		public void execute() {
			transferredAt = handler.deny(transferId, userId, request);
		}

		@Override
		public void doAfterCommit() {
			//移動時刻を通知
			JobExecutor.next(U.convert(transferredAt));
		}

		@Override
		public Object request() {
			return request;
		}

		@Override
		public CommandType type() {
			return CommandType.TRANSFER_DENY;
		}
	}

	private class ClosingCommand implements Command {

		private final UUID closingId;

		private final UUID userId;

		private final ClosingRequest request;

		private ClosingCommand(UUID closingId, UUID userId, ClosingRequest request) {
			this.closingId = closingId;
			this.userId = userId;
			this.request = request;
		}

		@Override
		public void execute() {
			ClosingHandler.close(closingId, userId, request);
		}

		@Override
		public void doAfterCommit() {}

		@Override
		public Object request() {
			return request;
		}

		@Override
		public CommandType type() {
			return CommandType.CLOSING;
		}
	}
}
