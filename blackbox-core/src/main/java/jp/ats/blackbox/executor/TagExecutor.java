package jp.ats.blackbox.executor;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blendee.jdbc.TablePath;
import org.blendee.sql.Recorder;
import org.blendee.util.Blendee;

import jp.ats.blackbox.common.BlackboxException;
import jp.ats.blackbox.persistence.TagHandler;

public class TagExecutor {

	private static final Logger logger = LogManager.getLogger(JobExecutor.class);

	private static final Recorder recorder = Recorder.newAsyncInstance();

	private static final int capacity = 100;

	@SuppressWarnings("serial")
	private static final LinkedHashMap<String, UUID> cache = new LinkedHashMap<>(capacity, 0.75f, true) {

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, UUID> eldest) {
			return size() > capacity;
		}
	};

	private static final ExecutorService service = Executors.newSingleThreadExecutor(r -> {
		var thread = Executors.defaultThreadFactory().newThread(r);
		thread.setDaemon(true);//daemonスレッドを使用
		return thread;
	});

	public static void stickTags(String[] tags, UUID targetId, TablePath target) {
		Arrays.stream(tags).map(TagExecutor::getTagId).forEach(tagId -> TagHandler.stickTag(tagId, targetId, target));
	}

	//主に更新用に、現在の連携を削除し再度連携を登録する
	public static void stickTagsAgain(String[] tags, UUID targetId, TablePath target) {
		Arrays.stream(tags).map(TagExecutor::getTagId).forEach(tagId -> TagHandler.stickTagAgain(tagId, targetId, target));
	}

	public static void stickTags(String[] tags, Consumer<UUID> stickAction) {
		Arrays.stream(tags).map(TagExecutor::getTagId).forEach(tagId -> stickAction.accept(tagId));
	}

	private static final UUID getTagId(String tag) {
		Future<UUID> future = service.submit(() -> {
			UUID id = cache.get(tag);

			if (id != null) return id;

			try {
				id = Blendee.executeAndGet(t -> {
					return TagHandler.getTagId(tag, recorder);
				});
			} catch (Throwable t) {
				logger.fatal(t.getMessage(), t);
			}

			cache.put(tag, id);

			return id;
		});

		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new BlackboxException(e);
		}
	}
}
