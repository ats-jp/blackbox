package jp.ats.blackbox.executor;

import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blendee.util.Blendee;

import com.google.gson.Gson;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.JsonHelper;
import jp.ats.blackbox.persistence.Retry;
import jp.ats.blackbox.persistence.TransferComponent.TransferDenyRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterResult;
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

	public TransferPromise register(UUID userId, Supplier<TransferRegisterRequest> requestSupplier) {
		TransferPromise promise = new TransferPromise();

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, requestSupplier.get(), promise));

		//バッチなど、単一の処理が大量に登録しても、他のスレッドが割り込めるように
		Thread.yield();

		return promise;
	}

	public TransferPromise deny(UUID userId, Supplier<TransferDenyRequest> requestSupplier) {
		TransferPromise promise = new TransferPromise();

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, requestSupplier.get(), promise));

		return promise;
	}

	private static void execute(Event event) {
		try {
			Blendee.execute(t -> {
				TransferRegisterResult result = null;
				while (true) {
					try {
						result = event.execute();
					} catch (Retry r) {
						logger.warn(r.getMessage(), r);

						t.rollback();
						continue;
					}

					break;
				}

				//他スレッドに更新が見えるようにcommit
				t.commit();

				//移動時刻を通知
				JobExecutor.next(U.convert(result.transferredAt));

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

		private UUID transferId;

		private UUID userId;

		private TransferRegisterRequest registerRequest;

		private TransferDenyRequest denyRequest;

		private TransferPromise promise;

		private boolean deny;

		private void set(UUID userId, TransferRegisterRequest request, TransferPromise promise) {
			deny = false;

			transferId = promise.getTransferId();
			this.userId = userId;
			registerRequest = request;
			denyRequest = null;
			this.promise = promise;
		}

		private void set(UUID userId, TransferDenyRequest request, TransferPromise promise) {
			deny = true;

			transferId = promise.getTransferId();
			this.userId = userId;
			registerRequest = null;
			denyRequest = request;
			this.promise = promise;
		}

		private TransferRegisterResult execute() {
			if (deny) {
				return handler.deny(transferId, userId, denyRequest);
			}

			return handler.register(transferId, userId, registerRequest);
		}

		private void insertErrorLog(Throwable error) {
			new transfer_errors().insertStatement(
				a -> a
					.INSERT(
						a.transfer_id,
						a.message,
						a.stack_trace,
						a.sql_state,
						a.user_id,
						a.request)
					.VALUES(
						transferId,
						error.getMessage(),
						U.getStackTrace(error),
						U.getSQLState(error).orElse(""),
						userId,
						JsonHelper.toJson(new Gson().toJson(deny ? denyRequest : registerRequest))))
				.execute();
		}
	}
}
