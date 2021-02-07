package jp.ats.blackbox.stock.persistence;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.stock.persistence.StockComponentHandler.GroupInfo;
import sqlassist.bb_stock.locations;

public class LocationHandler {

	public static class RegisterRequest extends StockComponentHandler.RegisterRequest {

		public UUID group_id;

		public UUID owner_id;
	}

	public static class UpdateRequest extends StockComponentHandler.UpdateRequest {

		public Optional<UUID> group_id = Optional.empty();

		public Optional<UUID> owner_id = Optional.empty();
	}

	public static UUID register(RegisterRequest request, UUID userId) {
		return StockComponentHandler.register(locations.$TABLE, request, new GroupInfo(request.group_id), userId, r -> {
			r.setUUID(locations.group_id, request.group_id);
			r.setUUID(locations.owner_id, request.owner_id);
		});
	}

	public static void update(UpdateRequest request, UUID userId) {
		StockComponentHandler.update(locations.$TABLE, request, userId, a -> {
			request.group_id.ifPresent(i -> a.col(locations.group_id).set(i));
			request.owner_id.ifPresent(i -> a.col(locations.owner_id).set(i));
		});
	}

	public static void delete(UUID id, long revision) {
		StockComponentHandler.delete(locations.$TABLE, id, revision);
	}
}
