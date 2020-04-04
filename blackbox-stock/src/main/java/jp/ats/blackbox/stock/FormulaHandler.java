package jp.ats.blackbox.stock;

import static jp.ats.blackbox.persistence.JsonHelper.toJson;
import static org.blendee.sql.Placeholder.$UUID;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.executor.TagExecutor;
import jp.ats.blackbox.persistence.InOut;
import jp.ats.blackbox.persistence.JsonHelper;
import jp.ats.blackbox.persistence.SecurityValues;
import jp.ats.blackbox.persistence.SeqUtils;
import jp.ats.blackbox.persistence.Utils;
import sqlassist.bb.transient_details;
import sqlassist.bb.transients;
import sqlassist.bb_stock.formula_details;
import sqlassist.bb_stock.formulas;

public class FormulaHandler {

	public static class RegisterRequest {

		public UUID group_id;

		public String name;

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public DetailRegisterRequest[] details;
	}

	public static class DetailRegisterRequest {

		public UUID formula_id;

		public UUID stock_id;

		public String name;

		public InOut in_out;

		public BigDecimal quantity;

		public Optional<String> props = Optional.empty();
	}

	public static class UpdateRequest {

		public UUID id;

		public Optional<UUID> group_id = Optional.empty();

		public Optional<String> name = Optional.empty();

		public Optional<String> props = Optional.empty();

		public Optional<String[]> tags = Optional.empty();

		public long revision;

		DetailRegisterRequest[] registerDetails = {};

		DetailUpdateRequest[] updateDetails = {};

		UUID[] deleteDetails = {};
	}

	public static class DetailUpdateRequest {

		public UUID id;

		public Optional<UUID> stock_id = Optional.empty();

		public Optional<InOut> in_out = Optional.empty();

		public Optional<BigDecimal> quantity = Optional.empty();

		public Optional<String> props = Optional.empty();

		public long revision;
	}

	public static UUID register(RegisterRequest request) {
		UUID id = UUID.randomUUID();

		var userId = SecurityValues.currentUserId();

		var formula = formulas.row();

		formula.setId(id);
		formula.setGroup_id(request.group_id);
		formula.setName(request.name);
		request.props.ifPresent(v -> formula.setProps(toJson(v)));
		request.tags.ifPresent(v -> formula.setTags(v));
		formula.setCreated_by(userId);
		formula.setUpdated_by(userId);

		formula.insert();

		request.tags.ifPresent(tags -> TagExecutor.stickTags(tags, id, formulas.$TABLE));

		for (int i = 0; i < request.details.length; i++) {
			registerDetail(id, request.details[i], SeqUtils.compute(i));
		}

		return id;
	}

	public static UUID registerDetail(long formulaRevision, UUID formulaId, DetailRegisterRequest request) {
		Utils.updateRevision(formulas.$TABLE, formulaRevision, formulaId);

		return registerDetail(formulaId, request);
	}

	private static UUID registerDetail(UUID formulaId, DetailRegisterRequest request) {
		int seq = U.recorder.play(
			() -> new formula_details()
				.SELECT(a -> a.MAX(a.seq))
				.WHERE(a -> a.formula_id.eq($UUID)),
			formulaId)
			.executeAndGet(r -> {
				r.next();
				return r.getInt(1);
			});

		return registerDetail(formulaId, request, SeqUtils.computeNextSeq(seq));
	}

	public static UUID registerDetail(long formulaRevision, UUID formulaId, DetailRegisterRequest request, int seq) {
		Utils.updateRevision(formulas.$TABLE, formulaRevision, formulaId);

		return registerDetail(formulaId, request, seq);
	}

	private static UUID registerDetail(UUID formulaId, DetailRegisterRequest request, int seq) {
		var detail = formula_details.row();

		var id = UUID.randomUUID();

		var userId = SecurityValues.currentUserId();

		detail.setId(id);
		detail.setFormula_id(formulaId);
		detail.setStock_id(request.stock_id);
		detail.setIn_out(request.in_out.intValue);
		detail.setSeq(seq);
		detail.setQuantity(request.quantity);
		detail.setCreated_by(userId);

		request.props.ifPresent(v -> detail.setProps(JsonHelper.toJson(v)));

		detail.insert();

		return id;
	}

	public static void update(UpdateRequest request) {
		int result = new formulas().UPDATE(a -> {
			request.group_id.ifPresent(v -> a.group_id.set(v));
			request.name.ifPresent(v -> a.name.set(v));
			a.revision.set(request.revision + 1);
			request.props.ifPresent(v -> a.props.set(JsonHelper.toJson(v)));
			request.tags.ifPresent(v -> a.tags.set((Object) v));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id).AND.revision.eq(request.revision)).execute();

		if (result != 1) throw Utils.decisionException(transient_details.$TABLE, request.id);

		request.tags.ifPresent(tags -> TagExecutor.stickTagsAgain(tags, request.id, formulas.$TABLE));

		Arrays.stream(request.registerDetails).forEach(r -> registerDetail(request.id, r));

		Arrays.stream(request.updateDetails).forEach(r -> updateDetail(r));

		Arrays.stream(request.deleteDetails).forEach(i -> deleteDetail(i));
	}

	public static void upateDetail(long formulaRevision, DetailUpdateRequest request) {
		Utils.updateRevision(formulas.$TABLE, formulaRevision, new formula_details().SELECT(a -> a.$formulas().id).WHERE(a -> a.id.eq(request.id)));

		updateDetail(request);
	}

	private static void updateDetail(DetailUpdateRequest request) {
		int result = new formula_details().UPDATE(a -> {
			request.stock_id.ifPresent(v -> a.stock_id.set(v));
			request.in_out.ifPresent(v -> a.in_out.set(v.intValue));
			request.quantity.ifPresent(v -> a.quantity.set(v));
			request.props.ifPresent(v -> a.props.set(JsonHelper.toJson(v)));
			a.updated_by.set(SecurityValues.currentUserId());
			a.updated_at.setAny("now()");
		}).WHERE(a -> a.id.eq(request.id)).execute();

		if (result != 1) throw Utils.decisionException(formula_details.$TABLE, request.id);
	}

	public static void delete(long formulaRevision, UUID formulaId) {
		Utils.delete(transients.$TABLE, formulaId, formulaRevision);
	}

	public static void deleteDetail(long formulaRevision, UUID detailId) {
		Utils.updateRevision(
			formulas.$TABLE,
			formulaRevision,
			new formula_details().SELECT(a -> a.$formulas().id).WHERE(a -> a.id.eq(detailId)));

		deleteDetail(detailId);
	}

	private static void deleteDetail(UUID detailId) {
		U.recorder.play(() -> new formula_details().DELETE().WHERE(a -> a.id.eq($UUID)), detailId).execute();
	}
}
