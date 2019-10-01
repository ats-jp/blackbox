package jp.ats.blackbox.executor;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blendee.jdbc.exception.DeadlockDetectedException;
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
import jp.ats.blackbox.persistence.JournalHandler;
import jp.ats.blackbox.persistence.JournalHandler.JournalDenyRequest;
import jp.ats.blackbox.persistence.JournalHandler.JournalRegisterRequest;
import jp.ats.blackbox.persistence.JsonHelper;
import jp.ats.blackbox.persistence.TransientHandler;
import jp.ats.blackbox.persistence.TransientHandler.TransientMoveRequest;
import jp.ats.blackbox.persistence.TransientHandler.TransientMoveResult;
import sqlassist.bb.journal_errors;

public class JournalExecutor {

	private static final Logger logger = LogManager.getLogger(JournalExecutor.class);

	private static final int bufferSize = 256;

	private static final int deadlockRetryCount = 10;

	private final Disruptor<Event> disruptor;

	private final RingBuffer<Event> ringBuffer;

	private final Recorder recorder = Recorder.newAsyncInstance();

	private final JournalHandler handler = new JournalHandler(recorder);

	public JournalExecutor() {
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

	public JournalPromise registerJournal(UUID userId, Supplier<JournalRegisterRequest> requestSupplier) {
		var promise = new JournalPromise();

		var command = new JournalRegisterCommand(promise.getId(), userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		//バッチなど、単一の処理が大量に登録しても、他のスレッドが割り込めるように
		Thread.yield();

		return promise;
	}

	public JournalPromise denyJournal(UUID userId, Supplier<JournalDenyRequest> requestSupplier) {
		var promise = new JournalPromise();

		var command = new JournalDenyCommand(promise.getId(), userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		return promise;
	}

	public JournalPromise close(UUID userId, Supplier<ClosingRequest> requestSupplier) {
		var promise = new JournalPromise();

		var command = new ClosingCommand(promise.getId(), userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		return promise;
	}

	public JournalPromise moveTransient(UUID userId, Supplier<TransientMoveRequest> requestSupplier) {
		var promise = new JournalPromise();

		var command = new TransientMoveCommand(promise.getId(), userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		return promise;
	}

	private static void execute(Event event) {
		int retry = 0;
		while (true) {
			try {
				Blendee.execute(t -> {
					event.command.execute();

					//他スレッドに更新が見えるようにcommit
					t.commit();

					event.command.doAfterCommit();

					//publishスレッドに新IDを通知
					event.promise.notifyFinished();
				});

				return;
			} catch (DeadlockDetectedException deadlockException) {
				//デッドロック検出で設定値分リトライ
				if (deadlockRetryCount >= retry++) {
					logger.warn(deadlockException.getMessage(), deadlockException);

					Thread.yield();

					continue;
				}

				logger.fatal("Deadlock retry count exceeded " + deadlockRetryCount);

				try {
					Blendee.execute(t -> {
						event.insertErrorLog(deadlockException);
					});
				} catch (Throwable errorsError) {
					logger.fatal(errorsError.getMessage(), errorsError);
					event.promise.notifyError(errorsError);
					return;
				}

				event.promise.notifyError(deadlockException);

				return;
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

				return;
			}
		}
	}

	private class Event {

		private UUID userId;

		private Command command;

		private JournalPromise promise;

		private void set(UUID userId, Command command, JournalPromise promise) {
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

	private class JournalRegisterCommand implements Command {

		private final UUID journalId;

		private final UUID userId;

		private final JournalRegisterRequest request;

		private JournalRegisterCommand(UUID journalId, UUID userId, JournalRegisterRequest request) {
			this.journalId = journalId;
			this.userId = userId;
			this.request = request;
		}

		@Override
		public void execute() {
			handler.register(journalId, U.NULL_ID, userId, request);
		}

		@Override
		public void doAfterCommit() {
			//移動時刻を通知
			JobExecutor.next(U.convert(request.fixed_at));
		}

		@Override
		public Object request() {
			return request;
		}

		@Override
		public CommandType type() {
			return CommandType.JOURNAL_REGISTER;
		}
	}

	private class JournalDenyCommand implements Command {

		private final UUID journalId;

		private final UUID userId;

		private final JournalDenyRequest request;

		private Timestamp fixedAt;

		private JournalDenyCommand(UUID journalId, UUID userId, JournalDenyRequest request) {
			this.journalId = journalId;
			this.userId = userId;
			this.request = request;
		}

		@Override
		public void execute() {
			fixedAt = handler.deny(journalId, userId, request);
		}

		@Override
		public void doAfterCommit() {
			//移動時刻を通知
			JobExecutor.next(U.convert(fixedAt));
		}

		@Override
		public Object request() {
			return request;
		}

		@Override
		public CommandType type() {
			return CommandType.JOURNAL_DENY;
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
		public void doAfterCommit() {
		}

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
			JobExecutor.next(U.convert(result.firstFixedAt));
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
