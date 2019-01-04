package jp.ats.blackbox.executor;

import java.util.function.Supplier;

import org.blendee.util.Blendee;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;
import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterResult;
import jp.ats.blackbox.persistence.TransferHandler;

public class TransferExecutor {

	private static final int bufferSize = 1024;

	private static Disruptor<Event> disruptor;

	private static RingBuffer<Event> ringBuffer;

	public static synchronized void start() {
		disruptor = new Disruptor<>(Event::new, bufferSize, DaemonThreadFactory.INSTANCE);

		disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
			execute(event);
		});

		disruptor.start();

		ringBuffer = disruptor.getRingBuffer();
	}

	public static synchronized void stop() {
		disruptor.shutdown();
	}

	public static synchronized TransferPromise register(long userId, Supplier<TransferRegisterRequest> requestSupplier) {
		TransferPromise promise = new TransferPromise();

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, requestSupplier.get(), promise));

		return promise;
	}

	public static synchronized TransferPromise deny(long userId, long transferId) {
		TransferPromise promise = new TransferPromise();

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, transferId, promise));

		return promise;
	}

	private static void execute(Event event) {
		try {
			Blendee.execute(t -> {
				TransferRegisterResult result = event.execute();

				//他スレッドに更新が見えるようにcommit
				t.commit();

				//移動時刻を通知
				JobExecutor.next(U.convert(result.transferredAt));

				//publishスレッドに新IDを通知
				event.promise.setTransferId(result.transferId);
			});
		} catch (Throwable t) {
			//TODO 例外をlog
			t.printStackTrace();

			event.promise.setError(t);
		}
	}

	private static class Event {

		private long userId;

		private TransferRegisterRequest request;

		private long denyTransferId;

		private TransferPromise promise;

		private boolean deny;

		private void set(long userId, TransferRegisterRequest request, TransferPromise promise) {
			this.userId = userId;
			this.request = request;
			this.promise = promise;
		}

		private void set(long userId, long denyTransferId, TransferPromise promise) {
			this.userId = userId;
			this.denyTransferId = denyTransferId;
			this.promise = promise;
			deny = true;
		}

		private TransferRegisterResult execute() {
			if (deny) {
				return TransferHandler.deny(userId, denyTransferId);
			}

			return TransferHandler.register(userId, request);
		}
	}
}
