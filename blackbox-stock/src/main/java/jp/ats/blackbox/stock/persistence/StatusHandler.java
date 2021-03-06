package jp.ats.blackbox.stock.persistence;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.core.persistence.Utils;
import jp.ats.blackbox.stock.persistence.StockComponentHandler.GroupInfo;
import sqlassist.bb_stock.statuses;

public class StatusHandler {

	public static class RegisterRequest extends StockComponentHandler.RegisterRequest {

		public UUID group_id;
	}

	public static class UpdateRequest extends StockComponentHandler.UpdateRequest {

		public Optional<UUID> group_id = Optional.empty();

		public Optional<UUID> owner_id = Optional.empty();
	}

	public static UUID register(RegisterRequest request, UUID userId) {
		return StockComponentHandler.register(statuses.$TABLE, request, new GroupInfo(request.group_id), userId, r -> {
			r.setUUID(statuses.group_id, request.group_id);
		});
	}

	public static void update(UpdateRequest request, UUID userId) {
		StockComponentHandler.update(statuses.$TABLE, request, userId, a -> {
			request.group_id.ifPresent(i -> a.col(statuses.group_id).set(i));
		});
	}

	public static void delete(UUID id, long revision) throws AlreadyUsedException {
		Utils.delete(statuses.$TABLE, id, revision);
	}
}
