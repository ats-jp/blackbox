package jp.ats.blackbox.stock;

import static org.blendee.sql.Placeholder.$UUID;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.gson.Gson;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.InOut;
import jp.ats.blackbox.persistence.JsonHelper;
import jp.ats.blackbox.persistence.JournalHandler.NodeRegisterRequest;
import jp.ats.blackbox.persistence.UnitHandler;
import sqlassist.bb_stock.stocks;

public class StockHandler {

	public static interface StockComponents {

		UUID groupId();

		UUID itemId();

		UUID ownerId();

		UUID locationId();

		UUID statusId();
	}

	public static stocks.Row prepareStock(
		Supplier<stocks> supplier,
		UUID userId,
		StockComponents components) {
		//stockが既に存在すればそれを使う
		//なければ新たに登録
		return U.recorder.play(
			() -> supplier.get()
				.WHERE(a -> a.group_id.eq($UUID).AND.item_id.eq($UUID).AND.owner_id.eq($UUID).AND.location_id.eq($UUID).AND.status_id.eq($UUID)),
			components.groupId(),
			components.itemId(),
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
		Optional<String> nodeProps) {
		var request = new NodeRegisterRequest();

		request.in_out = inOut;
		request.quantity = quantity;
		request.grants_unlimited = grantsUnlimited;
		request.props = nodeProps;

		var stock = prepareStock(
			() -> new stocks().SELECT(
				a -> a.ls(
					a.id,
					a.$groups().props,
					a.$items().props,
					a.$owners().props,
					a.$locations().props,
					a.$statuses().props)),
			userId,
			components);

		request.unit_id = stock.getId();

		var gson = new Gson();

		var props = new UnitProps();

		props.group_props = gson.fromJson(JsonHelper.toString(stock.$groups().getProps()), Map.class);
		props.item_props = gson.fromJson(JsonHelper.toString(stock.$items().getProps()), Map.class);
		props.owner_props = gson.fromJson(JsonHelper.toString(stock.$owners().getProps()), Map.class);
		props.location_props = gson.fromJson(JsonHelper.toString(stock.$locations().getProps()), Map.class);
		props.status_props = gson.fromJson(JsonHelper.toString(stock.$statuses().getProps()), Map.class);

		request.unit_props = Optional.of(gson.toJson(props));

		return request;
	}

	private static class UnitProps {

		@SuppressWarnings("unused")
		private Object group_props;

		@SuppressWarnings("unused")
		private Object item_props;

		@SuppressWarnings("unused")
		private Object owner_props;

		@SuppressWarnings("unused")
		private Object location_props;

		@SuppressWarnings("unused")
		private Object status_props;
	}

	/**
	 * stock登録処理
	 */
	private static stocks.Row registerStock(
		Supplier<stocks> supplier,
		UUID userId,
		StockComponents components) {
		UUID id = UnitHandler.register(userId);

		U.recorder.play(
			() -> new stocks().insertStatement(
				a -> a
					.INSERT(
						a.id,
						a.group_id,
						a.item_id,
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
			components.itemId(),
			components.ownerId(),
			components.locationId(),
			components.statusId(),
			userId)
			.execute();

		//関連情報取得のため改めて検索
		return U.recorder.play(() -> supplier.get()).fetch(id).get();
	}
}