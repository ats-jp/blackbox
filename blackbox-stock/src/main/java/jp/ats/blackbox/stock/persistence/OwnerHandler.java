package jp.ats.blackbox.stock.persistence;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.core.persistence.AlreadyUsedException;
import jp.ats.blackbox.core.persistence.Utils;
import jp.ats.blackbox.stock.persistence.StockComponentHandler.GroupInfo;
import sqlassist.bb_stock.owners;

public class OwnerHandler {

	public static class RegisterRequest extends StockComponentHandler.RegisterRequest {

		public UUID group_id;
	}

	public static class UpdateRequest extends StockComponentHandler.UpdateRequest {

		public Optional<UUID> group_id = Optional.empty();
	}

	public static UUID register(RegisterRequest request, UUID userId) {
		return StockComponentHandler.register(
			owners.$TABLE,
			request,
			new GroupInfo(request.group_id),
			userId,
			r -> {
			});
	}

	public static void update(UpdateRequest request, UUID userId) {
		StockComponentHandler.update(owners.$TABLE, request, userId, a -> {
			request.group_id.ifPresent(i -> a.col(owners.group_id).set(i));
		});
	}

	public static void delete(UUID id, long revision) throws AlreadyUsedException {
		Utils.delete(owners.$TABLE, id, revision);
	}
}
