package jp.ats.blackbox.stock;

import static org.blendee.sql.Placeholder.$UUID;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.InOut;
import jp.ats.blackbox.persistence.Requests.NodeRegisterRequest;
import jp.ats.blackbox.persistence.UnitHandler;
import sqlassist.bb_stock.stocks;

public class StockHandler {

	public static interface StockComponents {

		UUID groupId();

		UUID skuId();

		UUID ownerId();

		UUID locationId();

		UUID statusId();
	}

	private static class RecorderCacheKey {

		private final Class<?> supplierClass;

		private final int position;

		private RecorderCacheKey(Class<?> supplierClass, int position) {
			this.supplierClass = supplierClass;
			this.position = position;
		}

		@Override
		public int hashCode() {
			return Objects.hash(supplierClass, position);
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof RecorderCacheKey)) return false;
			var another = (RecorderCacheKey) object;
			return another.supplierClass.equals(supplierClass) && another.position == position;
		}
	}

	public static stocks.Row prepareStock(
		Supplier<stocks> supplier,
		UUID userId,
		StockComponents components) {
		//stockが既に存在すればそれを使う
		//なければ新たに登録
		return U.recorder.play(
			() -> new RecorderCacheKey(supplier.getClass(), 0),
			() -> supplier.get()
				.WHERE(a -> a.group_id.eq($UUID).AND.sku_id.eq($UUID).AND.owner_id.eq($UUID).AND.location_id.eq($UUID).AND.status_id.eq($UUID)),
			components.groupId(),
			components.skuId(),
			components.ownerId(),
			components.locationId(),
			components.statusId())
			.willUnique()
			.orElseGet(() -> registerStock(supplier, userId, components));
	}

	public static NodeRegisterRequest buildNodeRegisterRequest(
		UUID userId,
		StockComponents components,
		InOut inOut,
		BigDecimal quantity,
		Optional<Boolean> grantsUnlimited,
		Optional<String> nodeProps,
		Optional<Set<UUID>> allGroupIds) {
		var request = new NodeRegisterRequest();

		request.in_out = inOut;
		request.quantity = quantity;
		request.grants_unlimited = grantsUnlimited;
		request.props = nodeProps;

		var stock = prepareStock(
			() -> new stocks().SELECT(
				a -> a.ls(
					a.id,
					a.group_id,
					a.$items().group_id,
					a.$owners().group_id,
					a.$locations().group_id,
					a.$statuses().group_id)),
			userId,
			components);

		request.unit_id = stock.getId();

		allGroupIds.ifPresent(s -> {
			s.add(stock.getGroup_id());
			s.add(stock.$items().getGroup_id());
			s.add(stock.$owners().getGroup_id());
			s.add(stock.$locations().getGroup_id());
			s.add(stock.$statuses().getGroup_id());
		});

		return request;
	}

	/**
	 * stock登録処理
	 */
	private static stocks.Row registerStock(
		Supplier<stocks> supplier,
		UUID userId,
		StockComponents components) {
		UUID id = UnitHandler.register(userId, components.groupId());

		U.recorder.play(
			() -> new stocks().insertStatement(
				a -> a
					.INSERT(
						a.id,
						a.group_id,
						a.sku_id,
						a.owner_id,
						a.location_id,
						a.status_id,
						a.created_by)
					.VALUES(
						$UUID,
						$UUID,
						$UUID,
						$UUID,
						$UUID,
						$UUID,
						$UUID)),
			id,
			components.groupId(),
			components.skuId(),
			components.ownerId(),
			components.locationId(),
			components.statusId(),
			userId)
			.execute();

		//関連情報取得のため改めて検索
		//必ず存在するためOptional.get()する
		return U.recorder.play(
			() -> new RecorderCacheKey(supplier.getClass(), 1),
			() -> supplier.get()).fetch(id).get();
	}
}
