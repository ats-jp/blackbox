package jp.ats.blackbox.executor;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blendee.sql.Recorder;
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
import jp.ats.blackbox.persistence.TransferHandler;
import jp.ats.blackbox.persistence.TransferHandler.TransferDenyRequest;
import jp.ats.blackbox.persistence.TransferHandler.TransferRegisterRequest;
import jp.ats.blackbox.persistence.TransientHandler;
import jp.ats.blackbox.persistence.TransientHandler.TransientMoveRequest;
import jp.ats.blackbox.persistence.TransientHandler.TransientMoveResult;
import sqlassist.bb.journal_errors;

public class TransferExecutor {

	private static final Logger logger = LogManager.getLogger(TransferExecutor.class);

	private static final int bufferSize = 256;

	private final Disruptor<Event> disruptor;

	private final RingBuffer<Event> ringBuffer;

	private final Recorder recorder = Recorder.newAsyncInstance();

	private final TransferHandler handler = new TransferHandler(recorder);

	public TransferExecutor() {
		disruptor = new Disruptor<>(Event::new, bufferSize, DaemonThreadFactory.INSTANCE);

		disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
			execute(event);
		});

		ringBuffer = disruptor.getRingBuffer();
	}

	public void start() {
		disruptor.start();

	}

	public void stop() {
		disruptor.shutdown();
	}

	public boolean isEmpty() {
		return bufferSize - ringBuffer.remainingCapacity() == 0;
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

	public TransferPromise moveTransient(UUID userId, Supplier<TransientMoveRequest> requestSupplier) {
		var promise = new TransferPromise();

		var command = new TransientMoveCommand(promise.getId(), userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		return promise;
	}

	private static void execute(Event event) {
		try {
			Blendee.execute(t -> {
				event.command.execute();

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
			new journal_errors().insertStatement(
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
			handler.register(transferId, U.NULL_ID, userId, request);
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

	private class TransientMoveCommand implements Command {

		private final TransientMoveRequest request;

		private final UUID batchId;

		private final UUID userId;

		private TransientMoveResult result;

		private TransientMoveCommand(UUID batchId, UUID userId, TransientMoveRequest request) {
			this.batchId = batchId;
			this.userId = userId;
			this.request = request;
		}

		@Override
		public void execute() {
			result = TransientHandler.move(batchId, userId, request, recorder);
		}

		@Override
		public void doAfterCommit() {
			//最古の移動時刻を通知
			JobExecutor.next(U.convert(result.firstTransferredAt));
		}

		@Override
		public Object request() {
			return request;
		}

		@Override
		public CommandType type() {
			return CommandType.TRANSIENT_MOVE;
		}
	}
}
