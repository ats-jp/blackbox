package sqlassist.bb_stock;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import java.util.LinkedList;

import org.blendee.jdbc.BPreparedStatement;
import org.blendee.jdbc.ComposedSQL;
import org.blendee.jdbc.ContextManager;
import org.blendee.jdbc.Result;
import org.blendee.jdbc.TablePath;
import org.blendee.orm.ColumnNameDataObjectBuilder;
import org.blendee.orm.DataObject;
import org.blendee.orm.DataObjectIterator;
import org.blendee.orm.SelectContext;
import org.blendee.sql.Bindable;
import org.blendee.sql.Binder;
import org.blendee.sql.Criteria;
import org.blendee.sql.FromClause.JoinType;
import org.blendee.sql.GroupByClause;
import org.blendee.sql.MultiColumn;
import org.blendee.sql.OrderByClause;
import org.blendee.sql.Relationship;
import org.blendee.sql.RelationshipFactory;
import org.blendee.sql.SQLDecorator;
import org.blendee.sql.SQLQueryBuilder;
import org.blendee.sql.ValueExtractor;
import org.blendee.sql.ValueExtractorsConfigure;
import org.blendee.sql.RuntimeId;
import org.blendee.sql.RuntimeIdFactory;
import org.blendee.assist.CriteriaAnyColumn;
import org.blendee.assist.AssistColumn;
import org.blendee.assist.CriteriaAssistColumn;
import org.blendee.assist.CriteriaContext;
import org.blendee.assist.DataManipulationStatement;
import org.blendee.assist.DataManipulationStatementBehavior;
import org.blendee.assist.DataManipulator;
import org.blendee.assist.DeleteStatementIntermediate;
import org.blendee.assist.GroupByColumn;
import org.blendee.assist.GroupByOfferFunction;
import org.blendee.assist.GroupByClauseAssist;
import org.blendee.assist.HavingColumn;
import org.blendee.assist.HavingClauseAssist;
import org.blendee.assist.InsertColumn;
import org.blendee.assist.InsertOfferFunction;
import org.blendee.assist.InsertClauseAssist;
import org.blendee.assist.InsertStatementIntermediate;
import org.blendee.assist.InstantOneToManyQuery;
import org.blendee.assist.annotation.PrimaryKey;
import org.blendee.assist.annotation.ForeignKey;
import org.blendee.assist.Many;
import org.blendee.assist.LogicalOperators;
import org.blendee.assist.OnClause;
import org.blendee.assist.OnLeftColumn;
import org.blendee.assist.OnLeftClauseAssist;
import org.blendee.assist.OnRightColumn;
import org.blendee.assist.OnRightClauseAssist;
import org.blendee.assist.OneToManyQuery;
import org.blendee.assist.OneToManyBehavior;
import org.blendee.assist.OrderByColumn;
import org.blendee.assist.OrderByOfferFunction;
import org.blendee.assist.OrderByClauseAssist;
import org.blendee.assist.Query;
import org.blendee.assist.RightTable;
import org.blendee.assist.Row;
import org.blendee.assist.RowIterator;
import org.blendee.assist.SelectColumn;
import org.blendee.assist.SelectOfferFunction;
import org.blendee.assist.SelectClauseAssist;
import org.blendee.assist.Statement;
import org.blendee.assist.SelectStatement;
import org.blendee.assist.SelectStatementBehavior;
import org.blendee.assist.SelectStatementBehavior.PlaybackQuery;
import org.blendee.assist.TableFacade;
import org.blendee.assist.TableFacadeColumn;
import org.blendee.assist.TableFacadeContext;
import org.blendee.assist.TableFacadeAssist;
import org.blendee.assist.UpdateColumn;
import org.blendee.assist.UpdateClauseAssist;
import org.blendee.assist.UpdateStatementIntermediate;
import org.blendee.assist.WhereColumn;
import org.blendee.assist.WhereClauseAssist;
import org.blendee.assist.SQLDecorators;
import org.blendee.assist.ListSelectClauseAssist;
import org.blendee.assist.ListGroupByClauseAssist;
import org.blendee.assist.ListOrderByClauseAssist;
import org.blendee.assist.ListInsertClauseAssist;
import org.blendee.assist.ListUpdateClauseAssist;
import org.blendee.assist.annotation.Column;
import org.blendee.assist.Helper;
import org.blendee.assist.Vargs;
import org.blendee.assist.annotation.Table;

/**
 * 自動生成されたテーブル操作クラスです。
 * schema: bb_stock<br>
 * name: formula_details<br>
 * type: TABLE<br>
 * remarks: 変換式明細<br>
 * 一変換式の中の入もしくは出を表す<br>
 */
@Table(name = "formula_details", schema = "bb_stock", type = "TABLE", remarks = "変換式明細\n一変換式の中の入もしくは出を表す")
@PrimaryKey(name = "formula_details_pkey", columns = { "id" })
public class formula_details
	extends java.lang.Object
	implements
	TableFacade<Row>,
	SelectStatement,
	SQLDecorators,
	Query<formula_details.Iterator, formula_details.Row>,
	RightTable<formula_details.OnRightAssist> {

	/**
	 * この定数クラスのスキーマ名
	 */
	public static final String SCHEMA = "bb_stock";

	/**
	 * この定数クラスのテーブル名
	 */
	public static final String TABLE = "formula_details";

	/**
	 * この定数クラスのテーブルを指す {@link TablePath}
	 */
	public static final TablePath $TABLE = new TablePath(SCHEMA, TABLE);

	private final Relationship relationship$ = RelationshipFactory.getInstance().getInstance($TABLE);

	private final List<SQLDecorator> decorators$ = new LinkedList<SQLDecorator>();

	/**
	 * name: id<br>
	 * remarks: ID<br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "id", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "ID", defaultValue = "gen_random_uuid()", ordinalPosition = 1, notNull = true)
	public static final String id = "id";

	/**
	 * name: formula_id<br>
	 * remarks: 変換式ID<br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "formula_id", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "変換式ID", defaultValue = "null", ordinalPosition = 2, notNull = true)
	public static final String formula_id = "formula_id";

	/**
	 * name: stock_id<br>
	 * remarks: 対象在庫ID<br>
	 * 構成要素がNULLデータ(00000000-0000-0000-0000-000000000000)は、指定なしとして許容されるが、実施時にstockが特定できるように指定される必要がある<br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "stock_id", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "対象在庫ID\n構成要素がNULLデータ(00000000-0000-0000-0000-000000000000)は、指定なしとして許容されるが、実施時にstockが特定できるように指定される必要がある", defaultValue = "null", ordinalPosition = 3, notNull = true)
	public static final String stock_id = "stock_id";

	/**
	 * name: in_out<br>
	 * remarks: 入出区分<br>
	 * IN=1, OUT=-1<br>
	 * type: int2(5)<br>
	 * not null: true<br>
	 */
	@Column(name = "in_out", type = 5, typeName = "int2", size = 5, hasDecimalDigits = true, decimalDigits = 0, remarks = "入出区分\nIN=1, OUT=-1", defaultValue = "null", ordinalPosition = 4, notNull = true)
	public static final String in_out = "in_out";

	/**
	 * name: seq_in_formula<br>
	 * remarks: 変換式内連番<br>
	 * type: int4(10)<br>
	 * not null: true<br>
	 */
	@Column(name = "seq_in_formula", type = 4, typeName = "int4", size = 10, hasDecimalDigits = true, decimalDigits = 0, remarks = "変換式内連番", defaultValue = "null", ordinalPosition = 5, notNull = true)
	public static final String seq_in_formula = "seq_in_formula";

	/**
	 * name: quantity<br>
	 * remarks: 数量<br>
	 * type: numeric(131089)<br>
	 * not null: true<br>
	 */
	@Column(name = "quantity", type = 2, typeName = "numeric", size = 131089, hasDecimalDigits = true, decimalDigits = 0, remarks = "数量", defaultValue = "null", ordinalPosition = 6, notNull = true)
	public static final String quantity = "quantity";

	/**
	 * name: props<br>
	 * remarks: 外部アプリケーション情報JSON<br>
	 * type: jsonb(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "props", type = 1111, typeName = "jsonb", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "外部アプリケーション情報JSON", defaultValue = "'{}'::jsonb", ordinalPosition = 7, notNull = true)
	public static final String props = "props";

	/**
	 * name: created_at<br>
	 * remarks: <br>
	 * type: timestamptz(35)<br>
	 * not null: true<br>
	 */
	@Column(name = "created_at", type = 93, typeName = "timestamptz", size = 35, hasDecimalDigits = false, decimalDigits = 6, remarks = "", defaultValue = "now()", ordinalPosition = 8, notNull = true)
	public static final String created_at = "created_at";

	/**
	 * name: created_by<br>
	 * remarks: <br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "created_by", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = false, decimalDigits = 0, remarks = "", defaultValue = "null", ordinalPosition = 9, notNull = true)
	public static final String created_by = "created_by";

	/**
	 * name: updated_at<br>
	 * remarks: <br>
	 * type: timestamptz(35)<br>
	 * not null: true<br>
	 */
	@Column(name = "updated_at", type = 93, typeName = "timestamptz", size = 35, hasDecimalDigits = false, decimalDigits = 6, remarks = "", defaultValue = "now()", ordinalPosition = 10, notNull = true)
	public static final String updated_at = "updated_at";

	/**
	 * name: updated_by<br>
	 * remarks: <br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "updated_by", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = false, decimalDigits = 0, remarks = "", defaultValue = "null", ordinalPosition = 11, notNull = true)
	public static final String updated_by = "updated_by";

	/**
	 * name: formula_details_formula_id_fkey<br>
	 * references: formulas<br>
	 * columns: formula_id
	 */
	@ForeignKey(name = "formula_details_formula_id_fkey", references = "bb_stock.formulas", columns = { "formula_id" }, refColumns = { "id" })
	public static final String bb_stock$formulas$formula_details_formula_id_fkey = "formula_details_formula_id_fkey";

	/**
	 * name: formula_details_stock_id_fkey<br>
	 * references: stocks<br>
	 * columns: stock_id
	 */
	@ForeignKey(name = "formula_details_stock_id_fkey", references = "bb_stock.stocks", columns = { "stock_id" }, refColumns = { "id" })
	public static final String bb_stock$stocks$formula_details_stock_id_fkey = "formula_details_stock_id_fkey";

	/**
	 * name: formula_details_created_by_fkey<br>
	 * references: users<br>
	 * columns: created_by
	 */
	@ForeignKey(name = "formula_details_created_by_fkey", references = "bb.users", columns = { "created_by" }, refColumns = { "id" })
	public static final String bb$users$formula_details_created_by_fkey = "formula_details_created_by_fkey";

	/**
	 * name: formula_details_updated_by_fkey<br>
	 * references: users<br>
	 * columns: updated_by
	 */
	@ForeignKey(name = "formula_details_updated_by_fkey", references = "bb.users", columns = { "updated_by" }, refColumns = { "id" })
	public static final String bb$users$formula_details_updated_by_fkey = "formula_details_updated_by_fkey";

	/**
	 * 登録用コンストラクタです。
	 * @return {@link Row}
	 */
	public static Row row() {
		return new Row();
	}

	/**
	 * 参照、更新用コンストラクタです。<br>
	 * aggregate の検索結果からカラム名により値を取り込みます。
	 * @param result 値を持つ {@link Result}
	 * @return {@link Row}
	 */
	public static Row row(Result result) {
		return new Row(result);
	}

	/**
	 * 参照、更新用コンストラクタです。
	 * @param data 値を持つ {@link DataObject}
	 * @return {@link Row}
	 */
	public static Row row(DataObject data) {
		return new Row(data);
	}

	/**
	 * 自動生成された {@link Row} の実装クラスです。
	 */
	public static class Row extends java.lang.Object
		implements org.blendee.assist.Row {

		private final DataObject data$;

		private final Relationship rowRel$ = RelationshipFactory.getInstance().getInstance($TABLE);

		/**
		 * 登録用コンストラクタです。
		 */
		protected Row() {
			data$ = new DataObject(rowRel$);
		}

		/**
		 * 参照、更新用コンストラクタです。
		 * @param data 値を持つ {@link DataObject}
		 */
		protected Row(DataObject data) {
			this.data$ = data;
		}

		/**
		 * 参照、更新用コンストラクタです。<br>
		 * aggregate の検索結果からカラム名により値を取り込みます。
		 * @param result 値を持つ {@link Result}
		 */
		protected Row(Result result) {
			this.data$ = ColumnNameDataObjectBuilder.build(result, rowRel$, ContextManager.get(ValueExtractorsConfigure.class).getValueExtractors());
		}

		@Override
		public DataObject dataObject() {
			return data$;
		}

		@Override
		public TablePath tablePath() {
			return $TABLE;
		}

		/**
		 * setter
		 * name: id<br>
		 * remarks: ID<br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @param value java.util.UUID
		 */
		public void setId(java.util.UUID value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("id").getType());
			data$.setValue("id", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: id<br>
		 * remarks: ID<br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getId() {
			Binder binder = data$.getValue("id");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * setter
		 * name: formula_id<br>
		 * remarks: 変換式ID<br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @param value java.util.UUID
		 */
		public void setFormula_id(java.util.UUID value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("formula_id").getType());
			data$.setValue("formula_id", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: formula_id<br>
		 * remarks: 変換式ID<br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getFormula_id() {
			Binder binder = data$.getValue("formula_id");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * setter
		 * name: stock_id<br>
		 * remarks: 対象在庫ID<br>
		 * 構成要素がNULLデータ(00000000-0000-0000-0000-000000000000)は、指定なしとして許容されるが、実施時にstockが特定できるように指定される必要がある<br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @param value java.util.UUID
		 */
		public void setStock_id(java.util.UUID value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("stock_id").getType());
			data$.setValue("stock_id", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: stock_id<br>
		 * remarks: 対象在庫ID<br>
		 * 構成要素がNULLデータ(00000000-0000-0000-0000-000000000000)は、指定なしとして許容されるが、実施時にstockが特定できるように指定される必要がある<br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getStock_id() {
			Binder binder = data$.getValue("stock_id");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * setter
		 * name: in_out<br>
		 * remarks: 入出区分<br>
		 * IN=1, OUT=-1<br>
		 * type: int2(5)<br>
		 * not null: true<br>
		 * @param value java.lang.Integer
		 */
		public void setIn_out(java.lang.Integer value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("in_out").getType());
			data$.setValue("in_out", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: in_out<br>
		 * remarks: 入出区分<br>
		 * IN=1, OUT=-1<br>
		 * type: int2(5)<br>
		 * not null: true<br>
		 * @return java.lang.Integer
		 */
		public java.lang.Integer getIn_out() {
			Binder binder = data$.getValue("in_out");
			return (java.lang.Integer) binder.getValue();
		}

		/**
		 * setter
		 * name: seq_in_formula<br>
		 * remarks: 変換式内連番<br>
		 * type: int4(10)<br>
		 * not null: true<br>
		 * @param value java.lang.Integer
		 */
		public void setSeq_in_formula(java.lang.Integer value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("seq_in_formula").getType());
			data$.setValue("seq_in_formula", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: seq_in_formula<br>
		 * remarks: 変換式内連番<br>
		 * type: int4(10)<br>
		 * not null: true<br>
		 * @return java.lang.Integer
		 */
		public java.lang.Integer getSeq_in_formula() {
			Binder binder = data$.getValue("seq_in_formula");
			return (java.lang.Integer) binder.getValue();
		}

		/**
		 * setter
		 * name: quantity<br>
		 * remarks: 数量<br>
		 * type: numeric(131089)<br>
		 * not null: true<br>
		 * @param value java.math.BigDecimal
		 */
		public void setQuantity(java.math.BigDecimal value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("quantity").getType());
			data$.setValue("quantity", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: quantity<br>
		 * remarks: 数量<br>
		 * type: numeric(131089)<br>
		 * not null: true<br>
		 * @return java.math.BigDecimal
		 */
		public java.math.BigDecimal getQuantity() {
			Binder binder = data$.getValue("quantity");
			return (java.math.BigDecimal) binder.getValue();
		}

		/**
		 * setter
		 * name: props<br>
		 * remarks: 外部アプリケーション情報JSON<br>
		 * type: jsonb(2147483647)<br>
		 * not null: true<br>
		 * @param value java.lang.Object
		 */
		public void setProps(java.lang.Object value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("props").getType());
			data$.setValue("props", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: props<br>
		 * remarks: 外部アプリケーション情報JSON<br>
		 * type: jsonb(2147483647)<br>
		 * not null: true<br>
		 * @return java.lang.Object
		 */
		public java.lang.Object getProps() {
			Binder binder = data$.getValue("props");
			return binder.getValue();
		}

		/**
		 * setter
		 * name: created_at<br>
		 * remarks: <br>
		 * type: timestamptz(35)<br>
		 * not null: true<br>
		 * @param value java.sql.Timestamp
		 */
		public void setCreated_at(java.sql.Timestamp value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("created_at").getType());
			data$.setValue("created_at", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: created_at<br>
		 * remarks: <br>
		 * type: timestamptz(35)<br>
		 * not null: true<br>
		 * @return java.sql.Timestamp
		 */
		public java.sql.Timestamp getCreated_at() {
			Binder binder = data$.getValue("created_at");
			return (java.sql.Timestamp) binder.getValue();
		}

		/**
		 * setter
		 * name: created_by<br>
		 * remarks: <br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @param value java.util.UUID
		 */
		public void setCreated_by(java.util.UUID value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("created_by").getType());
			data$.setValue("created_by", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: created_by<br>
		 * remarks: <br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getCreated_by() {
			Binder binder = data$.getValue("created_by");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * setter
		 * name: updated_at<br>
		 * remarks: <br>
		 * type: timestamptz(35)<br>
		 * not null: true<br>
		 * @param value java.sql.Timestamp
		 */
		public void setUpdated_at(java.sql.Timestamp value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("updated_at").getType());
			data$.setValue("updated_at", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: updated_at<br>
		 * remarks: <br>
		 * type: timestamptz(35)<br>
		 * not null: true<br>
		 * @return java.sql.Timestamp
		 */
		public java.sql.Timestamp getUpdated_at() {
			Binder binder = data$.getValue("updated_at");
			return (java.sql.Timestamp) binder.getValue();
		}

		/**
		 * setter
		 * name: updated_by<br>
		 * remarks: <br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @param value java.util.UUID
		 */
		public void setUpdated_by(java.util.UUID value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("updated_by").getType());
			data$.setValue("updated_by", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: updated_by<br>
		 * remarks: <br>
		 * type: uuid(2147483647)<br>
		 * not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getUpdated_by() {
			Binder binder = data$.getValue("updated_by");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 formulas<br>
		 * 外部キー名 formula_details_formula_id_fkey<br>
		 * 項目名 formula_id
		 * @return 参照しているレコードの Row
		 */
		public sqlassist.bb_stock.formulas.Row $formulas() {
			return sqlassist.bb_stock.formulas.row(
				data$.getDataObject(bb_stock$formulas$formula_details_formula_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 stocks<br>
		 * 外部キー名 formula_details_stock_id_fkey<br>
		 * 項目名 stock_id
		 * @return 参照しているレコードの Row
		 */
		public sqlassist.bb_stock.stocks.Row $stocks() {
			return sqlassist.bb_stock.stocks.row(
				data$.getDataObject(bb_stock$stocks$formula_details_stock_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 users<br>
		 * 外部キー名 formula_details_created_by_fkey<br>
		 * 項目名 created_by
		 * @return 参照しているレコードの Row
		 */
		public sqlassist.bb.users.Row $users$formula_details_created_by_fkey() {
			return sqlassist.bb.users.row(
				data$.getDataObject(bb$users$formula_details_created_by_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 users<br>
		 * 外部キー名 formula_details_updated_by_fkey<br>
		 * 項目名 updated_by
		 * @return 参照しているレコードの Row
		 */
		public sqlassist.bb.users.Row $users$formula_details_updated_by_fkey() {
			return sqlassist.bb.users.row(
				data$.getDataObject(bb$users$formula_details_updated_by_fkey));
		}
	}

	private static final TableFacadeContext<SelectCol> selectContext$ = (assist, name) -> new SelectCol(assist, name);

	private static final TableFacadeContext<GroupByCol> groupByContext$ = (assist, name) -> new GroupByCol(assist, name);

	private static final TableFacadeContext<OrderByCol> orderByContext$ = (assist, name) -> new OrderByCol(assist, name);

	private static final TableFacadeContext<InsertCol> insertContext$ = (assist, name) -> new InsertCol(assist, name);

	private static final TableFacadeContext<UpdateCol> updateContext$ = (assist, name) -> new UpdateCol(assist, name);

	private static final TableFacadeContext<WhereColumn<WhereLogicalOperators>> whereContext$ = TableFacadeContext.newWhereBuilder();

	private static final TableFacadeContext<HavingColumn<HavingLogicalOperators>> havingContext$ = TableFacadeContext.newHavingBuilder();

	private static final TableFacadeContext<OnLeftColumn<OnLeftLogicalOperators>> onLeftContext$ = TableFacadeContext.newOnLeftBuilder();

	private static final TableFacadeContext<OnRightColumn<OnRightLogicalOperators>> onRightContext$ = TableFacadeContext.newOnRightBuilder();

	private static final TableFacadeContext<WhereColumn<DMSWhereLogicalOperators>> dmsWhereContext$ = TableFacadeContext.newDMSWhereBuilder();

	/**
	 * WHERE 句 で使用する AND, OR です。
	 */
	public class WhereLogicalOperators implements LogicalOperators<WhereAssist> {

		private WhereLogicalOperators() {
		}

		/**
		 * WHERE 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final WhereAssist OR = new WhereAssist(
			formula_details.this,
			whereContext$,
			CriteriaContext.OR,
			null);

		/**
		 * WHERE 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final WhereAssist AND = new WhereAssist(
			formula_details.this,
			whereContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public WhereAssist defaultOperator() {
			return AND;
		}
	}

	/**
	 * HAVING 句 で使用する AND, OR です。
	 */
	public class HavingLogicalOperators implements LogicalOperators<HavingAssist> {

		private HavingLogicalOperators() {
		}

		/**
		 * HAVING 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final HavingAssist OR = new HavingAssist(
			formula_details.this,
			havingContext$,
			CriteriaContext.OR,
			null);

		/**
		 * HAVING 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final HavingAssist AND = new HavingAssist(
			formula_details.this,
			havingContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public HavingAssist defaultOperator() {
			return AND;
		}
	}

	/**
	 * ON 句 (LEFT) で使用する AND, OR です。
	 */
	public class OnLeftLogicalOperators implements LogicalOperators<OnLeftAssist> {

		private OnLeftLogicalOperators() {
		}

		/**
		 * ON 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnLeftAssist OR = new OnLeftAssist(
			formula_details.this,
			onLeftContext$,
			CriteriaContext.OR,
			null);

		/**
		 * ON 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnLeftAssist AND = new OnLeftAssist(
			formula_details.this,
			onLeftContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public OnLeftAssist defaultOperator() {
			return AND;
		}
	}

	/**
	 * ON 句 (RIGHT) で使用する AND, OR です。
	 */
	public class OnRightLogicalOperators implements LogicalOperators<OnRightAssist> {

		private OnRightLogicalOperators() {
		}

		/**
		 * ON 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnRightAssist OR = new OnRightAssist(
			formula_details.this,
			onRightContext$,
			CriteriaContext.OR,
			null);

		/**
		 * ON 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnRightAssist AND = new OnRightAssist(
			formula_details.this,
			onRightContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public OnRightAssist defaultOperator() {
			return AND;
		}
	}

	/**
	 * WHERE 句 で使用する AND, OR です。
	 */
	public class DMSWhereLogicalOperators implements LogicalOperators<DMSWhereAssist> {

		private DMSWhereLogicalOperators() {
		}

		/**
		 * WHERE 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final DMSWhereAssist OR = new DMSWhereAssist(
			formula_details.this,
			dmsWhereContext$,
			CriteriaContext.OR,
			null);

		/**
		 * WHERE 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final DMSWhereAssist AND = new DMSWhereAssist(
			formula_details.this,
			dmsWhereContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public DMSWhereAssist defaultOperator() {
			return AND;
		}
	}

	private OnRightLogicalOperators onRightOperators$;

	private RuntimeId id$;

	private SelectBehavior selectBehavior$;

	private SelectBehavior selectBehavior() {
		return selectBehavior$ == null ? (selectBehavior$ = new SelectBehavior()) : selectBehavior$;
	}

	@Override
	public RuntimeId getRuntimeId() {
		return id$ == null ? (id$ = RuntimeIdFactory.runtimeInstance()) : id$;
	}

	private class SelectBehavior extends SelectStatementBehavior<SelectAssist, ListSelectAssist, GroupByAssist, ListGroupByAssist, WhereAssist, HavingAssist, OrderByAssist, ListOrderByAssist, OnLeftAssist> {

		private SelectBehavior() {
			super($TABLE, getRuntimeId(), formula_details.this);
		}

		@Override
		protected SelectAssist newSelect() {
			return new SelectAssist(
				formula_details.this,
				selectContext$);
		}

		@Override
		protected ListSelectAssist newListSelect() {
			return new ListSelectAssist(
				formula_details.this,
				selectContext$);
		}

		@Override
		protected GroupByAssist newGroupBy() {
			return new GroupByAssist(
				formula_details.this,
				groupByContext$);
		}

		@Override
		protected ListGroupByAssist newListGroupBy() {
			return new ListGroupByAssist(
				formula_details.this,
				groupByContext$);
		}

		@Override
		protected OrderByAssist newOrderBy() {
			return new OrderByAssist(
				formula_details.this,
				orderByContext$);
		}

		@Override
		protected ListOrderByAssist newListOrderBy() {
			return new ListOrderByAssist(
				formula_details.this,
				orderByContext$);
		}

		@Override
		protected WhereLogicalOperators newWhereOperators() {
			return new WhereLogicalOperators();
		}

		@Override
		protected HavingLogicalOperators newHavingOperators() {
			return new HavingLogicalOperators();
		}

		@Override
		protected OnLeftLogicalOperators newOnLeftOperators() {
			return new OnLeftLogicalOperators();
		}
	}

	private DMSBehavior dmsBehavior$;

	private DMSBehavior dmsBehavior() {
		return dmsBehavior$ == null ? (dmsBehavior$ = new DMSBehavior()) : dmsBehavior$;
	}

	private class DMSBehavior extends DataManipulationStatementBehavior<InsertAssist, ListInsertAssist, UpdateAssist, ListUpdateAssist, DMSWhereAssist> {

		public DMSBehavior() {
			super(
				$TABLE,
				relationship$,
				formula_details.this.getRuntimeId(),
				formula_details.this);
		}

		@Override
		protected InsertAssist newInsert() {
			return new InsertAssist(
				formula_details.this,
				insertContext$);
		}

		@Override
		protected ListInsertAssist newListInsert() {
			return new ListInsertAssist(
				formula_details.this,
				insertContext$);
		}

		@Override
		protected UpdateAssist newUpdate() {
			return new UpdateAssist(
				formula_details.this,
				updateContext$);
		}

		@Override
		protected ListUpdateAssist newListUpdate() {
			return new ListUpdateAssist(
				formula_details.this,
				updateContext$);
		}

		@Override
		protected LogicalOperators<DMSWhereAssist> newWhereOperators() {
			return new DMSWhereLogicalOperators();
		}
	}

	/**
	 * 空のインスタンスを生成します。
	 */
	public formula_details() {
	}

	/**
	 * このクラスのインスタンスを生成します。<br>
	 * このコンストラクタで生成されたインスタンス の SELECT 句で使用されるカラムは、 パラメータの {@link SelectContext} に依存します。
	 * @param context SELECT 句を決定する
	 */
	public formula_details(SelectContext context) {
		selectBehavior().setSelectContext(Objects.requireNonNull(context));
	}

	@Override
	public Row createRow(DataObject data) {
		return new Row(data);
	}

	@Override
	public TablePath getTablePath() {
		return $TABLE;
	}

	/**
	 *  {@link DataObjectIterator} を {@link RowIterator} に変換します。
	 * @param base 変換される {@link DataObjectIterator}
	 * @return {@link RowIterator}
	 */
	public Iterator wrap(DataObjectIterator base) {
		return new Iterator(base);
	}

	/**
	 * Iterator クラスです。
	 */
	public class Iterator extends RowIterator<Row> {

		/**
		 * 唯一のコンストラクタです。
		 * @param iterator
		 */
		private Iterator(
			DataObjectIterator iterator) {
			super(iterator);
		}

		@Override
		public Row next() {
			return createRow(nextDataObject());
		}
	}

	/**
	 * この {@link SelectStatement} のテーブルを表す {@link TableFacadeAssist} を参照するためのインスタンスです。
	 * @return assist
	 */
	public ExtAssist<TableFacadeColumn, Void> assist() {
		return new ExtAssist<>(this, TableFacadeContext.OTHER, CriteriaContext.NULL);
	}

	/**
	 * SELECT 句を作成する {@link Consumer}
	 * @param consumer {@link Consumer}
	 * @return this
	 */
	public formula_details selectClause(Consumer<ListSelectAssist> consumer) {
		selectBehavior().selectClause(consumer);
		return this;
	}

	/**
	 * GROUP BY 句を作成する {@link Consumer}
	 * @param consumer {@link Consumer}
	 * @return this
	 */
	public formula_details groupByClause(Consumer<ListGroupByAssist> consumer) {
		selectBehavior().groupByClause(consumer);
		return this;
	}

	/**
	 * GROUP BY 句を作成する {@link Consumer}
	 * @param consumer {@link Consumer}
	 * @return this
	 */
	public formula_details orderByClause(Consumer<ListOrderByAssist> consumer) {
		selectBehavior().orderByClause(consumer);
		return this;
	}

	/**
	 * SELECT 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public formula_details SELECT(
		SelectOfferFunction<SelectAssist> function) {
		selectBehavior().SELECT(function);
		return this;
	}

	/**
	 * DISTINCT を使用した SELECT 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public formula_details SELECT_DISTINCT(
		SelectOfferFunction<SelectAssist> function) {
		selectBehavior().SELECT_DISTINCT(function);
		return this;
	}

	/**
	 * COUNT(*) を使用した SELECT 句を記述します。
	 * @return この {@link SelectStatement}
	 */
	public formula_details SELECT_COUNT() {
		selectBehavior().SELECT_COUNT();
		return this;
	}

	/**
	 * GROUP BY 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public formula_details GROUP_BY(
		GroupByOfferFunction<GroupByAssist> function) {
		selectBehavior().GROUP_BY(function);
		return this;
	}

	/**
	 * WHERE 句を記述します。
	 * @param consumers
	 * @return この {@link SelectStatement}
	 */
	@SafeVarargs
	public final formula_details WHERE(
		Consumer<WhereAssist>... consumers) {
		selectBehavior().WHERE(consumers);
		return this;
	}

	/**
	 * WHERE 句で使用できる {@link  Criteria} を作成します。
	 * @param consumer {@link Consumer}
	 * @return {@link Criteria}
	 */
	public Criteria createWhereCriteria(
		Consumer<WhereAssist> consumer) {
		return selectBehavior().createWhereCriteria(consumer);
	}

	/**
	 * HAVING 句を記述します。
	 * @param consumers
	 * @return この {@link SelectStatement}
	 */
	@SafeVarargs
	public final formula_details HAVING(
		Consumer<HavingAssist>... consumers) {
		selectBehavior().HAVING(consumers);
		return this;
	}

	/**
	 * HAVING 句で使用できる {@link  Criteria} を作成します。
	 * @param consumer {@link Consumer}
	 * @return {@link Criteria}
	 */
	public Criteria createHavingCriteria(
		Consumer<HavingAssist> consumer) {
		return selectBehavior().createHavingCriteria(consumer);
	}

	/**
	 * このクエリに INNER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, formula_details> INNER_JOIN(RightTable<R> right) {
		return selectBehavior().INNER_JOIN(right, this);
	}

	/**
	 * このクエリに LEFT OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, formula_details> LEFT_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().LEFT_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに RIGHT OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, formula_details> RIGHT_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().RIGHT_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに FULL OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, formula_details> FULL_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().FULL_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに CROSS JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return この {@link SelectStatement}
	 */
	public <R extends OnRightClauseAssist<?>> formula_details CROSS_JOIN(RightTable<R> right) {
		selectBehavior().CROSS_JOIN(right);
		return this;
	}

	/**
	 * UNION するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select UNION 対象
	 * @return この {@link SelectStatement}
	 */
	public formula_details UNION(SelectStatement select) {
		selectBehavior().UNION(select);
		return this;
	}

	/**
	 * UNION ALL するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select UNION ALL 対象
	 * @return この {@link SelectStatement}
	 */
	public formula_details UNION_ALL(SelectStatement select) {
		selectBehavior().UNION_ALL(select);
		return this;
	}

	/**
	 * INTERSECT するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select INTERSECT 対象
	 * @return この {@link SelectStatement}
	 */
	public formula_details INTERSECT(SelectStatement select) {
		selectBehavior().INTERSECT(select);
		return this;
	}

	/**
	 * INTERSECT ALL するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select INTERSECT ALL 対象
	 * @return この {@link SelectStatement}
	 */
	public formula_details INTERSECT_ALL(SelectStatement select) {
		selectBehavior().INTERSECT_ALL(select);
		return this;
	}

	/**
	 * EXCEPT するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select EXCEPT 対象
	 * @return この {@link SelectStatement}
	 */
	public formula_details EXCEPT(SelectStatement select) {
		selectBehavior().INTERSECT(select);
		return this;
	}

	/**
	 * EXCEPT ALL するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select EXCEPT ALL 対象
	 * @return この {@link SelectStatement}
	 */
	public formula_details EXCEPT_ALL(SelectStatement select) {
		selectBehavior().EXCEPT_ALL(select);
		return this;
	}

	/**
	 * ORDER BY 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public formula_details ORDER_BY(
		OrderByOfferFunction<OrderByAssist> function) {
		selectBehavior().ORDER_BY(function);
		return this;
	}

	@Override
	public boolean hasWhereClause() {
		return selectBehavior().hasWhereClause();
	}

	/**
	 * 新規に GROUP BY 句をセットします。
	 * @param clause 新 ORDER BY 句
	 * @return {@link SelectStatement} 自身
	 * @throws IllegalStateException 既に ORDER BY 句がセットされている場合
	 */
	public formula_details setGroupByClause(GroupByClause clause) {
		selectBehavior().setGroupByClause(clause);
		return this;
	}

	/**
	 * 新規に ORDER BY 句をセットします。
	 * @param clause 新 ORDER BY 句
	 * @return {@link SelectStatement} 自身
	 * @throws IllegalStateException 既に ORDER BY 句がセットされている場合
	 */
	public formula_details setOrderByClause(OrderByClause clause) {
		selectBehavior().setOrderByClause(clause);
		return this;
	}

	/**
	 * 現時点の WHERE 句に新たな条件を AND 結合します。<br>
	 * AND 結合する対象がなければ、新条件としてセットされます。
	 * @param criteria AND 結合する新条件
	 * @return {@link SelectStatement} 自身
	 */
	public formula_details and(Criteria criteria) {
		selectBehavior().and(criteria);
		return this;
	}

	/**
	 * 現時点の WHERE 句に新たな条件を OR 結合します。<br>
	 * OR 結合する対象がなければ、新条件としてセットされます。
	 * @param criteria OR 結合する新条件
	 * @return {@link SelectStatement} 自身
	 */
	public formula_details or(Criteria criteria) {
		selectBehavior().or(criteria);
		return this;
	}

	/**
	 * 生成された SQL 文を加工する {SQLDecorator} を設定します。
	 * @param decorators {@link SQLDecorator}
	 * @return {@link SelectStatement} 自身
	 */
	@Override
	public formula_details accept(SQLDecorator... decorators) {
		for (SQLDecorator decorator : decorators) {
			this.decorators$.add(decorator);
		}

		return this;
	}

	@Override
	public SelectContext getSelectContext() {
		return selectBehavior().getSelectContext();
	}

	@Override
	public GroupByClause getGroupByClause() {
		return selectBehavior().getGroupByClause();
	}

	@Override
	public OrderByClause getOrderByClause() {
		return selectBehavior().getOrderByClause();
	}

	@Override
	public Criteria getWhereClause() {
		return selectBehavior().getWhereClause();
	}

	@Override
	public Relationship getRootRealtionship() {
		return relationship$;
	}

	@Override
	public LogicalOperators<WhereAssist> getWhereLogicalOperators() {
		return selectBehavior().whereOperators();
	}

	@Override
	public LogicalOperators<HavingAssist> getHavingLogicalOperators() {
		return selectBehavior().havingOperators();
	}

	@Override
	public LogicalOperators<OnLeftAssist> getOnLeftLogicalOperators() {
		return selectBehavior().onLeftOperators();
	}

	@Override
	public OnRightLogicalOperators getOnRightLogicalOperators() {
		return onRightOperators$ == null ? (onRightOperators$ = new OnRightLogicalOperators()) : onRightOperators$;
	}

	@Override
	public SQLDecorator[] decorators() {
		return decorators$.toArray(new SQLDecorator[decorators$.size()]);
	}

	@Override
	public Iterator retrieve() {
		SelectBehavior selectBehavior = selectBehavior();
		selectBehavior.checkRowMode();
		return wrap(selectBehavior.query().retrieve());
	}

	@Override
	public Optional<Row> fetch(String... primaryKeyMembers) {
		SelectBehavior selectBehavior = selectBehavior();
		selectBehavior.checkRowMode();
		return selectBehavior.query().fetch(primaryKeyMembers).map(o -> createRow(o));
	}

	@Override
	public Optional<Row> fetch(Number... primaryKeyMembers) {
		SelectBehavior selectBehavior = selectBehavior();
		selectBehavior.checkRowMode();
		return selectBehavior.query().fetch(primaryKeyMembers).map(o -> createRow(o));
	}

	@Override
	public Optional<Row> fetch(Bindable... primaryKeyMembers) {
		SelectBehavior selectBehavior = selectBehavior();
		selectBehavior.checkRowMode();
		return selectBehavior.query().fetch(primaryKeyMembers).map(o -> createRow(o));
	}

	@Override
	public int count() {
		SelectBehavior selectBehavior = selectBehavior();
		selectBehavior.checkRowMode();
		return selectBehavior.query().count();
	}

	@Override
	public ComposedSQL countSQL() {
		SelectBehavior selectBehavior = selectBehavior();
		selectBehavior.checkRowMode();
		return selectBehavior.query().countSQL();
	}

	@Override
	public ComposedSQL aggregateSQL() {
		selectBehavior().quitRowMode();
		return this;
	}

	@Override
	public String sql() {
		return selectBehavior().composeSQL().sql();
	}

	@Override
	public int complement(int done, BPreparedStatement statement) {
		return selectBehavior().composeSQL().complement(done, statement);
	}

	@Override
	public Query reproduce(Object... placeHolderValues) {
		return new Query(selectBehavior().query().reproduce(placeHolderValues));
	}

	@Override
	public Query reproduce() {
		return new Query(selectBehavior().query().reproduce());
	}

	@Override
	public Binder[] currentBinders() {
		return selectBehavior().query().currentBinders();
	}

	@Override
	public void joinTo(SQLQueryBuilder builder, JoinType joinType, Criteria onCriteria) {
		selectBehavior().joinTo(builder, joinType, onCriteria);
	}

	@Override
	public SQLQueryBuilder toSQLQueryBuilder() {
		return selectBehavior().buildBuilder();
	}

	@Override
	public void forSubquery(boolean forSubquery) {
		selectBehavior().forSubquery(forSubquery);
	}

	/**
	 * 現在保持している SELECT 文の WHERE 句をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetWhere() {
		selectBehavior().resetWhere();
		return this;
	}

	/**
	 * 現在保持している HAVING 句をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetHaving() {
		selectBehavior().resetHaving();
		return this;
	}

	/**
	 * 現在保持している SELECT 句をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetSelect() {
		selectBehavior().resetSelect();
		return this;
	}

	/**
	 * 現在保持している GROUP BY 句をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetGroupBy() {
		selectBehavior().resetGroupBy();
		return this;
	}

	/**
	 * 現在保持している ORDER BY 句をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetOrderBy() {
		selectBehavior().resetOrderBy();
		return this;
	}

	/**
	 * 現在保持している UNION をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetUnions() {
		selectBehavior().resetUnions();
		return this;
	}

	/**
	 * 現在保持している JOIN をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetJoins() {
		selectBehavior().resetJoins();
		return this;
	}

	/**
	 * 現在保持している INSERT 文のカラムをリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetInsert() {
		dmsBehavior().resetInsert();
		return this;
	}

	/**
	 * 現在保持している UPDATE 文の更新要素をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetUpdate() {
		dmsBehavior().resetUpdate();
		return this;
	}

	/**
	 * 現在保持している SET 文の更新要素をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetDelete() {
		dmsBehavior().resetDelete();
		return this;
	}

	/**
	 * 現在保持している {@link SQLDecorator} をリセットします。
	 * @return このインスタンス
	 */
	public formula_details resetDecorators() {
		decorators$.clear();
		return this;
	}

	/**
	 * 現在保持している条件、並び順をリセットします。
	 * @return このインスタンス
	 */
	public formula_details reset() {
		selectBehavior().reset();
		dmsBehavior().reset();
		resetDecorators();
		return this;
	}

	@Override
	public void quitRowMode() {
		selectBehavior().quitRowMode();
	}

	@Override
	public boolean rowMode() {
		return selectBehavior().rowMode();
	}

	@Override
	public ComposedSQL composeSQL() {
		return selectBehavior().composeSQL();
	}

	@Override
	public OnRightAssist joint() {
		return getOnRightLogicalOperators().AND;
	}

	@Override
	public SelectStatement getSelectStatement() {
		return this;
	}

	/**
	 * INSERT 文を作成する {@link Function}
	 * @param function {@link Function}
	 * @return {@link DataManipulator}
	 */
	public DataManipulator insertStatement(Function<ListInsertAssist, DataManipulator> function) {
		return dmsBehavior().insertStatement(function);
	}

	/**
	 * UPDATE 文を作成する {@link Function}
	 * @param function {@link Function}
	 * @return {@link DataManipulator}
	 */
	public DataManipulator updateStatement(Function<ListUpdateAssist, DataManipulator> function) {
		return dmsBehavior().updateStatement(function);
	}

	/**
	 * INSERT 文を生成します。
	 * @param function function
	 * @return {@link InsertStatementIntermediate}
	 */
	public InsertStatementIntermediate INSERT(InsertOfferFunction<InsertAssist> function) {
		return dmsBehavior().INSERT(function);
	}

	/**
	 * INSERT 文を生成します。<br>
	 * このインスタンスが現時点で保持しているカラムを使用します。<br>
	 * 以前使用した VALUES の値はクリアされています。
	 * @return {@link InsertStatementIntermediate}
	 */
	public InsertStatementIntermediate INSERT() {
		return dmsBehavior().INSERT();
	}

	/**
	 * INSERT 文を生成します。
	 * @param function function
	 * @param select select
	 * @return {@link InsertStatementIntermediate}
	 */
	public DataManipulator INSERT(InsertOfferFunction<InsertAssist> function, SelectStatement select) {
		return dmsBehavior().INSERT(function, select);
	}

	/**
	 * INSERT 文を生成します。
	 * @param select select
	 * @return {@link InsertStatementIntermediate}
	 */
	public DataManipulator INSERT(SelectStatement select) {
		return dmsBehavior().INSERT(select);
	}

	/**
	 * UPDATE 文を生成します。
	 * @param consumer
	 * @return {@link UpdateStatementIntermediate}
	 */
	public UpdateStatementIntermediate<DMSWhereAssist> UPDATE(Consumer<UpdateAssist> consumer) {
		return dmsBehavior().UPDATE(consumer);
	}

	/**
	 * UPDATE 文を生成します。
	 * @return {@link UpdateStatementIntermediate}
	 */
	public UpdateStatementIntermediate<DMSWhereAssist> UPDATE() {
		return dmsBehavior().UPDATE();
	}

	/**
	 * DELETE 文を生成します。
	 * @return {@link DeleteStatementIntermediate}
	 */
	public final DeleteStatementIntermediate<DMSWhereAssist> DELETE() {
		return dmsBehavior().DELETE();
	}

	@Override
	public String toString() {
		return selectBehavior().toString();
	}

	/**
	 * 自動生成された {@link TableFacadeAssist} の実装クラスです。<br>
	 * 条件として使用できるカラムを内包しており、それらを使用して検索 SQL を生成可能にします。
	 * @param <T> 使用されるカラムのタイプにあった型
	 * @param <M> Many 一対多の多側の型連鎖
	 */
	public static class Assist<T, M> implements TableFacadeAssist {

		final formula_details table$;

		private final CriteriaContext context$;

		private final TableFacadeAssist parent$;

		private final String fkName$;

		/**
		 * 項目名 id
		 */
		public final T id;

		/**
		 * 項目名 formula_id
		 */
		public final T formula_id;

		/**
		 * 項目名 stock_id
		 */
		public final T stock_id;

		/**
		 * 項目名 in_out
		 */
		public final T in_out;

		/**
		 * 項目名 seq_in_formula
		 */
		public final T seq_in_formula;

		/**
		 * 項目名 quantity
		 */
		public final T quantity;

		/**
		 * 項目名 props
		 */
		public final T props;

		/**
		 * 項目名 created_at
		 */
		public final T created_at;

		/**
		 * 項目名 created_by
		 */
		public final T created_by;

		/**
		 * 項目名 updated_at
		 */
		public final T updated_at;

		/**
		 * 項目名 updated_by
		 */
		public final T updated_by;

		private Assist(
			formula_details table$,
			TableFacadeContext<T> builder$,
			CriteriaContext context$,
			TableFacadeAssist parent$,
			String fkName$) {
			this.table$ = table$;
			this.context$ = context$;
			this.parent$ = parent$;
			this.fkName$ = fkName$;

			this.id = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.id);
			this.formula_id = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.formula_id);
			this.stock_id = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.stock_id);
			this.in_out = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.in_out);
			this.seq_in_formula = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.seq_in_formula);
			this.quantity = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.quantity);
			this.props = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.props);
			this.created_at = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.created_at);
			this.created_by = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.created_by);
			this.updated_at = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.updated_at);
			this.updated_by = builder$.buildColumn(this, sqlassist.bb_stock.formula_details.updated_by);
		}

		/**
		 * 直接使用しないでください。
		 * @param builder$ builder
		 * @param parent$ parent
		 * @param fkName$ fkName
		 */
		public Assist(
			TableFacadeContext<T> builder$,
			TableFacadeAssist parent$,
			String fkName$) {
			this(null, builder$, null, parent$, fkName$);
		}

		private Assist(
			formula_details table$,
			TableFacadeContext<T> builder$,
			CriteriaContext context$) {
			this(table$, builder$, context$, null, null);
		}

		@Override
		public CriteriaContext getContext() {
			if (context$ == null) return parent$.getContext();

			return context$;
		}

		@Override
		public Relationship getRelationship() {
			if (parent$ != null) {
				return parent$.getRelationship().find(fkName$);
			}

			return table$.relationship$;
		}

		@Override
		public SelectStatement getSelectStatement() {
			if (table$ != null) return table$;
			return parent$.getSelectStatement();
		}

		@Override
		public DataManipulationStatement getDataManipulationStatement() {
			if (table$ != null) return table$.dmsBehavior();
			return parent$.getDataManipulationStatement();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof TableFacadeAssist)) return false;
			return getRelationship()
				.equals(((TableFacadeAssist) o).getRelationship());
		}

		@Override
		public int hashCode() {
			return getRelationship().hashCode();
		}

		@Override
		public OneToManyBehavior getOneToManyBehavior() {
			return new OneToManyBehavior(
				parent$ == null ? null : parent$.getOneToManyBehavior(),
				Assist.this.getRelationship(),
				data -> new Row(data),
				table$ != null ? table$.id$ : parent$.getSelectStatement().getRuntimeId());
		}
	}

	/**
	 * 自動生成された {@link TableFacadeAssist} の実装クラスです。<br>
	 * 条件として使用できるカラムと、参照しているテーブルを内包しており、それらを使用して検索 SQL を生成可能にします。
	 * @param <T> 使用されるカラムのタイプにあった型
	 * @param <M> Many 一対多の多側の型連鎖
	 */
	public static class ExtAssist<T, M> extends Assist<T, M> {

		private final TableFacadeContext<T> builder$;

		/**
		 * 直接使用しないでください。
		 * @param builder$ builder
		 * @param parent$ parent
		 * @param fkName$ fkName
		 */
		public ExtAssist(
			TableFacadeContext<T> builder$,
			TableFacadeAssist parent$,
			String fkName$) {
			super(builder$, parent$, fkName$);
			this.builder$ = builder$;
		}

		private ExtAssist(
			formula_details table$,
			TableFacadeContext<T> builder$,
			CriteriaContext context$) {
			super(table$, builder$, context$);
			this.builder$ = builder$;
		}

		/**
		 * この {@link TableFacadeAssist} が表すテーブルの Row を一とし、多をもつ検索結果を生成する {@link OneToManyQuery} を返します。
		 * @return {@link OneToManyQuery}
		 */
		public OneToManyQuery<Row, M> intercept() {
			//このインスタンスでは直接使用することはできません
			if (super.table$ != null) throw new IllegalStateException("It can not be used directly in this instance.");
			//集計モードでは実行できない処理です
			if (!getSelectStatement().rowMode()) throw new IllegalStateException("This operation can only in \"Row Mode\".");
			return new InstantOneToManyQuery<>(this, getSelectStatement().decorators());
		}

		/**
		 * 参照先テーブル名 formulas<br>
		 * 外部キー名 formula_details_formula_id_fkey<br>
		 * 項目名 formula_id
		 * @return formulas relationship
		 */
		public sqlassist.bb_stock.formulas.ExtAssist<T, Many<sqlassist.bb_stock.formula_details.Row, M>> $formulas() {
			return new sqlassist.bb_stock.formulas.ExtAssist<>(
				builder$,
				this,
				sqlassist.bb_stock.formula_details.bb_stock$formulas$formula_details_formula_id_fkey);
		}

		/**
		 * 参照先テーブル名 stocks<br>
		 * 外部キー名 formula_details_stock_id_fkey<br>
		 * 項目名 stock_id
		 * @return stocks relationship
		 */
		public sqlassist.bb_stock.stocks.ExtAssist<T, Many<sqlassist.bb_stock.formula_details.Row, M>> $stocks() {
			return new sqlassist.bb_stock.stocks.ExtAssist<>(
				builder$,
				this,
				sqlassist.bb_stock.formula_details.bb_stock$stocks$formula_details_stock_id_fkey);
		}

		/**
		 * 参照先テーブル名 users<br>
		 * 外部キー名 formula_details_created_by_fkey<br>
		 * 項目名 created_by
		 * @return users relationship
		 */
		public sqlassist.bb.users.ExtAssist<T, Many<sqlassist.bb_stock.formula_details.Row, M>> $users$formula_details_created_by_fkey() {
			return new sqlassist.bb.users.ExtAssist<>(
				builder$,
				this,
				sqlassist.bb_stock.formula_details.bb$users$formula_details_created_by_fkey);
		}

		/**
		 * 参照先テーブル名 users<br>
		 * 外部キー名 formula_details_updated_by_fkey<br>
		 * 項目名 updated_by
		 * @return users relationship
		 */
		public sqlassist.bb.users.ExtAssist<T, Many<sqlassist.bb_stock.formula_details.Row, M>> $users$formula_details_updated_by_fkey() {
			return new sqlassist.bb.users.ExtAssist<>(
				builder$,
				this,
				sqlassist.bb_stock.formula_details.bb$users$formula_details_updated_by_fkey);
		}
	}

	/**
	 * SELECT 句用
	 */
	public static class SelectAssist extends ExtAssist<SelectCol, Void> implements SelectClauseAssist {

		private SelectAssist(
			formula_details table$,
			TableFacadeContext<SelectCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * SELECT 句用
	 */
	public static class ListSelectAssist extends SelectAssist implements ListSelectClauseAssist {

		private ListSelectAssist(
			formula_details table$,
			TableFacadeContext<SelectCol> builder$) {
			super(table$, builder$);
		}

		@Override
		public SelectBehavior behavior() {
			return table$.selectBehavior();
		}
	}

	/**
	 * SELECT 文 WHERE 句用
	 */
	public static class WhereAssist extends ExtAssist<WhereColumn<WhereLogicalOperators>, Void> implements WhereClauseAssist<WhereAssist> {

		/**
		 * 条件接続 OR
		 */
		public final WhereAssist OR;

		private WhereAssist(
			formula_details table$,
			TableFacadeContext<WhereColumn<WhereLogicalOperators>> builder$,
			CriteriaContext context$,
			WhereAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
		}

		/**
		 * 任意のカラムを生成します。
		 * @param expression SQL 内のカラムを構成する文字列
		 * @param values プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<WhereLogicalOperators> expr(String expression, Object... values) {
			return new CriteriaAnyColumn<>(statement(), expression, values);
		}

		/**
		 * 任意のカラムを生成します。
		 * @param value プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<WhereLogicalOperators> expr(Object value) {
			return new CriteriaAnyColumn<>(statement(), value);
		}

		@Override
		public WhereLogicalOperators EXISTS(SelectStatement subquery) {
			SelectStatement statement = getSelectStatement();
			Helper.setExists(statement.getRuntimeId(), this, subquery);
			return (WhereLogicalOperators) statement.getWhereLogicalOperators();
		}

		@Override
		public WhereLogicalOperators NOT_EXISTS(SelectStatement subquery) {
			SelectStatement statement = getSelectStatement();
			Helper.setNotExists(statement.getRuntimeId(), this, subquery);
			return (WhereLogicalOperators) statement.getWhereLogicalOperators();
		}

		@Override
		public WhereLogicalOperators IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (WhereLogicalOperators) getSelectStatement().getWhereLogicalOperators();
		}

		@Override
		public WhereLogicalOperators NOT_IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, true, mainColumns, subquery);
			return (WhereLogicalOperators) getSelectStatement().getWhereLogicalOperators();
		}

		/**
		 * この句に任意のカラムを追加します。
		 * @param template カラムのテンプレート
		 * @return {@link LogicalOperators} AND か OR
		 */
		@Override
		public WhereColumn<WhereLogicalOperators> any(String template) {
			SelectStatement statement = getSelectStatement();
			return new WhereColumn<>(
				statement,
				getContext(),
				new MultiColumn(statement.getRootRealtionship(), template));
		}

		/**
		 * Consumer に渡された条件句を () で囲みます。
		 * @param consumer {@link Consumer}
		 * @return this
		 */
		@Override
		public WhereLogicalOperators paren(Consumer<WhereAssist> consumer) {
			SelectStatement statement = getSelectStatement();
			Helper.paren(statement.getRuntimeId(), getContext(), consumer, this);
			return (WhereLogicalOperators) statement.getWhereLogicalOperators();
		}

		@Override
		public Statement statement() {
			return getSelectStatement();
		}
	}

	/**
	 * GROUB BY 句用
	 */
	public static class GroupByAssist extends ExtAssist<GroupByCol, Void> implements GroupByClauseAssist {

		private GroupByAssist(
			formula_details table$,
			TableFacadeContext<GroupByCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}

		@Override
		public GroupByClause getGroupByClause() {
			return getSelectStatement().getGroupByClause();
		}
	}

	/**
	 * GROUB BY 句用
	 */
	public static class ListGroupByAssist extends GroupByAssist implements ListGroupByClauseAssist {

		private ListGroupByAssist(
			formula_details table$,
			TableFacadeContext<GroupByCol> builder$) {
			super(table$, builder$);
		}

		@Override
		public SelectBehavior behavior() {
			return table$.selectBehavior();
		}
	}

	/**
	 * HAVING 句用
	 */
	public static class HavingAssist extends ExtAssist<HavingColumn<HavingLogicalOperators>, Void> implements HavingClauseAssist<HavingAssist> {

		/**
		 * 条件接続 OR
		 */
		public final HavingAssist OR;

		private HavingAssist(
			formula_details table$,
			TableFacadeContext<HavingColumn<HavingLogicalOperators>> builder$,
			CriteriaContext context$,
			HavingAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
		}

		/**
		 * 任意のカラムを生成します。
		 * @param expression SQL 内のカラムを構成する文字列
		 * @param values プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<HavingLogicalOperators> expr(String expression, Object... values) {
			return new CriteriaAnyColumn<>(statement(), expression, values);
		}

		/**
		 * 任意のカラムを生成します。
		 * @param value プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<HavingLogicalOperators> expr(Object value) {
			return new CriteriaAnyColumn<>(statement(), value);
		}

		@Override
		public HavingLogicalOperators EXISTS(SelectStatement subquery) {
			SelectStatement statement = getSelectStatement();
			Helper.setExists(statement.getRuntimeId(), this, subquery);
			return (HavingLogicalOperators) statement.getHavingLogicalOperators();
		}

		@Override
		public HavingLogicalOperators NOT_EXISTS(SelectStatement subquery) {
			SelectStatement statement = getSelectStatement();
			Helper.setNotExists(statement.getRuntimeId(), this, subquery);
			return (HavingLogicalOperators) statement.getHavingLogicalOperators();
		}

		@Override
		public HavingLogicalOperators IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (HavingLogicalOperators) getSelectStatement().getHavingLogicalOperators();
		}

		@Override
		public HavingLogicalOperators NOT_IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, true, mainColumns, subquery);
			return (HavingLogicalOperators) getSelectStatement().getHavingLogicalOperators();
		}

		/**
		 * この句に任意のカラムを追加します。
		 * @param template カラムのテンプレート
		 * @return {@link LogicalOperators} AND か OR
		 */
		@Override
		public HavingColumn<HavingLogicalOperators> any(String template) {
			SelectStatement statement = getSelectStatement();
			return new HavingColumn<>(
				statement,
				getContext(),
				new MultiColumn(statement.getRootRealtionship(), template));
		}

		/**
		 * Consumer に渡された条件句を () で囲みます。
		 * @param consumer {@link Consumer}
		 * @return this
		 */
		@Override
		public HavingLogicalOperators paren(Consumer<HavingAssist> consumer) {
			SelectStatement statement = getSelectStatement();
			Helper.paren(statement.getRuntimeId(), getContext(), consumer, this);
			return (HavingLogicalOperators) statement.getHavingLogicalOperators();
		}
	}

	/**
	 * ORDER BY 句用
	 */
	public static class OrderByAssist extends ExtAssist<OrderByCol, Void> implements OrderByClauseAssist {

		private OrderByAssist(
			formula_details table$,
			TableFacadeContext<OrderByCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}

		@Override
		public OrderByClause getOrderByClause() {
			return getSelectStatement().getOrderByClause();
		}
	}

	/**
	 * GROUB BY 句用
	 */
	public static class ListOrderByAssist extends OrderByAssist implements ListOrderByClauseAssist {

		private ListOrderByAssist(
			formula_details table$,
			TableFacadeContext<OrderByCol> builder$) {
			super(table$, builder$);
		}

		@Override
		public SelectBehavior behavior() {
			return table$.selectBehavior();
		}
	}

	/**
	 * ON 句 (LEFT) 用
	 */
	public static class OnLeftAssist extends ExtAssist<OnLeftColumn<OnLeftLogicalOperators>, Void> implements OnLeftClauseAssist<OnLeftAssist> {

		/**
		 * 条件接続 OR
		 */
		public final OnLeftAssist OR;

		private OnLeftAssist(
			formula_details table$,
			TableFacadeContext<OnLeftColumn<OnLeftLogicalOperators>> builder$,
			CriteriaContext context$,
			OnLeftAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
		}

		/**
		 * 任意のカラムを生成します。
		 * @param expression SQL 内のカラムを構成する文字列
		 * @param values プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<OnLeftLogicalOperators> expr(String expression, Object... values) {
			return new CriteriaAnyColumn<>(statement(), expression, values);
		}

		/**
		 * 任意のカラムを生成します。
		 * @param value プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<OnLeftLogicalOperators> expr(Object value) {
			return new CriteriaAnyColumn<>(statement(), value);
		}

		@Override
		public OnLeftLogicalOperators EXISTS(SelectStatement subquery) {
			SelectStatement statement = getSelectStatement();
			Helper.setExists(statement.getRuntimeId(), this, subquery);
			return (OnLeftLogicalOperators) statement.getOnLeftLogicalOperators();
		}

		@Override
		public OnLeftLogicalOperators NOT_EXISTS(SelectStatement subquery) {
			SelectStatement statement = getSelectStatement();
			Helper.setNotExists(statement.getRuntimeId(), this, subquery);
			return (OnLeftLogicalOperators) statement.getOnLeftLogicalOperators();
		}

		@Override
		public OnLeftLogicalOperators IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (OnLeftLogicalOperators) getSelectStatement().getOnLeftLogicalOperators();
		}

		@Override
		public OnLeftLogicalOperators NOT_IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, true, mainColumns, subquery);
			return (OnLeftLogicalOperators) getSelectStatement().getOnLeftLogicalOperators();
		}

		/**
		 * この句に任意のカラムを追加します。
		 * @param template カラムのテンプレート
		 * @return {@link LogicalOperators} AND か OR
		 */
		@Override
		public OnLeftColumn<OnLeftLogicalOperators> any(String template) {
			SelectStatement statement = getSelectStatement();
			return new OnLeftColumn<>(
				statement,
				getContext(),
				new MultiColumn(statement.getRootRealtionship(), template));
		}

		/**
		 * Consumer に渡された条件句を () で囲みます。
		 * @param consumer {@link Consumer}
		 * @return this
		 */
		@Override
		public OnLeftLogicalOperators paren(Consumer<OnLeftAssist> consumer) {
			SelectStatement statement = getSelectStatement();
			Helper.paren(statement.getRuntimeId(), getContext(), consumer, this);
			return (OnLeftLogicalOperators) statement.getOnLeftLogicalOperators();
		}
	}

	/**
	 * ON 句 (RIGHT) 用
	 */
	public static class OnRightAssist extends Assist<OnRightColumn<OnRightLogicalOperators>, Void> implements OnRightClauseAssist<OnRightAssist> {

		/**
		 * 条件接続 OR
		 */
		public final OnRightAssist OR;

		private OnRightAssist(
			formula_details table$,
			TableFacadeContext<OnRightColumn<OnRightLogicalOperators>> builder$,
			CriteriaContext context$,
			OnRightAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
		}

		/**
		 * 任意のカラムを生成します。
		 * @param expression SQL 内のカラムを構成する文字列
		 * @param values プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<OnRightLogicalOperators> expr(String expression, Object... values) {
			return new CriteriaAnyColumn<>(statement(), expression, values);
		}

		/**
		 * 任意のカラムを生成します。
		 * @param value プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<OnRightLogicalOperators> expr(Object value) {
			return new CriteriaAnyColumn<>(statement(), value);
		}

		@Override
		public OnRightLogicalOperators EXISTS(SelectStatement subquery) {
			SelectStatement statement = getSelectStatement();
			Helper.setExists(statement.getRuntimeId(), this, subquery);
			return (OnRightLogicalOperators) statement.getOnRightLogicalOperators();
		}

		@Override
		public OnRightLogicalOperators NOT_EXISTS(SelectStatement subquery) {
			SelectStatement statement = getSelectStatement();
			Helper.setNotExists(statement.getRuntimeId(), this, subquery);
			return (OnRightLogicalOperators) statement.getOnRightLogicalOperators();
		}

		@Override
		public OnRightLogicalOperators IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (OnRightLogicalOperators) getSelectStatement().getOnRightLogicalOperators();
		}

		@Override
		public OnRightLogicalOperators NOT_IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, true, mainColumns, subquery);
			return (OnRightLogicalOperators) getSelectStatement().getOnRightLogicalOperators();
		}

		/**
		 * この句に任意のカラムを追加します。
		 * @param template カラムのテンプレート
		 * @return {@link LogicalOperators} AND か OR
		 */
		@Override
		public OnRightColumn<OnRightLogicalOperators> any(String template) {
			SelectStatement statement = getSelectStatement();
			return new OnRightColumn<>(
				statement,
				getContext(),
				new MultiColumn(statement.getRootRealtionship(), template));
		}

		/**
		 * Consumer に渡された条件句を () で囲みます。
		 * @param consumer {@link Consumer}
		 * @return this
		 */
		@Override
		public OnRightLogicalOperators paren(Consumer<OnRightAssist> consumer) {
			SelectStatement statement = getSelectStatement();
			Helper.paren(statement.getRuntimeId(), getContext(), consumer, this);
			return (OnRightLogicalOperators) statement.getOnRightLogicalOperators();
		}
	}

	/**
	 * INSERT 用
	 */
	public static class InsertAssist extends Assist<InsertCol, Void> implements InsertClauseAssist {

		private InsertAssist(
			formula_details table$,
			TableFacadeContext<InsertCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * INSERT 用
	 */
	public static class ListInsertAssist extends InsertAssist implements ListInsertClauseAssist {

		private ListInsertAssist(
			formula_details table$,
			TableFacadeContext<InsertCol> builder$) {
			super(table$, builder$);
		}

		@Override
		public DataManipulationStatementBehavior<?, ?, ?, ?, ?> behavior() {
			return table$.dmsBehavior();
		}
	}

	/**
	 * UPDATE 用
	 */
	public static class UpdateAssist extends Assist<UpdateCol, Void> implements UpdateClauseAssist {

		private UpdateAssist(
			formula_details table$,
			TableFacadeContext<UpdateCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * INSERT 用
	 */
	public static class ListUpdateAssist extends UpdateAssist implements ListUpdateClauseAssist<DMSWhereAssist> {

		private ListUpdateAssist(
			formula_details table$,
			TableFacadeContext<UpdateCol> builder$) {
			super(table$, builder$);
		}

		@Override
		public DataManipulationStatementBehavior<?, ?, ?, ?, DMSWhereAssist> behavior() {
			return table$.dmsBehavior();
		}
	}

	/**
	 * UPDATE, DELETE 文 WHERE 句用
	 */
	public static class DMSWhereAssist extends Assist<WhereColumn<DMSWhereLogicalOperators>, Void> implements WhereClauseAssist<DMSWhereAssist> {

		/**
		 * 条件接続 OR
		 */
		public final DMSWhereAssist OR;

		private DMSWhereAssist(
			formula_details table$,
			TableFacadeContext<WhereColumn<DMSWhereLogicalOperators>> builder$,
			CriteriaContext context$,
			DMSWhereAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
		}

		/**
		 * 任意のカラムを生成します。
		 * @param expression SQL 内のカラムを構成する文字列
		 * @param values プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<DMSWhereLogicalOperators> expr(String expression, Object... values) {
			return new CriteriaAnyColumn<>(statement(), expression, values);
		}

		/**
		 * 任意のカラムを生成します。
		 * @param value プレースホルダの値
		 * @return {@link CriteriaAssistColumn}
		 */
		public CriteriaAssistColumn<DMSWhereLogicalOperators> expr(Object value) {
			return new CriteriaAnyColumn<>(statement(), value);
		}

		@Override
		public DMSWhereLogicalOperators EXISTS(SelectStatement subquery) {
			DataManipulationStatement statement = getDataManipulationStatement();
			Helper.setExists(statement.getRuntimeId(), this, subquery);
			return (DMSWhereLogicalOperators) statement.getWhereLogicalOperators();
		}

		@Override
		public DMSWhereLogicalOperators NOT_EXISTS(SelectStatement subquery) {
			DataManipulationStatement statement = getDataManipulationStatement();
			Helper.setNotExists(statement.getRuntimeId(), this, subquery);
			return (DMSWhereLogicalOperators) statement.getWhereLogicalOperators();
		}

		@Override
		public DMSWhereLogicalOperators IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (DMSWhereLogicalOperators) getDataManipulationStatement().getWhereLogicalOperators();
		}

		@Override
		public DMSWhereLogicalOperators NOT_IN(Vargs<AssistColumn> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, true, mainColumns, subquery);
			return (DMSWhereLogicalOperators) getDataManipulationStatement().getWhereLogicalOperators();
		}

		/**
		 * この句に任意のカラムを追加します。
		 * @param template カラムのテンプレート
		 * @return {@link WhereColumn}
		 */
		@Override
		public WhereColumn<DMSWhereLogicalOperators> any(String template) {
			DataManipulationStatement statement = getDataManipulationStatement();
			return new WhereColumn<>(
				statement,
				getContext(),
				new MultiColumn(statement.getRootRealtionship(), template));
		}

		/**
		 * Consumer に渡された条件句を () で囲みます。
		 * @param consumer {@link Consumer}
		 * @return {@link DMSWhereLogicalOperators}
		 */
		@Override
		public DMSWhereLogicalOperators paren(Consumer<DMSWhereAssist> consumer) {
			DataManipulationStatement statement = getDataManipulationStatement();
			Helper.paren(statement.getRuntimeId(), getContext(), consumer, this);
			return (DMSWhereLogicalOperators) statement.getWhereLogicalOperators();
		}

		@Override
		public Statement statement() {
			return getDataManipulationStatement();
		}
	}

	/**
	 * SELECT 句用
	 */
	public static class SelectCol extends SelectColumn {

		private SelectCol(TableFacadeAssist assist, String name) {
			super(assist, name);
		}
	}

	/**
	 * GROUP BY 句用
	 */
	public static class GroupByCol extends GroupByColumn {

		private GroupByCol(TableFacadeAssist assist, String name) {
			super(assist, name);
		}
	}

	/**
	 * ORDER BY 句用
	 */
	public static class OrderByCol extends OrderByColumn {

		private OrderByCol(TableFacadeAssist assist, String name) {
			super(assist, name);
		}
	}

	/**
	 * INSERT 文用
	 */
	public static class InsertCol extends InsertColumn {

		private InsertCol(TableFacadeAssist assist, String name) {
			super(assist, name);
		}
	}

	/**
	 * UPDATE 文用
	 */
	public static class UpdateCol extends UpdateColumn {

		private UpdateCol(TableFacadeAssist assist, String name) {
			super(assist, name);
		}
	}

	/**
	 * Query
	 */
	public class Query implements org.blendee.assist.Query<Iterator, Row> {

		private final PlaybackQuery inner;

		private Query(PlaybackQuery inner) {
			this.inner = inner;
		}

		@Override
		public Iterator retrieve() {
			return wrap(inner.retrieve());
		}

		@Override
		public Optional<Row> fetch(Bindable... primaryKeyMembers) {
			return inner.fetch(primaryKeyMembers).map(object -> createRow(object));
		}

		@Override
		public int count() {
			return inner.count();
		}

		@Override
		public ComposedSQL countSQL() {
			return inner.countSQL();
		}

		@Override
		public ComposedSQL aggregateSQL() {
			return inner.aggregateSQL();
		}

		@Override
		public boolean rowMode() {
			return inner.rowMode();
		}

		@Override
		public String sql() {
			return inner.sql();
		}

		@Override
		public int complement(int done, BPreparedStatement statement) {
			return inner.complement(done, statement);
		}

		@Override
		public Query reproduce(Object... placeHolderValues) {
			return new Query(inner.reproduce(placeHolderValues));
		}

		@Override
		public Query reproduce() {
			return new Query(inner.reproduce());
		}

		@Override
		public Binder[] currentBinders() {
			return inner.currentBinders();
		}
	}
}
