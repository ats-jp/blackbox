package jp.ats.blackbox.blendee.bb;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import java.util.LinkedList;

import org.blendee.jdbc.BPreparedStatement;
import org.blendee.jdbc.BResultSet;
import org.blendee.jdbc.ComposedSQL;
import org.blendee.jdbc.ContextManager;
import org.blendee.jdbc.Result;
import org.blendee.jdbc.ResultSetIterator;
import org.blendee.jdbc.TablePath;
import org.blendee.orm.ColumnNameDataObjectBuilder;
import org.blendee.orm.DataObject;
import org.blendee.orm.DataObjectIterator;
import org.blendee.selector.AnchorOptimizerFactory;
import org.blendee.selector.Optimizer;
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
import org.blendee.assist.CriteriaColumn;
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
 * schema: bb<br>
 * name: transient_transfers<br>
 * type: TABLE<br>
 * remarks: 一時作業移動伝票<br>
 */
@Table(name = "transient_transfers", schema = "bb", type = "TABLE", remarks = "一時作業移動伝票")
@PrimaryKey(name = "transient_transfers_pkey", columns = { "id" })
public class transient_transfers
	extends java.lang.Object
	implements
	TableFacade<Row>,
	SelectStatement,
	SQLDecorators,
	Query<transient_transfers.Iterator, transient_transfers.Row>,
	RightTable<transient_transfers.OnRightAssist> {

	/**
	 * この定数クラスのスキーマ名
	 */
	public static final String SCHEMA = "bb";

	/**
	 * この定数クラスのテーブル名
	 */
	public static final String TABLE = "transient_transfers";

	/**
	 * この定数クラスのテーブルを指す {@link TablePath}
	 */
	public static final TablePath $TABLE = new TablePath(SCHEMA, TABLE);

	private final Relationship relationship$ = RelationshipFactory.getInstance().getInstance($TABLE);

	private final List<SQLDecorator> decorators$ = new LinkedList<SQLDecorator>();

	/**
	 * name: id<br>
	 * remarks: ID<br>
	 * type: bigserial(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "id", type = -5, typeName = "bigserial", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "ID", defaultValue = "nextval('bb.transient_transfers_id_seq'::regclass)", ordinalPosition = 1, notNull = true)
	public static final String id = "id";

	/**
	 * name: transient_id<br>
	 * remarks: 一時作業ID<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "transient_id", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "一時作業ID", defaultValue = "null", ordinalPosition = 2, notNull = true)
	public static final String transient_id = "transient_id";

	/**
	 * name: group_id<br>
	 * remarks: グループID<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "group_id", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "グループID", defaultValue = "null", ordinalPosition = 3, notNull = true)
	public static final String group_id = "group_id";

	/**
	 * name: transferred_at<br>
	 * remarks: 移動時刻<br>
	 * type: timestamptz(35, 6)<br>
	 * not null: true<br>
	 */
	@Column(name = "transferred_at", type = 93, typeName = "timestamptz", size = 35, hasDecimalDigits = true, decimalDigits = 6, remarks = "移動時刻", defaultValue = "null", ordinalPosition = 4, notNull = true)
	public static final String transferred_at = "transferred_at";

	/**
	 * name: extension<br>
	 * remarks: 外部アプリケーション情報JSON<br>
	 * type: jsonb(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "extension", type = 1111, typeName = "jsonb", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "外部アプリケーション情報JSON", defaultValue = "'{}'::jsonb", ordinalPosition = 5, notNull = true)
	public static final String extension = "extension";

	/**
	 * name: tags<br>
	 * remarks: 保存用タグ<br>
	 * type: _text(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "tags", type = 2003, typeName = "_text", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "保存用タグ", defaultValue = "'{}'::text[]", ordinalPosition = 6, notNull = true)
	public static final String tags = "tags";

	/**
	 * name: completed<br>
	 * remarks: 実施済フラグ<br>
	 * type: bool(1)<br>
	 * not null: true<br>
	 */
	@Column(name = "completed", type = -7, typeName = "bool", size = 1, hasDecimalDigits = true, decimalDigits = 0, remarks = "実施済フラグ", defaultValue = "false", ordinalPosition = 7, notNull = true)
	public static final String completed = "completed";

	/**
	 * name: trigger_id<br>
	 * remarks: 追加処理ID<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "trigger_id", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "追加処理ID", defaultValue = "0", ordinalPosition = 8, notNull = true)
	public static final String trigger_id = "trigger_id";

	/**
	 * name: parameter<br>
	 * remarks: triggerパラメータ<br>
	 * type: jsonb(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "parameter", type = 1111, typeName = "jsonb", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "triggerパラメータ", defaultValue = "'{}'::jsonb", ordinalPosition = 9, notNull = true)
	public static final String parameter = "parameter";

	/**
	 * name: revision<br>
	 * remarks: リビジョン番号<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "revision", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "リビジョン番号", defaultValue = "0", ordinalPosition = 10, notNull = true)
	public static final String revision = "revision";

	/**
	 * name: created_at<br>
	 * remarks: 作成時刻<br>
	 * type: timestamptz(35, 6)<br>
	 * not null: true<br>
	 */
	@Column(name = "created_at", type = 93, typeName = "timestamptz", size = 35, hasDecimalDigits = true, decimalDigits = 6, remarks = "作成時刻", defaultValue = "now()", ordinalPosition = 11, notNull = true)
	public static final String created_at = "created_at";

	/**
	 * name: created_by<br>
	 * remarks: 作成ユーザー<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "created_by", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "作成ユーザー", defaultValue = "null", ordinalPosition = 12, notNull = true)
	public static final String created_by = "created_by";

	/**
	 * name: updated_at<br>
	 * remarks: 更新時刻<br>
	 * type: timestamptz(35, 6)<br>
	 * not null: true<br>
	 */
	@Column(name = "updated_at", type = 93, typeName = "timestamptz", size = 35, hasDecimalDigits = true, decimalDigits = 6, remarks = "更新時刻", defaultValue = "now()", ordinalPosition = 13, notNull = true)
	public static final String updated_at = "updated_at";

	/**
	 * name: updated_by<br>
	 * remarks: 更新ユーザー<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "updated_by", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "更新ユーザー", defaultValue = "null", ordinalPosition = 14, notNull = true)
	public static final String updated_by = "updated_by";

	/**
	 * name: transient_transfers_group_id_fkey<br>
	 * reference: groups<br>
	 * columns: group_id
	 */
	@ForeignKey(name = "transient_transfers_group_id_fkey", references = "groups", columns = { "group_id" }, refColumns = { "id" })
	public static final String groups$transient_transfers_group_id_fkey = "transient_transfers_group_id_fkey";

	/**
	 * name: transient_transfers_transient_id_fkey<br>
	 * reference: transients<br>
	 * columns: transient_id
	 */
	@ForeignKey(name = "transient_transfers_transient_id_fkey", references = "transients", columns = { "transient_id" }, refColumns = { "id" })
	public static final String transients$transient_transfers_transient_id_fkey = "transient_transfers_transient_id_fkey";

	/**
	 * name: transient_transfers_trigger_id_fkey<br>
	 * reference: triggers<br>
	 * columns: trigger_id
	 */
	@ForeignKey(name = "transient_transfers_trigger_id_fkey", references = "triggers", columns = { "trigger_id" }, refColumns = { "id" })
	public static final String triggers$transient_transfers_trigger_id_fkey = "transient_transfers_trigger_id_fkey";

	/**
	 * name: transient_transfers_created_by_fkey<br>
	 * reference: users<br>
	 * columns: created_by
	 */
	@ForeignKey(name = "transient_transfers_created_by_fkey", references = "users", columns = { "created_by" }, refColumns = { "id" })
	public static final String users$transient_transfers_created_by_fkey = "transient_transfers_created_by_fkey";

	/**
	 * name: transient_transfers_updated_by_fkey<br>
	 * reference: users<br>
	 * columns: updated_by
	 */
	@ForeignKey(name = "transient_transfers_updated_by_fkey", references = "users", columns = { "updated_by" }, refColumns = { "id" })
	public static final String users$transient_transfers_updated_by_fkey = "transient_transfers_updated_by_fkey";

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

		private Row() {
			data$ = new DataObject(rowRel$);
		}

		private Row(DataObject data) {
			this.data$ = data;
		}

		private Row(Result result) {
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
		* type: bigserial(19)<br>
		* not null: true<br>
		 * @param value java.lang.Long
		 */
		public void setId(java.lang.Long value) {
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
		* type: bigserial(19)<br>
		* not null: true<br>
		 * @return java.lang.Long
		 */
		public java.lang.Long getId() {
			Binder binder = data$.getValue("id");
			return (java.lang.Long) binder.getValue();
		}

		/**
		 * setter
		 * name: transient_id<br>
		* remarks: 一時作業ID<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @param value java.lang.Long
		 */
		public void setTransient_id(java.lang.Long value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("transient_id").getType());
			data$.setValue("transient_id", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: transient_id<br>
		* remarks: 一時作業ID<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @return java.lang.Long
		 */
		public java.lang.Long getTransient_id() {
			Binder binder = data$.getValue("transient_id");
			return (java.lang.Long) binder.getValue();
		}

		/**
		 * setter
		 * name: group_id<br>
		* remarks: グループID<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @param value java.lang.Long
		 */
		public void setGroup_id(java.lang.Long value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("group_id").getType());
			data$.setValue("group_id", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: group_id<br>
		* remarks: グループID<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @return java.lang.Long
		 */
		public java.lang.Long getGroup_id() {
			Binder binder = data$.getValue("group_id");
			return (java.lang.Long) binder.getValue();
		}

		/**
		 * setter
		 * name: transferred_at<br>
		* remarks: 移動時刻<br>
		* type: timestamptz(35, 6)<br>
		* not null: true<br>
		 * @param value java.sql.Timestamp
		 */
		public void setTransferred_at(java.sql.Timestamp value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("transferred_at").getType());
			data$.setValue("transferred_at", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: transferred_at<br>
		* remarks: 移動時刻<br>
		* type: timestamptz(35, 6)<br>
		* not null: true<br>
		 * @return java.sql.Timestamp
		 */
		public java.sql.Timestamp getTransferred_at() {
			Binder binder = data$.getValue("transferred_at");
			return (java.sql.Timestamp) binder.getValue();
		}

		/**
		 * setter
		 * name: extension<br>
		* remarks: 外部アプリケーション情報JSON<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @param value java.lang.Object
		 */
		public void setExtension(java.lang.Object value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("extension").getType());
			data$.setValue("extension", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: extension<br>
		* remarks: 外部アプリケーション情報JSON<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @return java.lang.Object
		 */
		public java.lang.Object getExtension() {
			Binder binder = data$.getValue("extension");
			return binder.getValue();
		}

		/**
		 * setter
		 * name: tags<br>
		* remarks: 保存用タグ<br>
		* type: _text(2147483647)<br>
		* not null: true<br>
		 * @param value java.lang.Object
		 */
		public void setTags(java.lang.Object value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("tags").getType());
			data$.setValue("tags", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: tags<br>
		* remarks: 保存用タグ<br>
		* type: _text(2147483647)<br>
		* not null: true<br>
		 * @return java.lang.Object
		 */
		public java.lang.Object getTags() {
			Binder binder = data$.getValue("tags");
			return binder.getValue();
		}

		/**
		 * setter
		 * name: completed<br>
		* remarks: 実施済フラグ<br>
		* type: bool(1)<br>
		* not null: true<br>
		 * @param value java.lang.Boolean
		 */
		public void setCompleted(java.lang.Boolean value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("completed").getType());
			data$.setValue("completed", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: completed<br>
		* remarks: 実施済フラグ<br>
		* type: bool(1)<br>
		* not null: true<br>
		 * @return java.lang.Boolean
		 */
		public java.lang.Boolean getCompleted() {
			Binder binder = data$.getValue("completed");
			return (java.lang.Boolean) binder.getValue();
		}

		/**
		 * setter
		 * name: trigger_id<br>
		* remarks: 追加処理ID<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @param value java.lang.Long
		 */
		public void setTrigger_id(java.lang.Long value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("trigger_id").getType());
			data$.setValue("trigger_id", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: trigger_id<br>
		* remarks: 追加処理ID<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @return java.lang.Long
		 */
		public java.lang.Long getTrigger_id() {
			Binder binder = data$.getValue("trigger_id");
			return (java.lang.Long) binder.getValue();
		}

		/**
		 * setter
		 * name: parameter<br>
		* remarks: triggerパラメータ<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @param value java.lang.Object
		 */
		public void setParameter(java.lang.Object value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("parameter").getType());
			data$.setValue("parameter", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: parameter<br>
		* remarks: triggerパラメータ<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @return java.lang.Object
		 */
		public java.lang.Object getParameter() {
			Binder binder = data$.getValue("parameter");
			return binder.getValue();
		}

		/**
		 * setter
		 * name: revision<br>
		* remarks: リビジョン番号<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @param value java.lang.Long
		 */
		public void setRevision(java.lang.Long value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("revision").getType());
			data$.setValue("revision", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: revision<br>
		* remarks: リビジョン番号<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @return java.lang.Long
		 */
		public java.lang.Long getRevision() {
			Binder binder = data$.getValue("revision");
			return (java.lang.Long) binder.getValue();
		}

		/**
		 * setter
		 * name: created_at<br>
		* remarks: 作成時刻<br>
		* type: timestamptz(35, 6)<br>
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
		* remarks: 作成時刻<br>
		* type: timestamptz(35, 6)<br>
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
		* remarks: 作成ユーザー<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @param value java.lang.Long
		 */
		public void setCreated_by(java.lang.Long value) {
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
		* remarks: 作成ユーザー<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @return java.lang.Long
		 */
		public java.lang.Long getCreated_by() {
			Binder binder = data$.getValue("created_by");
			return (java.lang.Long) binder.getValue();
		}

		/**
		 * setter
		 * name: updated_at<br>
		* remarks: 更新時刻<br>
		* type: timestamptz(35, 6)<br>
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
		* remarks: 更新時刻<br>
		* type: timestamptz(35, 6)<br>
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
		* remarks: 更新ユーザー<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @param value java.lang.Long
		 */
		public void setUpdated_by(java.lang.Long value) {
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
		* remarks: 更新ユーザー<br>
		* type: int8(19)<br>
		* not null: true<br>
		 * @return java.lang.Long
		 */
		public java.lang.Long getUpdated_by() {
			Binder binder = data$.getValue("updated_by");
			return (java.lang.Long) binder.getValue();
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 groups<br>
		 * 外部キー名 transient_transfers_group_id_fkey<br>
		 * 項目名 group_id
		 * @return 参照しているレコードの Row
		 */
		public jp.ats.blackbox.blendee.bb.groups.Row $groups() {
			return jp.ats.blackbox.blendee.bb.groups.row(
				data$.getDataObject(groups$transient_transfers_group_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 transients<br>
		 * 外部キー名 transient_transfers_transient_id_fkey<br>
		 * 項目名 transient_id
		 * @return 参照しているレコードの Row
		 */
		public jp.ats.blackbox.blendee.bb.transients.Row $transients() {
			return jp.ats.blackbox.blendee.bb.transients.row(
				data$.getDataObject(transients$transient_transfers_transient_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 triggers<br>
		 * 外部キー名 transient_transfers_trigger_id_fkey<br>
		 * 項目名 trigger_id
		 * @return 参照しているレコードの Row
		 */
		public jp.ats.blackbox.blendee.bb.triggers.Row $triggers() {
			return jp.ats.blackbox.blendee.bb.triggers.row(
				data$.getDataObject(triggers$transient_transfers_trigger_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 users<br>
		 * 外部キー名 transient_transfers_created_by_fkey<br>
		 * 項目名 created_by
		 * @return 参照しているレコードの Row
		 */
		public jp.ats.blackbox.blendee.bb.users.Row $users$transient_transfers_created_by_fkey() {
			return jp.ats.blackbox.blendee.bb.users.row(
				data$.getDataObject(users$transient_transfers_created_by_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 users<br>
		 * 外部キー名 transient_transfers_updated_by_fkey<br>
		 * 項目名 updated_by
		 * @return 参照しているレコードの Row
		 */
		public jp.ats.blackbox.blendee.bb.users.Row $users$transient_transfers_updated_by_fkey() {
			return jp.ats.blackbox.blendee.bb.users.row(
				data$.getDataObject(users$transient_transfers_updated_by_fkey));
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

		private WhereLogicalOperators() {}

		/**
		 * WHERE 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final WhereAssist OR = new WhereAssist(
			transient_transfers.this,
			whereContext$,
			CriteriaContext.OR,
			null);

		/**
		 * WHERE 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final WhereAssist AND = new WhereAssist(
			transient_transfers.this,
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

		private HavingLogicalOperators() {}

		/**
		 * HAVING 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final HavingAssist OR = new HavingAssist(
			transient_transfers.this,
			havingContext$,
			CriteriaContext.OR,
			null);

		/**
		 * HAVING 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final HavingAssist AND = new HavingAssist(
			transient_transfers.this,
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

		private OnLeftLogicalOperators() {}

		/**
		 * ON 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnLeftAssist OR = new OnLeftAssist(
			transient_transfers.this,
			onLeftContext$,
			CriteriaContext.OR,
			null);

		/**
		 * ON 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnLeftAssist AND = new OnLeftAssist(
			transient_transfers.this,
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

		private OnRightLogicalOperators() {}

		/**
		 * ON 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnRightAssist OR = new OnRightAssist(
			transient_transfers.this,
			onRightContext$,
			CriteriaContext.OR,
			null);

		/**
		 * ON 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnRightAssist AND = new OnRightAssist(
			transient_transfers.this,
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

		private DMSWhereLogicalOperators() {}

		/**
		 * WHERE 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final DMSWhereAssist OR = new DMSWhereAssist(
			transient_transfers.this,
			dmsWhereContext$,
			CriteriaContext.OR,
			null);

		/**
		 * WHERE 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final DMSWhereAssist AND = new DMSWhereAssist(
			transient_transfers.this,
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
		return id$ == null ? (id$ = RuntimeIdFactory.getRuntimeInstance()) : id$;
	}

	private class SelectBehavior extends SelectStatementBehavior<SelectAssist, ListSelectAssist, GroupByAssist, ListGroupByAssist, WhereAssist, HavingAssist, OrderByAssist, ListOrderByAssist, OnLeftAssist> {

		private SelectBehavior() {
			super($TABLE, getRuntimeId(), transient_transfers.this);
		}

		@Override
		protected SelectAssist newSelect() {
			return new SelectAssist(
				transient_transfers.this,
				selectContext$);
		}

		@Override
		protected ListSelectAssist newListSelect() {
			return new ListSelectAssist(
				transient_transfers.this,
				selectContext$);
		}

		@Override
		protected GroupByAssist newGroupBy() {
			return new GroupByAssist(
				transient_transfers.this,
				groupByContext$);
		}

		@Override
		protected ListGroupByAssist newListGroupBy() {
			return new ListGroupByAssist(
				transient_transfers.this,
				groupByContext$);
		}

		@Override
		protected OrderByAssist newOrderBy() {
			return new OrderByAssist(
				transient_transfers.this,
				orderByContext$);
		}

		@Override
		protected ListOrderByAssist newListOrderBy() {
			return new ListOrderByAssist(
				transient_transfers.this,
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
			super($TABLE, transient_transfers.this.getRuntimeId(), transient_transfers.this);
		}

		@Override
		protected InsertAssist newInsert() {
			return new InsertAssist(
				transient_transfers.this,
				insertContext$);
		}

		@Override
		protected ListInsertAssist newListInsert() {
			return new ListInsertAssist(
				transient_transfers.this,
				insertContext$);
		}

		@Override
		protected UpdateAssist newUpdate() {
			return new UpdateAssist(
				transient_transfers.this,
				updateContext$);
		}

		@Override
		protected ListUpdateAssist newListUpdate() {
			return new ListUpdateAssist(
				transient_transfers.this,
				updateContext$);
		}

		@Override
		protected LogicalOperators<DMSWhereAssist> newWhereOperators() {
			return new DMSWhereLogicalOperators();
		}
	}

	/**
	 * このクラスのインスタンスを生成します。<br>
	 * インスタンスは ID として、引数で渡された id を使用します。<br>
	 * フィールド定義の必要がなく、簡易に使用できますが、 ID は呼び出し側クラス内で一意である必要があります。
	 * @param id {@link SelectStatement} を使用するクラス内で一意の ID
	 * @return このクラスのインスタンス
	 */
	public static transient_transfers of(String id) {
		if (id == null || id.equals(""))
			throw new IllegalArgumentException("id が空です");

		return new transient_transfers(getUsing(new Throwable().getStackTrace()[1]), id);
	}

	/**
	 * 空のインスタンスを生成します。
	 */
	public transient_transfers() {}

	/**
	 * このクラスのインスタンスを生成します。<br>
	 * このコンストラクタで生成されたインスタンス の SELECT 句で使用されるカラムは、 パラメータの {@link Optimizer} に依存します。
	 * @param optimizer SELECT 句を決定する
	 */
	public transient_transfers(Optimizer optimizer) {
		selectBehavior().setOptimizer(Objects.requireNonNull(optimizer));
	}

	private transient_transfers(Class<?> using, String id) {
		selectBehavior().setOptimizer(
			ContextManager.get(AnchorOptimizerFactory.class).getInstance(id, getRuntimeId(), $TABLE, using));
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
	public transient_transfers selectClause(Consumer<ListSelectAssist> consumer) {
		selectBehavior().selectClause(consumer);
		return this;
	}

	/**
	 * GROUP BY 句を作成する {@link Consumer}
	 * @param consumer {@link Consumer}
	 * @return this
	 */
	public transient_transfers groupByClause(Consumer<ListGroupByAssist> consumer) {
		selectBehavior().groupByClause(consumer);
		return this;
	}

	/**
	 * GROUP BY 句を作成する {@link Consumer}
	 * @param consumer {@link Consumer}
	 * @return this
	 */
	public transient_transfers orderByClause(Consumer<ListOrderByAssist> consumer) {
		selectBehavior().orderByClause(consumer);
		return this;
	}

	/**
	 * SELECT 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public transient_transfers SELECT(
		SelectOfferFunction<SelectAssist> function) {
		selectBehavior().SELECT(function);
		return this;
	}

	/**
	 * DISTINCT を使用した SELECT 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public transient_transfers SELECT_DISTINCT(
		SelectOfferFunction<SelectAssist> function) {
		selectBehavior().SELECT_DISTINCT(function);
		return this;
	}

	/**
	 * COUNT(*) を使用した SELECT 句を記述します。
	 * @return この {@link SelectStatement}
	 */
	public transient_transfers SELECT_COUNT() {
		selectBehavior().SELECT_COUNT();
		return this;
	}

	/**
	 * GROUP BY 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public transient_transfers GROUP_BY(
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
	public final transient_transfers WHERE(
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
	public final transient_transfers HAVING(
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
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, transient_transfers> INNER_JOIN(RightTable<R> right) {
		return selectBehavior().INNER_JOIN(right, this);
	}

	/**
	 * このクエリに LEFT OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, transient_transfers> LEFT_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().LEFT_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに RIGHT OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, transient_transfers> RIGHT_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().RIGHT_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに FULL OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, transient_transfers> FULL_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().FULL_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに CROSS JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return この {@link SelectStatement}
	 */
	public <R extends OnRightClauseAssist<?>> transient_transfers CROSS_JOIN(RightTable<R> right) {
		selectBehavior().CROSS_JOIN(right, this);
		return this;
	}

	/**
	 * UNION するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select UNION 対象
	 * @return この {@link SelectStatement}
	 */
	public transient_transfers UNION(SelectStatement select) {
		selectBehavior().UNION(select);
		return this;
	}

	/**
	 * UNION ALL するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select UNION ALL 対象
	 * @return この {@link SelectStatement}
	 */
	public transient_transfers UNION_ALL(SelectStatement select) {
		selectBehavior().UNION_ALL(select);
		return this;
	}

	/**
	 * ORDER BY 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public transient_transfers ORDER_BY(
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
	public transient_transfers setGroupByClause(GroupByClause clause) {
		selectBehavior().setGroupByClause(clause);
		return this;
	}

	/**
	 * 新規に ORDER BY 句をセットします。
	 * @param clause 新 ORDER BY 句
	 * @return {@link SelectStatement} 自身
	 * @throws IllegalStateException 既に ORDER BY 句がセットされている場合
	 */
	public transient_transfers setOrderByClause(OrderByClause clause) {
		selectBehavior().setOrderByClause(clause);
		return this;
	}

	/**
	 * 現時点の WHERE 句に新たな条件を AND 結合します。<br>
	 * AND 結合する対象がなければ、新条件としてセットされます。
	 * @param criteria AND 結合する新条件
	 * @return {@link SelectStatement} 自身
	 */
	public transient_transfers and(Criteria criteria) {
		selectBehavior().and(criteria);
		return this;
	}

	/**
	 * 現時点の WHERE 句に新たな条件を OR 結合します。<br>
	 * OR 結合する対象がなければ、新条件としてセットされます。
	 * @param criteria OR 結合する新条件
	 * @return {@link SelectStatement} 自身
	 */
	public transient_transfers or(Criteria criteria) {
		selectBehavior().or(criteria);
		return this;
	}

	/**
	 * 生成された SQL 文を加工する {SQLDecorator} を設定します。
	 * @param decorators {@link SQLDecorator}
	 * @return {@link SelectStatement} 自身
	 */
	@Override
	public transient_transfers apply(SQLDecorator... decorators) {
		for (SQLDecorator decorator : decorators) {
			this.decorators$.add(decorator);
		}

		return this;
	}

	@Override
	public Optimizer getOptimizer() {
		return selectBehavior().getOptimizer();
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
	public Iterator execute() {
		selectBehavior().checkRowMode();
		return wrap(selectBehavior().query().execute());
	}

	@Override
	public Optional<Row> fetch(String... primaryKeyMembers) {
		selectBehavior().checkRowMode();
		return selectBehavior().query().fetch(primaryKeyMembers).map(o -> createRow(o));
	}

	@Override
	public Optional<Row> fetch(Number... primaryKeyMembers) {
		selectBehavior().checkRowMode();
		return selectBehavior().query().fetch(primaryKeyMembers).map(o -> createRow(o));
	}

	@Override
	public Optional<Row> fetch(Bindable... primaryKeyMembers) {
		selectBehavior().checkRowMode();
		return selectBehavior().query().fetch(primaryKeyMembers).map(o -> createRow(o));
	}

	@Override
	public int count() {
		selectBehavior().checkRowMode();
		return selectBehavior().query().count();
	}

	@Override
	public ComposedSQL toCountSQL() {
		selectBehavior().checkRowMode();
		return selectBehavior().query().toCountSQL();
	}

	@Override
	public void aggregate(Consumer<BResultSet> consumer) {
		selectBehavior().quitRowMode();
		org.blendee.assist.Query.super.aggregate(consumer);
	}

	@Override
	public <T> T aggregateAndGet(Function<BResultSet, T> function) {
		selectBehavior().quitRowMode();
		return org.blendee.assist.Query.super.aggregateAndGet(function);
	}

	@Override
	public ResultSetIterator aggregate() {
		selectBehavior().quitRowMode();
		return org.blendee.assist.Query.super.aggregate();
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
	public transient_transfers resetWhere() {
		selectBehavior().resetWhere();
		return this;
	}

	/**
	 * 現在保持している HAVING 句をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetHaving() {
		selectBehavior().resetHaving();
		return this;
	}

	/**
	 * 現在保持している SELECT 句をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetSelect() {
		selectBehavior().resetSelect();
		return this;
	}

	/**
	 * 現在保持している GROUP BY 句をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetGroupBy() {
		selectBehavior().resetGroupBy();
		return this;
	}

	/**
	 * 現在保持している ORDER BY 句をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetOrderBy() {
		selectBehavior().resetOrderBy();
		return this;
	}

	/**
	 * 現在保持している UNION をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetUnions() {
		selectBehavior().resetUnions();
		return this;
	}

	/**
	 * 現在保持している JOIN をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetJoins() {
		selectBehavior().resetJoins();
		return this;
	}

	/**
	 * 現在保持している INSERT 文のカラムをリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetInsert() {
		dmsBehavior().resetInsert();
		return this;
	}

	/**
	 * 現在保持している UPDATE 文の更新要素をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetUpdate() {
		dmsBehavior().resetUpdate();
		return this;
	}

	/**
	 * 現在保持している SET 文の更新要素をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetDelete() {
		dmsBehavior().resetDelete();
		return this;
	}

	/**
	 * 現在保持している {@link SQLDecorator} をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers resetDecorators() {
		decorators$.clear();
		return this;
	}

	/**
	 * 現在保持している条件、並び順をリセットします。
	 * @return このインスタンス
	 */
	public transient_transfers reset() {
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
	public Query query() {
		return new Query(selectBehavior().query());
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
	 * INSERT 文を作成する {@link Consumer}
	 * @param function {@link Function}
	 * @return {@link DataManipulator}
	 */
	public DataManipulator insertStatement(Function<ListInsertAssist, DataManipulator> function) {
		return dmsBehavior().insertStatement(function);
	}

	/**
	 * UPDATE 文を作成する {@link Consumer}
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

	private static Class<?> getUsing(StackTraceElement element) {
		try {
			return Class.forName(element.getClassName());
		} catch (Exception e) {
			throw new IllegalStateException(e.toString());
		}
	}

	/**
	 * 自動生成された {@link TableFacadeAssist} の実装クラスです。<br>
	 * 条件として使用できるカラムを内包しており、それらを使用して検索 SQL を生成可能にします。
	 * @param <T> 使用されるカラムのタイプにあった型
	 * @param <M> Many 一対多の多側の型連鎖
	 */
	public static class Assist<T, M> implements TableFacadeAssist {

		final transient_transfers table$;

		private final CriteriaContext context$;

		private final TableFacadeAssist parent$;

		private final String fkName$;

		/**
		 * 項目名 id
		 */
		public final T id;

		/**
		 * 項目名 transient_id
		 */
		public final T transient_id;

		/**
		 * 項目名 group_id
		 */
		public final T group_id;

		/**
		 * 項目名 transferred_at
		 */
		public final T transferred_at;

		/**
		 * 項目名 extension
		 */
		public final T extension;

		/**
		 * 項目名 tags
		 */
		public final T tags;

		/**
		 * 項目名 completed
		 */
		public final T completed;

		/**
		 * 項目名 trigger_id
		 */
		public final T trigger_id;

		/**
		 * 項目名 parameter
		 */
		public final T parameter;

		/**
		 * 項目名 revision
		 */
		public final T revision;

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
			transient_transfers table$,
			TableFacadeContext<T> builder$,
			CriteriaContext context$,
			TableFacadeAssist parent$,
			String fkName$) {
			this.table$ = table$;
			this.context$ = context$;
			this.parent$ = parent$;
			this.fkName$ = fkName$;

			this.id = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.id);
			this.transient_id = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.transient_id);
			this.group_id = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.group_id);
			this.transferred_at = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.transferred_at);
			this.extension = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.extension);
			this.tags = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.tags);
			this.completed = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.completed);
			this.trigger_id = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.trigger_id);
			this.parameter = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.parameter);
			this.revision = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.revision);
			this.created_at = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.created_at);
			this.created_by = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.created_by);
			this.updated_at = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.updated_at);
			this.updated_by = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.updated_by);

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
			transient_transfers table$,
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
			transient_transfers table$,
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
			if (super.table$ != null) throw new IllegalStateException("このインスタンスでは直接使用することはできません");
			if (!getSelectStatement().rowMode()) throw new IllegalStateException("集計モードでは実行できない処理です");
			return new InstantOneToManyQuery<>(this, getSelectStatement().decorators());
		}

		/**
		 * 参照先テーブル名 groups<br>
		 * 外部キー名 transient_transfers_group_id_fkey<br>
		 * 項目名 group_id
		 * @return groups relationship
		 */
		public jp.ats.blackbox.blendee.bb.groups.ExtAssist<T, Many<jp.ats.blackbox.blendee.bb.transient_transfers.Row, M>> $groups() {
			return new jp.ats.blackbox.blendee.bb.groups.ExtAssist<>(
				builder$,
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.groups$transient_transfers_group_id_fkey);
		}

		/**
		 * 参照先テーブル名 transients<br>
		 * 外部キー名 transient_transfers_transient_id_fkey<br>
		 * 項目名 transient_id
		 * @return transients relationship
		 */
		public jp.ats.blackbox.blendee.bb.transients.ExtAssist<T, Many<jp.ats.blackbox.blendee.bb.transient_transfers.Row, M>> $transients() {
			return new jp.ats.blackbox.blendee.bb.transients.ExtAssist<>(
				builder$,
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.transients$transient_transfers_transient_id_fkey);
		}

		/**
		 * 参照先テーブル名 triggers<br>
		 * 外部キー名 transient_transfers_trigger_id_fkey<br>
		 * 項目名 trigger_id
		 * @return triggers relationship
		 */
		public jp.ats.blackbox.blendee.bb.triggers.ExtAssist<T, Many<jp.ats.blackbox.blendee.bb.transient_transfers.Row, M>> $triggers() {
			return new jp.ats.blackbox.blendee.bb.triggers.ExtAssist<>(
				builder$,
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.triggers$transient_transfers_trigger_id_fkey);
		}

		/**
		 * 参照先テーブル名 users<br>
		 * 外部キー名 transient_transfers_created_by_fkey<br>
		 * 項目名 created_by
		 * @return users relationship
		 */
		public jp.ats.blackbox.blendee.bb.users.ExtAssist<T, Many<jp.ats.blackbox.blendee.bb.transient_transfers.Row, M>> $users$transient_transfers_created_by_fkey() {
			return new jp.ats.blackbox.blendee.bb.users.ExtAssist<>(
				builder$,
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.users$transient_transfers_created_by_fkey);
		}

		/**
		 * 参照先テーブル名 users<br>
		 * 外部キー名 transient_transfers_updated_by_fkey<br>
		 * 項目名 updated_by
		 * @return users relationship
		 */
		public jp.ats.blackbox.blendee.bb.users.ExtAssist<T, Many<jp.ats.blackbox.blendee.bb.transient_transfers.Row, M>> $users$transient_transfers_updated_by_fkey() {
			return new jp.ats.blackbox.blendee.bb.users.ExtAssist<>(
				builder$,
				this,
				jp.ats.blackbox.blendee.bb.transient_transfers.users$transient_transfers_updated_by_fkey);
		}

	}

	/**
	 * SELECT 句用
	 */
	public static class SelectAssist extends ExtAssist<SelectCol, Void> implements SelectClauseAssist {

		private SelectAssist(
			transient_transfers table$,
			TableFacadeContext<SelectCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * SELECT 句用
	 */
	public static class ListSelectAssist extends SelectAssist implements ListSelectClauseAssist {

		private ListSelectAssist(
			transient_transfers table$,
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
			transient_transfers table$,
			TableFacadeContext<WhereColumn<WhereLogicalOperators>> builder$,
			CriteriaContext context$,
			WhereAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public WhereLogicalOperators IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (WhereLogicalOperators) getSelectStatement().getWhereLogicalOperators();
		}

		@Override
		public WhereLogicalOperators NOT_IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
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
			return new WhereColumn<>(
				getSelectStatement(),
				getContext(),
				new MultiColumn(template));
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
		public Statement getStatement() {
			return getSelectStatement();
		}
	}

	/**
	 * GROUB BY 句用
	 */
	public static class GroupByAssist extends ExtAssist<GroupByCol, Void> implements GroupByClauseAssist {

		private GroupByAssist(
			transient_transfers table$,
			TableFacadeContext<GroupByCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * GROUB BY 句用
	 */
	public static class ListGroupByAssist extends GroupByAssist implements ListGroupByClauseAssist {

		private ListGroupByAssist(
			transient_transfers table$,
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
			transient_transfers table$,
			TableFacadeContext<HavingColumn<HavingLogicalOperators>> builder$,
			CriteriaContext context$,
			HavingAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public HavingLogicalOperators IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (HavingLogicalOperators) getSelectStatement().getHavingLogicalOperators();
		}

		@Override
		public HavingLogicalOperators NOT_IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
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
			return new HavingColumn<>(
				getSelectStatement(),
				getContext(),
				new MultiColumn(template));
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
			transient_transfers table$,
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
			transient_transfers table$,
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
			transient_transfers table$,
			TableFacadeContext<OnLeftColumn<OnLeftLogicalOperators>> builder$,
			CriteriaContext context$,
			OnLeftAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public OnLeftLogicalOperators IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (OnLeftLogicalOperators) getSelectStatement().getOnLeftLogicalOperators();
		}

		@Override
		public OnLeftLogicalOperators NOT_IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
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
			return new OnLeftColumn<>(
				getSelectStatement(),
				getContext(),
				new MultiColumn(template));
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
			transient_transfers table$,
			TableFacadeContext<OnRightColumn<OnRightLogicalOperators>> builder$,
			CriteriaContext context$,
			OnRightAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public OnRightLogicalOperators IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (OnRightLogicalOperators) getSelectStatement().getOnRightLogicalOperators();
		}

		@Override
		public OnRightLogicalOperators NOT_IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
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
			return new OnRightColumn<>(
				getSelectStatement(),
				getContext(),
				new MultiColumn(template));
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
			transient_transfers table$,
			TableFacadeContext<InsertCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * INSERT 用
	 */
	public static class ListInsertAssist extends InsertAssist implements ListInsertClauseAssist {

		private ListInsertAssist(
			transient_transfers table$,
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
			transient_transfers table$,
			TableFacadeContext<UpdateCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * INSERT 用
	 */
	public static class ListUpdateAssist extends UpdateAssist implements ListUpdateClauseAssist<DMSWhereAssist> {

		private ListUpdateAssist(
			transient_transfers table$,
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
			transient_transfers table$,
			TableFacadeContext<WhereColumn<DMSWhereLogicalOperators>> builder$,
			CriteriaContext context$,
			DMSWhereAssist or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public DMSWhereLogicalOperators IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
			Helper.addInCriteria(this, false, mainColumns, subquery);
			return (DMSWhereLogicalOperators) getDataManipulationStatement().getWhereLogicalOperators();
		}

		@Override
		public DMSWhereLogicalOperators NOT_IN(Vargs<CriteriaColumn<?>> mainColumns, SelectStatement subquery) {
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
			return new WhereColumn<>(
				getDataManipulationStatement(),
				getContext(),
				new MultiColumn(template));
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
		public Statement getStatement() {
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
		public Iterator execute() {
			return wrap(inner.execute());
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
		public ComposedSQL toCountSQL() {
			return inner.toCountSQL();
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
