package jp.ats.blackbox.stock;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.stock.StockComponentHandler.GroupInfo;
import sqlassist.bb_stock.statuses;

public class StatusHandler {

	public static class RegisterRequest extends StockComponentHandler.RegisterRequest {

		public UUID group_id;

		public UUID owner_id;
	}

	public static class UpdateRequest extends StockComponentHandler.UpdateRequest {

		public Optional<UUID> group_id = Optional.empty();

		public Optional<UUID> owner_id = Optional.empty();
	}

	public static UUID register(RegisterRequest request, UUID userId) {
		return StockComponentHandler.register(statuses.$TABLE, request, new GroupInfo(request.group_id), userId, r -> {
			r.setUUID(statuses.group_id, request.group_id);
			r.setUUID(statuses.owner_id, request.owner_id);
		});
	}

	public static void update(UpdateRequest request, UUID userId) {
		StockComponentHandler.update(statuses.$TABLE, request, userId, a -> {
			request.group_id.ifPresent(i -> a.col(statuses.group_id).set(i));
			request.owner_id.ifPresent(i -> a.col(statuses.owner_id).set(i));
		});
	}

	public static void delete(UUID id, long revision) {
		StockComponentHandler.delete(statuses.$TABLE, id, revision);
	}
}
