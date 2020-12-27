package jp.ats.blackbox.executor;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blendee.jdbc.BlendeeManager;
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
import jp.ats.blackbox.persistence.GroupHandler;
import jp.ats.blackbox.persistence.JournalHandler;
import jp.ats.blackbox.persistence.JournalHandler.JournalDenyRequest;
import jp.ats.blackbox.persistence.JournalHandler.JournalRegisterRequest;
import jp.ats.blackbox.persistence.JournalHandler.OverwriteRequest;
import jp.ats.blackbox.persistence.JsonHelper;
import jp.ats.blackbox.persistence.MinusTotalException;
import jp.ats.blackbox.persistence.TransientHandler;
import jp.ats.blackbox.persistence.TransientHandler.TransientMoveRequest;
import jp.ats.blackbox.persistence.TransientHandler.TransientMoveResult;
import sqlassist.bb.journal_errors;

public class JournalExecutor {

	private static final Logger logger = LogManager.getLogger(JournalExecutor.class);

	private static final int deadlockRetryCount = 10;

	private static final Runnable currentUnitsUpdater = () -> JobExecutor.updateDifferentRows();

	private final Disruptor<Event> disruptor;

	private final RingBuffer<Event> ringBuffer;

	private final Recorder recorder = Recorder.newAsyncInstance();

	private final JournalHandler handler = new JournalHandler(recorder, currentUnitsUpdater);

	private final Map<UUID, PausingGroup> pausingGroups = new HashMap<>();

	public JournalExecutor() {
		this(256);
	}

	public JournalExecutor(int bufferSize) {//bufferSize must be a power of 2
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
		return disruptor.getBufferSize() - ringBuffer.remainingCapacity() == 0;
	}

	public JournalPromise registerJournal(UUID userId, Supplier<JournalRegisterRequest> requestSupplier) {
		var promise = new JournalPromise();

		var command = new JournalRegisterCommand(promise.getId(), userId, requestSupplier.get());

		registerJournalInternal(userId, promise, command);

		return promise;
	}

	public JournalPromise registerJournalLazily(UUID userId, Supplier<JournalRegisterRequest> requestSupplier) {
		var promise = new JournalPromise();

		var command = new LazyJournalRegisterCommand(promise.getId(), userId, requestSupplier.get());

		registerJournalInternal(userId, promise, command);

		return promise;
	}

	private void registerJournalInternal(UUID userId, JournalPromise promise, Command command) {
		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		//バッチなど、単一の処理が大量に登録しても、他のスレッドが割り込めるように
		Thread.yield();
	}

	public JournalPromise denyJournal(UUID userId, Supplier<JournalDenyRequest> requestSupplier) {
		var promise = new JournalPromise();

		var command = new JournalDenyCommand(promise.getId(), userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		return promise;
	}

	public OverwritePromise overwrite(UUID userId, Supplier<OverwriteRequest> requestSupplier) {
		var promise = new OverwritePromise();

		var command = new OverwriteCommand(promise, userId, requestSupplier.get());

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		return promise;
	}

	public static class PausingGroup {

		public final UUID groupId;

		public final Timestamp willCloseAt;

		private PausingGroup(UUID groupId, Timestamp willCloseAt) {
			this.groupId = Objects.requireNonNull(groupId);
			this.willCloseAt = Objects.requireNonNull(willCloseAt);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PausingGroup)) return false;
			var another = (PausingGroup) obj;
			return groupId.equals(another.groupId) && willCloseAt.equals(another.willCloseAt);
		}

		@Override
		public int hashCode() {
			return Objects.hash(groupId, willCloseAt);
		}

		@Override
		public String toString() {
			return "group id; " + groupId + ", will close at: " + willCloseAt;
		}
	}

	//仮締め開始
	public PausingGroup[] pauseGroups(UUID userId, UUID groupId, Timestamp willCloseAt) throws CommandFailedException, InterruptedException {
		var promise = new JournalPromise();

		var command = new PauseCommand(groupId, willCloseAt, userId);

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		promise.waitUntilFinished();

		synchronized (command.groups) {
			return command.groups.toArray(new PausingGroup[command.groups.size()]);
		}
	}

	//仮締めキャンセル
	public PausingGroup[] resumeGroups(UUID userId, UUID groupId) throws CommandFailedException, InterruptedException {
		var promise = new JournalPromise();

		var command = new ResumeCommand(groupId);

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		promise.waitUntilFinished();

		synchronized (command.groups) {
			return command.groups.toArray(new PausingGroup[command.groups.size()]);
		}
	}

	//現在仮締め中グループ取得
	public PausingGroup[] getPausingGroups(UUID userId, UUID groupId) throws CommandFailedException, InterruptedException {
		var promise = new JournalPromise();

		var command = new GetPausingGroupsCommand(groupId);

		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(userId, command, promise));

		promise.waitUntilFinished();

		synchronized (command.groups) {
			return command.groups.toArray(new PausingGroup[command.groups.size()]);
		}
	}

	public JournalPromise close(UUID userId, Supplier<ClosingRequest> requestSupplier) {
		var promise = new JournalPromise();

		var command = new CloseCommand(promise.getId(), userId, requestSupplier.get());

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

					//publishスレッドに終了を通知
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

				insertErrorLog(event, deadlockException);

				event.promise.notifyError(deadlockException);

				return;
			} catch (Throwable error) {
				logger.fatal(error.getMessage(), error);

				insertErrorLog(event, error);

				event.promise.notifyError(error);

				return;
			}
		}
	}

	@SuppressWarnings("serial")
	public static class GroupPausingException extends BlackboxException {

		private GroupPausingException(PausingGroup group) {
			super(group.toString());
		}
	}

	private void checkPausing(UUID groupId, Timestamp willCloseAt) {
		var group = pausingGroups.get(groupId);
		if (group == null) return;

		if (group.willCloseAt.getTime() > willCloseAt.getTime()) new GroupPausingException(group);
	}

	private static void insertErrorLog(Event event, Throwable error) {
		try {
			Blendee.execute(t -> {
				event.insertErrorLog(error);
			});
		} catch (Throwable errorsError) {
			logger.fatal(errorsError.getMessage(), errorsError);
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
						command.type().name(),
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
			checkPausing(request.group_id, request.fixed_at);

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

	private class LazyJournalRegisterCommand implements Command {

		private final UUID journalId;

		private final UUID userId;

		private final JournalRegisterRequest request;

		private LazyJournalRegisterCommand(UUID journalId, UUID userId, JournalRegisterRequest request) {
			this.journalId = journalId;
			this.userId = userId;
			this.request = request;
		}

		@Override
		public void execute() {
			checkPausing(request.group_id, request.fixed_at);

			handler.registerLazily(journalId, U.NULL_ID, userId, request);
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
			return CommandType.JOURNAL_LAZY_REGISTER;
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
			fixedAt = handler.deny(journalId, userId, request, r -> checkPausing(r.group_id, r.fixed_at));
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

	private class OverwriteCommand implements Command {

		private final OverwritePromise promise;

		private final UUID journalId;

		private final UUID userId;

		private final OverwriteRequest request;

		private final List<UUID> deniedJournals = new LinkedList<>();

		private OverwriteCommand(OverwritePromise promise, UUID userId, OverwriteRequest request) {
			this.promise = promise;
			this.journalId = promise.getId();
			this.userId = userId;
			this.request = request;
		}

		@Override
		public void execute() {
			overwrite();
		}

		private void overwrite() {
			try {
				handler.overwrite(journalId, userId, request, r -> checkPausing(r.group_id, r.fixed_at));
			} catch (MinusTotalException e) {
				var journalIds = e.getMinusTotalJournalIds();

				//自身がマイナスになった場合
				if (journalIds.length == 0) throw e;

				//再度実行するためロールバック
				BlendeeManager.get().getCurrentTransaction().rollback();

				//マイナスを起こしたjournalを取消
				denyMinusJournals(journalIds[0], deniedJournals);

				//whileではなく再起呼び出しにすることで無限ループを回避
				overwrite();
			}
		}

		private void denyMinusJournals(UUID journalId, List<UUID> deniedJournals) {
			var denyRequest = new JournalDenyRequest();
			denyRequest.deny_id = journalId;

			try {
				handler.deny(UUID.randomUUID(), userId, denyRequest, r -> checkPausing(r.group_id, r.fixed_at));
			} catch (MinusTotalException e) {
				//打消し自身はマイナスにはならないので、この例外が出ているということは未来のjournalで発生している
				//その未来のjournalを打消し
				denyMinusJournals(e.getMinusTotalJournalIds()[0], deniedJournals);
			}

			deniedJournals.add(journalId);
		}

		@Override
		public void doAfterCommit() {
			//移動時刻を通知
			JobExecutor.next(U.convert(request.fixed_at));

			promise.setDeniedJournalIds(deniedJournals.toArray(new UUID[deniedJournals.size()]));
		}

		@Override
		public Object request() {
			return request;
		}

		@Override
		public CommandType type() {
			return CommandType.OVERWRITE;
		}
	}

	private class PauseCommand implements Command {

		private final PausingGroup origin;

		private final Set<PausingGroup> groups = new LinkedHashSet<>();

		private final UUID userId;

		private PauseCommand(UUID groupId, Timestamp willCloseAt, UUID userId) {
			this.origin = new PausingGroup(groupId, willCloseAt);
			this.userId = userId;
		}

		@Override
		public void execute() {
			GroupHandler.lockChildren(origin.groupId, userId);

			synchronized (groups) {
				pausingGroups.put(origin.groupId, origin);
				groups.add(origin);
				GroupHandler.children(origin.groupId).forEach(id -> {
					var child = new PausingGroup(id, origin.willCloseAt);
					pausingGroups.put(id, child);
					groups.add(child);
				});
			}
		}

		@Override
		public void doAfterCommit() {
		}

		@Override
		public Object request() {
			return origin;
		}

		@Override
		public CommandType type() {
			return CommandType.PAUSE;
		}
	}

	private class ResumeCommand implements Command {

		private final UUID groupId;

		private final List<PausingGroup> groups = new LinkedList<>();

		private ResumeCommand(UUID groupId) {
			this.groupId = groupId;
		}

		@Override
		public void execute() {
			synchronized (groups) {
				var group = pausingGroups.remove(groupId);
				if (group != null) groups.add(group);

				GroupHandler.children(groupId).forEach(id -> {
					var g = pausingGroups.remove(id);
					if (g != null) groups.add(g);
				});
			}

			GroupHandler.unlock(groupId);
		}

		@Override
		public void doAfterCommit() {
		}

		@Override
		public Object request() {
			return groupId;
		}

		@Override
		public CommandType type() {
			return CommandType.RESUME;
		}
	}

	private class GetPausingGroupsCommand implements Command {

		private final UUID groupId;

		private final List<PausingGroup> groups = new LinkedList<>();

		private GetPausingGroupsCommand(UUID groupId) {
			this.groupId = groupId;
		}

		@Override
		public void execute() {
			synchronized (groups) {
				var group = pausingGroups.remove(groupId);
				if (pausingGroups.containsKey(groupId)) groups.add(group);

				GroupHandler.children(groupId).forEach(id -> {
					var g = pausingGroups.get(id);
					if (g != null) groups.add(g);
				});
			}
		}

		@Override
		public void doAfterCommit() {
		}

		@Override
		public Object request() {
			return groupId;
		}

		@Override
		public CommandType type() {
			return CommandType.GET_PAUSING_GROUPS;
		}
	}

	private class CloseCommand implements Command {

		private final UUID closingId;

		private final UUID userId;

		private final ClosingRequest request;

		private CloseCommand(UUID closingId, UUID userId, ClosingRequest request) {
			this.closingId = closingId;
			this.userId = userId;
			this.request = request;
		}

		@Override
		public void execute() {
			try {
				GroupHandler.lockChildren(request.group_id, userId);
				ClosingHandler.close(closingId, userId, request);

				pausingGroups.remove(request.group_id);

				GroupHandler.children(request.group_id).forEach(e -> {
					pausingGroups.remove(e);
				});
			} finally {
				GroupHandler.unlock(request.group_id);
			}
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
			return CommandType.CLOSE;
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
			result = TransientHandler.move(batchId, userId, request, recorder, currentUnitsUpdater, r -> checkPausing(r.group_id, r.fixed_at));
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
