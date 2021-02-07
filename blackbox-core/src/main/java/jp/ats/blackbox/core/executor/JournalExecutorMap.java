package jp.ats.blackbox.core.executor;

import static org.blendee.sql.Placeholder.$UUID;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import sqlassist.bb.executors;
import sqlassist.bb.executors_groups;
import sqlassist.bb.groups;
import sqlassist.bb.orgs;
import sqlassist.bb.relationships;

public class JournalExecutorMap {

	private static final Logger logger = LogManager.getLogger(JobExecutor.class);

	private static final ExecutorService service = Executors.newSingleThreadExecutor();

	private static boolean initialized = false;

	//key: org_id, value: group_ids
	private static final Map<UUID, List<UUID>> orgGroups = new HashMap<>();

	//key: group_id, value: executor
	private static final Map<UUID, JournalExecutor> executors = new HashMap<>();

	public static JournalExecutor get(UUID groupId) {
		try {
			return service.submit(() -> {
				initializeIfUninitialized();

				return executors.get(groupId);
			}).get();
		} catch (InterruptedException e) {
			logger.warn(e.getMessage());
			throw new IllegalStateException(e);
		} catch (ExecutionException e) {
			logger.fatal(e.getMessage());
			throw new IllegalStateException(e);
		}
	}

	//組織のすべてのexecutorを再設定する
	public static void reloadOrg(UUID orgId) {
		service.submit(() -> {
			initializeIfUninitialized();

			Blendee.execute(t -> {
				reloadOrgInternal(orgId);
			});
		});
	}

	public static void stopAllExecutors() {
		service.submit(() -> {
			executors.values().forEach(e -> e.stop());
			executors.clear();
			orgGroups.clear();
		});
	}

	public static void shutdown() {
		stopAllExecutors();
		service.shutdown();
	}

	private static void initializeIfUninitialized() {
		if (initialized) return;
		initialized = true;

		Blendee.execute(t -> {
			new orgs()
				.SELECT(a -> a.id)
				.WHERE(a -> a.active.eq(true))
				.ORDER_BY(a -> a.seq)
				.forEach(r -> reloadOrgInternal(r.getId()));
		});
	}

	private static void reloadOrgInternal(UUID orgId) {
		//旧executorsはstopしつつ削除
		optional(
			orgGroups.remove(orgId),
			l -> l.forEach(i -> optional(executors.remove(i), e -> e.stop())));

		U.recorder.play(() -> new executors().SELECT(a -> a.id).WHERE(a -> a.org_id.eq($UUID).AND.active.eq(true)), orgId).forEach(r -> {
			//個別のexecutorを作成し、設定されたグループとその配下のグループにセットしていく
			var executor = new JournalExecutor();
			executor.start();

			U.recorder.play(
				() -> new relationships().SELECT(a -> a.child_id)
					.WHERE(
						a -> a.parent_id.IN(
							new executors_groups()
								.SELECT(sa -> sa.group_id)
								.WHERE(sa -> sa.id.eq($UUID)))),
				r.getId())
				.forEach(rr -> {
					executors.put(rr.getChild_id(), executor);
				});
		});

		var orgExecutor = new JournalExecutor();

		//一旦すべての組織配下のグループに組織に割り当てられたexecutorをセット
		U.recorder.play(
			() -> new groups().SELECT(a -> a.id).WHERE(a -> a.org_id.eq($UUID).AND.active.eq(true)),
			orgId).forEach(r -> {
				var groupId = r.getId();

				orgGroups.putIfAbsent(orgId, new LinkedList<>());
				orgGroups.get(orgId).add(groupId);

				if (!executors.containsKey(groupId)) {
					executors.put(groupId, orgExecutor);
					orgExecutor.start();//セットが確実な場合のみ開始
				}
			});
		//結局orgExecutorがセットされなかった場合、GC
	}

	private static <T> void optional(T nullable, Consumer<T> consumer) {
		Optional.ofNullable(nullable).ifPresent(consumer);
	}
}
