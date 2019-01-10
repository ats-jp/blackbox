package jp.ats.blackbox.executor;

import java.util.UUID;
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

		return promise;
	}

	public TransferPromise deny(UUID userId, UUID transferId) {
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
				event.promise.notify(null);
			});
		} catch (Throwable t) {
			//TODO 例外をlog
			t.printStackTrace();

			event.promise.notify(t);
		}
	}

	private class Event {

		private UUID transferId;

		private UUID userId;

		private TransferRegisterRequest request;

		private UUID denyTransferId;

		private TransferPromise promise;

		private boolean deny;

		private void set(UUID userId, TransferRegisterRequest request, TransferPromise promise) {
			deny = false;

			transferId = promise.getTransferId();
			this.userId = userId;
			this.request = request;
			this.promise = promise;
		}

		private void set(UUID userId, UUID denyTransferId, TransferPromise promise) {
			deny = true;

			transferId = promise.getTransferId();
			this.userId = userId;
			this.denyTransferId = denyTransferId;
			this.promise = promise;
		}

		private TransferRegisterResult execute() {
			if (deny) {
				return handler.deny(transferId, userId, denyTransferId);
			}

			return handler.register(transferId, userId, request);
		}
	}
}
