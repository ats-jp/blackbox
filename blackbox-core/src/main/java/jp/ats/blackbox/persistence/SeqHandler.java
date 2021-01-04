package jp.ats.blackbox.persistence;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.blendee.jdbc.TablePath;
import org.blendee.util.GenericTable;

public class SeqHandler {

	private static final SeqHandler singleton = new SeqHandler();

	public static class Request {

		public TablePath table;

		public String dependsColumn;

		public UUID dependsId;

		public String seqColumn = "seq";
	}

	public static SeqHandler getInstance() {
		return singleton;
	}

	private final Map<SeqKey, SeqContainer> cache = new HashMap<>();

	public void nextSeq(Request request, Consumer<Long> seqConsumer) {
		var value = container(request);
		synchronized (value.lock) {
			var next = value.next();
			seqConsumer.accept(next);

			//例外が発生しなければ新しい値で更新
			value.updateNext(next);
		}
	}

	public <T> T nextSeqAndGet(Request request, Function<Long, T> seqFunction) {
		var value = container(request);
		synchronized (value.lock) {
			var next = value.next();
			T result = seqFunction.apply(next);

			//例外が発生しなければ新しい値で更新
			value.updateNext(next);

			return result;
		}
	}

	private SeqContainer container(Request request) {
		var key = new SeqKey(request);
		synchronized (cache) {
			var value = cache.get(key);
			if (value == null) {
				value = new SeqContainer(request);
				cache.put(key, value);
			}

			return value;
		}
	}

	private static class SeqKey {

		public final TablePath table;

		public final String dependsColumn;

		public final UUID dependsId;

		private SeqKey(Request request) {
			table = Objects.requireNonNull(request.table);
			dependsColumn = Objects.requireNonNull(request.dependsColumn);
			dependsId = Objects.requireNonNull(request.dependsId);
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof SeqKey)) return false;

			var another = (SeqKey) object;
			return table.equals(another.table) && dependsColumn.equals(another.dependsColumn) && dependsId.equals(another.dependsId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(table, dependsColumn, dependsId);
		}
	}

	private static class SeqContainer {

		private final TablePath table;

		private final String dependsColumn;

		private final UUID dependsId;

		private final String seqColumn;

		private final Object lock = new Object();

		private long currentSeq;

		private SeqContainer(Request request) {
			table = Objects.requireNonNull(request.table);
			dependsColumn = Objects.requireNonNull(request.dependsColumn);
			dependsId = Objects.requireNonNull(request.dependsId);
			seqColumn = Objects.requireNonNull(request.seqColumn);

			currentSeq = new GenericTable(table)
				.SELECT(a -> a.MAX(a.col(seqColumn)))
				.WHERE(a -> a.col(dependsColumn).eq(dependsId))
				.executeAndGet(r -> {
					r.next();
					var max = r.getBigDecimal(1);
					return max == null ? BigDecimal.ZERO : max;
				})
				.longValue();
		}

		private long next() {
			return currentSeq + 1;
		}

		private void updateNext(long next) {
			currentSeq = next;
		}
	}
}
