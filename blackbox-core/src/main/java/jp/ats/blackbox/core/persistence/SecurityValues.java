package jp.ats.blackbox.core.persistence;

import java.util.Objects;
import java.util.UUID;

import org.blendee.util.Blendee;

import sqlassist.bb.instances;

public class SecurityValues {

	private static final ThreadLocal<UUID> container = new ThreadLocal<>();

	public static void start(UUID userId) {
		container.set(Objects.requireNonNull(userId));
	}

	public static void end() {
		container.remove();
	}

	static UUID currentInstanceId() {
		UUID[] id = { null };
		Blendee.execute(t -> {
			id[0] = new instances().SELECT(a -> a.id).WHERE(a -> a.principal.eq(true)).willUnique().get().getId();
		});

		return id[0];
	}

	public static UUID currentUserId() {
		return Objects.requireNonNull(container.get());
	}
}
