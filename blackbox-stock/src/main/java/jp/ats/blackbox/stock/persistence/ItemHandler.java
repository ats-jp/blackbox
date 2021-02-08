package jp.ats.blackbox.stock.persistence;

import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.stock.persistence.StockComponentHandler.GroupInfo;
import jp.ats.blackbox.stock.persistence.StockComponentHandler.RegisterInfo;
import sqlassist.bb_stock.items;
import sqlassist.bb_stock.skus;

public class ItemHandler {

	public static class ItemRegisterRequest extends StockComponentHandler.RegisterRequest {

		public UUID group_id;

		public UUID owner_id;
	}

	public static class ItemUpdateRequest extends StockComponentHandler.UpdateRequest {

		public Optional<UUID> group_id = Optional.empty();

		public Optional<UUID> owner_id = Optional.empty();
	}

	public static class SkuRegisterRequest extends StockComponentHandler.RegisterRequest {

		public UUID item_id;
	}

	public static class SkuUpdateRequest extends StockComponentHandler.UpdateRequest {

		public Optional<UUID> item_id = Optional.empty();
	}

	public static UUID register(ItemRegisterRequest request, UUID userId) {
		return StockComponentHandler.register(items.$TABLE, request, new GroupInfo(request.group_id), userId, r -> {
			r.setUUID(items.group_id, request.group_id);
			r.setUUID(items.owner_id, request.owner_id);
		});
	}

	public static void update(ItemUpdateRequest request, UUID userId) {
		StockComponentHandler.update(items.$TABLE, request, userId, a -> {
			request.group_id.ifPresent(i -> a.col(items.group_id).set(i));
			request.owner_id.ifPresent(i -> a.col(items.owner_id).set(i));
		});
	}

	public static void deleteItem(UUID id, long revision) {
		StockComponentHandler.delete(items.$TABLE, id, revision);
	}

	public static UUID register(SkuRegisterRequest request, UUID userId) {
		return StockComponentHandler.register(skus.$TABLE, request, new RegisterInfo() {

			@Override
			public String seqColumn() {
				return skus.seq_in_item;
			}

			@Override
			public String dependsColumn() {
				return skus.item_id;
			}

			@Override
			public UUID dependsId() {
				return request.item_id;
			}

			@Override
			public String generateDefaultCode(long seq) {
				return U.recorder.play(() -> new items().SELECT(a -> a.seq)).fetch(request.item_id).get().getSeq() + "-" + seq;
			}
		}, userId, r -> {
			r.setUUID(skus.item_id, request.item_id);
		});
	}

	public static void update(SkuUpdateRequest request, UUID userId) {
		StockComponentHandler.update(skus.$TABLE, request, userId, a -> {
			request.item_id.ifPresent(i -> a.col(skus.item_id).set(i));
		});
	}

	public static void deleteSku(UUID id, long revision) {
		StockComponentHandler.delete(skus.$TABLE, id, revision);
	}
}
