package sqlassist.bb;

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
 * schema: bb<br>
 * name: journals<br>
 * type: TABLE<br>
 * remarks: 伝票<br>
 */
@Table(name = "journals", schema = "bb", type = "TABLE", remarks = "伝票")
@PrimaryKey(name = "journals_pkey", columns = { "id" })
public class journals
	extends java.lang.Object
	implements
	TableFacade<Row>,
	SelectStatement,
	SQLDecorators,
	Query<journals.Iterator, journals.Row>,
	RightTable<journals.OnRightAssist> {

	/**
	 * この定数クラスのスキーマ名
	 */
	public static final String SCHEMA = "bb";

	/**
	 * この定数クラスのテーブル名
	 */
	public static final String TABLE = "journals";

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
	 * name: group_id<br>
	 * remarks: グループID<br>
	 * この伝票の属するグループ<br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "group_id", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "グループID\nこの伝票の属するグループ", defaultValue = "null", ordinalPosition = 2, notNull = true)
	public static final String group_id = "group_id";

	/**
	 * name: journal_batch_id<br>
	 * remarks: 移動伝票一括登録ID<br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "journal_batch_id", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "移動伝票一括登録ID", defaultValue = "null", ordinalPosition = 3, notNull = true)
	public static final String journal_batch_id = "journal_batch_id";

	/**
	 * name: fixed_at<br>
	 * remarks: 確定時刻<br>
	 * type: timestamptz(35, 6)<br>
	 * not null: true<br>
	 */
	@Column(name = "fixed_at", type = 93, typeName = "timestamptz", size = 35, hasDecimalDigits = true, decimalDigits = 6, remarks = "確定時刻", defaultValue = "null", ordinalPosition = 4, notNull = true)
	public static final String fixed_at = "fixed_at";

	/**
	 * name: props<br>
	 * remarks: 外部アプリケーション情報JSON<br>
	 * type: jsonb(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "props", type = 1111, typeName = "jsonb", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "外部アプリケーション情報JSON", defaultValue = "'{}'::jsonb", ordinalPosition = 5, notNull = true)
	public static final String props = "props";

	/**
	 * name: tags<br>
	 * remarks: 保存用タグ<br>
	 * type: _text(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "tags", type = 2003, typeName = "_text", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "保存用タグ", defaultValue = "'{}'::text[]", ordinalPosition = 6, notNull = true)
	public static final String tags = "tags";

	/**
	 * name: instance_id<br>
	 * remarks: 発生元インスタンスのID<br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "instance_id", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "発生元インスタンスのID", defaultValue = "null", ordinalPosition = 7, notNull = true)
	public static final String instance_id = "instance_id";

	/**
	 * name: denied_id<br>
	 * remarks: 取消元伝票ID<br>
	 * 訂正後の伝票が訂正前の伝票のIDを持つ<br>
	 * ここに入っているIDが指す伝票は、取り消されたものとなる<br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "denied_id", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "取消元伝票ID\n訂正後の伝票が訂正前の伝票のIDを持つ\nここに入っているIDが指す伝票は、取り消されたものとなる", defaultValue = "'00000000-0000-0000-0000-000000000000'::uuid", ordinalPosition = 8, notNull = true)
	public static final String denied_id = "denied_id";

	/**
	 * name: deny_reason<br>
	 * remarks: 取消理由<br>
	 * type: text(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "deny_reason", type = 12, typeName = "text", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "取消理由", defaultValue = "''::text", ordinalPosition = 9, notNull = true)
	public static final String deny_reason = "deny_reason";

	/**
	 * name: org_props<br>
	 * remarks: 組織のprops<br>
	 * type: jsonb(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "org_props", type = 1111, typeName = "jsonb", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "組織のprops", defaultValue = "null", ordinalPosition = 10, notNull = true)
	public static final String org_props = "org_props";

	/**
	 * name: group_props<br>
	 * remarks: グループのprops<br>
	 * type: jsonb(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "group_props", type = 1111, typeName = "jsonb", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "グループのprops", defaultValue = "null", ordinalPosition = 11, notNull = true)
	public static final String group_props = "group_props";

	/**
	 * name: user_props<br>
	 * remarks: 作成ユーザーのprops<br>
	 * type: jsonb(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "user_props", type = 1111, typeName = "jsonb", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "作成ユーザーのprops", defaultValue = "null", ordinalPosition = 12, notNull = true)
	public static final String user_props = "user_props";

	/**
	 * name: created_at<br>
	 * remarks: 作成時刻<br>
	 * type: timestamptz(35, 6)<br>
	 * not null: true<br>
	 */
	@Column(name = "created_at", type = 93, typeName = "timestamptz", size = 35, hasDecimalDigits = true, decimalDigits = 6, remarks = "作成時刻", defaultValue = "null", ordinalPosition = 13, notNull = true)
	public static final String created_at = "created_at";

	/**
	 * name: created_by<br>
	 * remarks: 作成ユーザー<br>
	 * type: uuid(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "created_by", type = 1111, typeName = "uuid", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "作成ユーザー", defaultValue = "null", ordinalPosition = 14, notNull = true)
	public static final String created_by = "created_by";

	/**
	 * name: journals_group_id_fkey<br>
	 * references: groups<br>
	 * columns: group_id
	 */
	@ForeignKey(name = "journals_group_id_fkey", references = "bb.groups", columns = { "group_id" }, refColumns = { "id" })
	public static final String bb$groups$journals_group_id_fkey = "journals_group_id_fkey";

	/**
	 * name: journals_instance_id_fkey<br>
	 * references: instances<br>
	 * columns: instance_id
	 */
	@ForeignKey(name = "journals_instance_id_fkey", references = "bb.instances", columns = { "instance_id" }, refColumns = { "id" })
	public static final String bb$instances$journals_instance_id_fkey = "journals_instance_id_fkey";

	/**
	 * name: journals_denied_id_fkey<br>
	 * references: journals<br>
	 * columns: denied_id
	 */
	@ForeignKey(name = "journals_denied_id_fkey", references = "bb.journals", columns = { "denied_id" }, refColumns = { "id" })
	public static final String bb$journals$journals_denied_id_fkey = "journals_denied_id_fkey";

	/**
	 * name: journals_journal_batch_id_fkey<br>
	 * references: journal_batches<br>
	 * columns: journal_batch_id
	 */
	@ForeignKey(name = "journals_journal_batch_id_fkey", references = "bb.journal_batches", columns = { "journal_batch_id" }, refColumns = { "id" })
	public static final String bb$journal_batches$journals_journal_batch_id_fkey = "journals_journal_batch_id_fkey";

	/**
	 * name: journals_created_by_fkey<br>
	 * references: users<br>
	 * columns: created_by
	 */
	@ForeignKey(name = "journals_created_by_fkey", references = "bb.users", columns = { "created_by" }, refColumns = { "id" })
	public static final String bb$users$journals_created_by_fkey = "journals_created_by_fkey";

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
		 * name: group_id<br>
		* remarks: グループID<br>
		* この伝票の属するグループ<br>
		* type: uuid(2147483647)<br>
		* not null: true<br>
		 * @param value java.util.UUID
		 */
		public void setGroup_id(java.util.UUID value) {
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
		* この伝票の属するグループ<br>
		* type: uuid(2147483647)<br>
		* not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getGroup_id() {
			Binder binder = data$.getValue("group_id");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * setter
		 * name: journal_batch_id<br>
		* remarks: 移動伝票一括登録ID<br>
		* type: uuid(2147483647)<br>
		* not null: true<br>
		 * @param value java.util.UUID
		 */
		public void setJournal_batch_id(java.util.UUID value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("journal_batch_id").getType());
			data$.setValue("journal_batch_id", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: journal_batch_id<br>
		* remarks: 移動伝票一括登録ID<br>
		* type: uuid(2147483647)<br>
		* not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getJournal_batch_id() {
			Binder binder = data$.getValue("journal_batch_id");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * setter
		 * name: fixed_at<br>
		* remarks: 確定時刻<br>
		* type: timestamptz(35, 6)<br>
		* not null: true<br>
		 * @param value java.sql.Timestamp
		 */
		public void setFixed_at(java.sql.Timestamp value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("fixed_at").getType());
			data$.setValue("fixed_at", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: fixed_at<br>
		* remarks: 確定時刻<br>
		* type: timestamptz(35, 6)<br>
		* not null: true<br>
		 * @return java.sql.Timestamp
		 */
		public java.sql.Timestamp getFixed_at() {
			Binder binder = data$.getValue("fixed_at");
			return (java.sql.Timestamp) binder.getValue();
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
		 * name: instance_id<br>
		* remarks: 発生元インスタンスのID<br>
		* type: uuid(2147483647)<br>
		* not null: true<br>
		 * @param value java.util.UUID
		 */
		public void setInstance_id(java.util.UUID value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("instance_id").getType());
			data$.setValue("instance_id", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: instance_id<br>
		* remarks: 発生元インスタンスのID<br>
		* type: uuid(2147483647)<br>
		* not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getInstance_id() {
			Binder binder = data$.getValue("instance_id");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * setter
		 * name: denied_id<br>
		* remarks: 取消元伝票ID<br>
		* 訂正後の伝票が訂正前の伝票のIDを持つ<br>
		* ここに入っているIDが指す伝票は、取り消されたものとなる<br>
		* type: uuid(2147483647)<br>
		* not null: true<br>
		 * @param value java.util.UUID
		 */
		public void setDenied_id(java.util.UUID value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("denied_id").getType());
			data$.setValue("denied_id", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: denied_id<br>
		* remarks: 取消元伝票ID<br>
		* 訂正後の伝票が訂正前の伝票のIDを持つ<br>
		* ここに入っているIDが指す伝票は、取り消されたものとなる<br>
		* type: uuid(2147483647)<br>
		* not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getDenied_id() {
			Binder binder = data$.getValue("denied_id");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * setter
		 * name: deny_reason<br>
		* remarks: 取消理由<br>
		* type: text(2147483647)<br>
		* not null: true<br>
		 * @param value java.lang.String
		 */
		public void setDeny_reason(java.lang.String value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("deny_reason").getType());
			data$.setValue("deny_reason", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: deny_reason<br>
		* remarks: 取消理由<br>
		* type: text(2147483647)<br>
		* not null: true<br>
		 * @return java.lang.String
		 */
		public java.lang.String getDeny_reason() {
			Binder binder = data$.getValue("deny_reason");
			return (java.lang.String) binder.getValue();
		}

		/**
		 * setter
		 * name: org_props<br>
		* remarks: 組織のprops<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @param value java.lang.Object
		 */
		public void setOrg_props(java.lang.Object value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("org_props").getType());
			data$.setValue("org_props", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: org_props<br>
		* remarks: 組織のprops<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @return java.lang.Object
		 */
		public java.lang.Object getOrg_props() {
			Binder binder = data$.getValue("org_props");
			return binder.getValue();
		}

		/**
		 * setter
		 * name: group_props<br>
		* remarks: グループのprops<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @param value java.lang.Object
		 */
		public void setGroup_props(java.lang.Object value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("group_props").getType());
			data$.setValue("group_props", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: group_props<br>
		* remarks: グループのprops<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @return java.lang.Object
		 */
		public java.lang.Object getGroup_props() {
			Binder binder = data$.getValue("group_props");
			return binder.getValue();
		}

		/**
		 * setter
		 * name: user_props<br>
		* remarks: 作成ユーザーのprops<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @param value java.lang.Object
		 */
		public void setUser_props(java.lang.Object value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("user_props").getType());
			data$.setValue("user_props", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: user_props<br>
		* remarks: 作成ユーザーのprops<br>
		* type: jsonb(2147483647)<br>
		* not null: true<br>
		 * @return java.lang.Object
		 */
		public java.lang.Object getUser_props() {
			Binder binder = data$.getValue("user_props");
			return binder.getValue();
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
		* remarks: 作成ユーザー<br>
		* type: uuid(2147483647)<br>
		* not null: true<br>
		 * @return java.util.UUID
		 */
		public java.util.UUID getCreated_by() {
			Binder binder = data$.getValue("created_by");
			return (java.util.UUID) binder.getValue();
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 groups<br>
		 * 外部キー名 journals_group_id_fkey<br>
		 * 項目名 group_id
		 * @return 参照しているレコードの Row
		 */
		public sqlassist.bb.groups.Row $groups() {
			return sqlassist.bb.groups.row(
				data$.getDataObject(bb$groups$journals_group_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 instances<br>
		 * 外部キー名 journals_instance_id_fkey<br>
		 * 項目名 instance_id
		 * @return 参照しているレコードの Row
		 */
		public sqlassist.bb.instances.Row $instances() {
			return sqlassist.bb.instances.row(
				data$.getDataObject(bb$instances$journals_instance_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 journals<br>
		 * 外部キー名 journals_denied_id_fkey<br>
		 * 項目名 denied_id
		 * @return 参照しているレコードの Row
		 */
		public sqlassist.bb.journals.Row $journals() {
			return sqlassist.bb.journals.row(
				data$.getDataObject(bb$journals$journals_denied_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 journal_batches<br>
		 * 外部キー名 journals_journal_batch_id_fkey<br>
		 * 項目名 journal_batch_id
		 * @return 参照しているレコードの Row
		 */
		public sqlassist.bb.journal_batches.Row $journal_batches() {
			return sqlassist.bb.journal_batches.row(
				data$.getDataObject(bb$journal_batches$journals_journal_batch_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 users<br>
		 * 外部キー名 journals_created_by_fkey<br>
		 * 項目名 created_by
		 * @return 参照しているレコードの Row
		 */
		public sqlassist.bb.users.Row $users() {
			return sqlassist.bb.users.row(
				data$.getDataObject(bb$users$journals_created_by_fkey));
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
			journals.this,
			whereContext$,
			CriteriaContext.OR,
			null);

		/**
		 * WHERE 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final WhereAssist AND = new WhereAssist(
			journals.this,
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
			journals.this,
			havingContext$,
			CriteriaContext.OR,
			null);

		/**
		 * HAVING 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final HavingAssist AND = new HavingAssist(
			journals.this,
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
			journals.this,
			onLeftContext$,
			CriteriaContext.OR,
			null);

		/**
		 * ON 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnLeftAssist AND = new OnLeftAssist(
			journals.this,
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
			journals.this,
			onRightContext$,
			CriteriaContext.OR,
			null);

		/**
		 * ON 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final OnRightAssist AND = new OnRightAssist(
			journals.this,
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
			journals.this,
			dmsWhereContext$,
			CriteriaContext.OR,
			null);

		/**
		 * WHERE 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeAssist} です。
		 */
		public final DMSWhereAssist AND = new DMSWhereAssist(
			journals.this,
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
			super($TABLE, getRuntimeId(), journals.this);
		}

		@Override
		protected SelectAssist newSelect() {
			return new SelectAssist(
				journals.this,
				selectContext$);
		}

		@Override
		protected ListSelectAssist newListSelect() {
			return new ListSelectAssist(
				journals.this,
				selectContext$);
		}

		@Override
		protected GroupByAssist newGroupBy() {
			return new GroupByAssist(
				journals.this,
				groupByContext$);
		}

		@Override
		protected ListGroupByAssist newListGroupBy() {
			return new ListGroupByAssist(
				journals.this,
				groupByContext$);
		}

		@Override
		protected OrderByAssist newOrderBy() {
			return new OrderByAssist(
				journals.this,
				orderByContext$);
		}

		@Override
		protected ListOrderByAssist newListOrderBy() {
			return new ListOrderByAssist(
				journals.this,
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
				journals.this.getRuntimeId(),
				journals.this);
		}

		@Override
		protected InsertAssist newInsert() {
			return new InsertAssist(
				journals.this,
				insertContext$);
		}

		@Override
		protected ListInsertAssist newListInsert() {
			return new ListInsertAssist(
				journals.this,
				insertContext$);
		}

		@Override
		protected UpdateAssist newUpdate() {
			return new UpdateAssist(
				journals.this,
				updateContext$);
		}

		@Override
		protected ListUpdateAssist newListUpdate() {
			return new ListUpdateAssist(
				journals.this,
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
	public static journals of(String id) {
		if (id == null || id.equals(""))
			throw new IllegalArgumentException("id が空です");

		return new journals(getUsing(new Throwable().getStackTrace()[1]), id);
	}

	/**
	 * 空のインスタンスを生成します。
	 */
	public journals() {}

	/**
	 * このクラスのインスタンスを生成します。<br>
	 * このコンストラクタで生成されたインスタンス の SELECT 句で使用されるカラムは、 パラメータの {@link Optimizer} に依存します。
	 * @param optimizer SELECT 句を決定する
	 */
	public journals(Optimizer optimizer) {
		selectBehavior().setOptimizer(Objects.requireNonNull(optimizer));
	}

	private journals(Class<?> using, String id) {
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
	public journals selectClause(Consumer<ListSelectAssist> consumer) {
		selectBehavior().selectClause(consumer);
		return this;
	}

	/**
	 * GROUP BY 句を作成する {@link Consumer}
	 * @param consumer {@link Consumer}
	 * @return this
	 */
	public journals groupByClause(Consumer<ListGroupByAssist> consumer) {
		selectBehavior().groupByClause(consumer);
		return this;
	}

	/**
	 * GROUP BY 句を作成する {@link Consumer}
	 * @param consumer {@link Consumer}
	 * @return this
	 */
	public journals orderByClause(Consumer<ListOrderByAssist> consumer) {
		selectBehavior().orderByClause(consumer);
		return this;
	}

	/**
	 * SELECT 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public journals SELECT(
		SelectOfferFunction<SelectAssist> function) {
		selectBehavior().SELECT(function);
		return this;
	}

	/**
	 * DISTINCT を使用した SELECT 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public journals SELECT_DISTINCT(
		SelectOfferFunction<SelectAssist> function) {
		selectBehavior().SELECT_DISTINCT(function);
		return this;
	}

	/**
	 * COUNT(*) を使用した SELECT 句を記述します。
	 * @return この {@link SelectStatement}
	 */
	public journals SELECT_COUNT() {
		selectBehavior().SELECT_COUNT();
		return this;
	}

	/**
	 * GROUP BY 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public journals GROUP_BY(
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
	public final journals WHERE(
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
	public final journals HAVING(
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
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, journals> INNER_JOIN(RightTable<R> right) {
		return selectBehavior().INNER_JOIN(right, this);
	}

	/**
	 * このクエリに LEFT OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, journals> LEFT_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().LEFT_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに RIGHT OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, journals> RIGHT_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().RIGHT_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに FULL OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightClauseAssist<?>> OnClause<OnLeftAssist, R, journals> FULL_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().FULL_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに CROSS JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return この {@link SelectStatement}
	 */
	public <R extends OnRightClauseAssist<?>> journals CROSS_JOIN(RightTable<R> right) {
		selectBehavior().CROSS_JOIN(right, this);
		return this;
	}

	/**
	 * UNION するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select UNION 対象
	 * @return この {@link SelectStatement}
	 */
	public journals UNION(SelectStatement select) {
		selectBehavior().UNION(select);
		return this;
	}

	/**
	 * UNION ALL するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select UNION ALL 対象
	 * @return この {@link SelectStatement}
	 */
	public journals UNION_ALL(SelectStatement select) {
		selectBehavior().UNION_ALL(select);
		return this;
	}

	/**
	 * INTERSECT するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select INTERSECT 対象
	 * @return この {@link SelectStatement}
	 */
	public journals INTERSECT(SelectStatement select) {
		selectBehavior().INTERSECT(select);
		return this;
	}

	/**
	 * INTERSECT ALL するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select INTERSECT ALL 対象
	 * @return この {@link SelectStatement}
	 */
	public journals INTERSECT_ALL(SelectStatement select) {
		selectBehavior().INTERSECT_ALL(select);
		return this;
	}

	/**
	 * EXCEPT するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select EXCEPT 対象
	 * @return この {@link SelectStatement}
	 */
	public journals EXCEPT(SelectStatement select) {
		selectBehavior().INTERSECT(select);
		return this;
	}

	/**
	 * EXCEPT ALL するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select EXCEPT ALL 対象
	 * @return この {@link SelectStatement}
	 */
	public journals EXCEPT_ALL(SelectStatement select) {
		selectBehavior().EXCEPT_ALL(select);
		return this;
	}

	/**
	 * ORDER BY 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public journals ORDER_BY(
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
	public journals setGroupByClause(GroupByClause clause) {
		selectBehavior().setGroupByClause(clause);
		return this;
	}

	/**
	 * 新規に ORDER BY 句をセットします。
	 * @param clause 新 ORDER BY 句
	 * @return {@link SelectStatement} 自身
	 * @throws IllegalStateException 既に ORDER BY 句がセットされている場合
	 */
	public journals setOrderByClause(OrderByClause clause) {
		selectBehavior().setOrderByClause(clause);
		return this;
	}

	/**
	 * 現時点の WHERE 句に新たな条件を AND 結合します。<br>
	 * AND 結合する対象がなければ、新条件としてセットされます。
	 * @param criteria AND 結合する新条件
	 * @return {@link SelectStatement} 自身
	 */
	public journals and(Criteria criteria) {
		selectBehavior().and(criteria);
		return this;
	}

	/**
	 * 現時点の WHERE 句に新たな条件を OR 結合します。<br>
	 * OR 結合する対象がなければ、新条件としてセットされます。
	 * @param criteria OR 結合する新条件
	 * @return {@link SelectStatement} 自身
	 */
	public journals or(Criteria criteria) {
		selectBehavior().or(criteria);
		return this;
	}

	/**
	 * 生成された SQL 文を加工する {SQLDecorator} を設定します。
	 * @param decorators {@link SQLDecorator}
	 * @return {@link SelectStatement} 自身
	 */
	@Override
	public journals accept(SQLDecorator... decorators) {
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
		SelectBehavior selectBehavior = selectBehavior();
		selectBehavior.checkRowMode();
		return wrap(selectBehavior.query().execute());
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
	public journals resetWhere() {
		selectBehavior().resetWhere();
		return this;
	}

	/**
	 * 現在保持している HAVING 句をリセットします。
	 * @return このインスタンス
	 */
	public journals resetHaving() {
		selectBehavior().resetHaving();
		return this;
	}

	/**
	 * 現在保持している SELECT 句をリセットします。
	 * @return このインスタンス
	 */
	public journals resetSelect() {
		selectBehavior().resetSelect();
		return this;
	}

	/**
	 * 現在保持している GROUP BY 句をリセットします。
	 * @return このインスタンス
	 */
	public journals resetGroupBy() {
		selectBehavior().resetGroupBy();
		return this;
	}

	/**
	 * 現在保持している ORDER BY 句をリセットします。
	 * @return このインスタンス
	 */
	public journals resetOrderBy() {
		selectBehavior().resetOrderBy();
		return this;
	}

	/**
	 * 現在保持している UNION をリセットします。
	 * @return このインスタンス
	 */
	public journals resetUnions() {
		selectBehavior().resetUnions();
		return this;
	}

	/**
	 * 現在保持している JOIN をリセットします。
	 * @return このインスタンス
	 */
	public journals resetJoins() {
		selectBehavior().resetJoins();
		return this;
	}

	/**
	 * 現在保持している INSERT 文のカラムをリセットします。
	 * @return このインスタンス
	 */
	public journals resetInsert() {
		dmsBehavior().resetInsert();
		return this;
	}

	/**
	 * 現在保持している UPDATE 文の更新要素をリセットします。
	 * @return このインスタンス
	 */
	public journals resetUpdate() {
		dmsBehavior().resetUpdate();
		return this;
	}

	/**
	 * 現在保持している SET 文の更新要素をリセットします。
	 * @return このインスタンス
	 */
	public journals resetDelete() {
		dmsBehavior().resetDelete();
		return this;
	}

	/**
	 * 現在保持している {@link SQLDecorator} をリセットします。
	 * @return このインスタンス
	 */
	public journals resetDecorators() {
		decorators$.clear();
		return this;
	}

	/**
	 * 現在保持している条件、並び順をリセットします。
	 * @return このインスタンス
	 */
	public journals reset() {
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

		final journals table$;

		private final CriteriaContext context$;

		private final TableFacadeAssist parent$;

		private final String fkName$;

		/**
		 * 項目名 id
		 */
		public final T id;

		/**
		 * 項目名 group_id
		 */
		public final T group_id;

		/**
		 * 項目名 journal_batch_id
		 */
		public final T journal_batch_id;

		/**
		 * 項目名 fixed_at
		 */
		public final T fixed_at;

		/**
		 * 項目名 props
		 */
		public final T props;

		/**
		 * 項目名 tags
		 */
		public final T tags;

		/**
		 * 項目名 instance_id
		 */
		public final T instance_id;

		/**
		 * 項目名 denied_id
		 */
		public final T denied_id;

		/**
		 * 項目名 deny_reason
		 */
		public final T deny_reason;

		/**
		 * 項目名 org_props
		 */
		public final T org_props;

		/**
		 * 項目名 group_props
		 */
		public final T group_props;

		/**
		 * 項目名 user_props
		 */
		public final T user_props;

		/**
		 * 項目名 created_at
		 */
		public final T created_at;

		/**
		 * 項目名 created_by
		 */
		public final T created_by;

		private Assist(
			journals table$,
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
				sqlassist.bb.journals.id);
			this.group_id = builder$.buildColumn(
				this,
				sqlassist.bb.journals.group_id);
			this.journal_batch_id = builder$.buildColumn(
				this,
				sqlassist.bb.journals.journal_batch_id);
			this.fixed_at = builder$.buildColumn(
				this,
				sqlassist.bb.journals.fixed_at);
			this.props = builder$.buildColumn(
				this,
				sqlassist.bb.journals.props);
			this.tags = builder$.buildColumn(
				this,
				sqlassist.bb.journals.tags);
			this.instance_id = builder$.buildColumn(
				this,
				sqlassist.bb.journals.instance_id);
			this.denied_id = builder$.buildColumn(
				this,
				sqlassist.bb.journals.denied_id);
			this.deny_reason = builder$.buildColumn(
				this,
				sqlassist.bb.journals.deny_reason);
			this.org_props = builder$.buildColumn(
				this,
				sqlassist.bb.journals.org_props);
			this.group_props = builder$.buildColumn(
				this,
				sqlassist.bb.journals.group_props);
			this.user_props = builder$.buildColumn(
				this,
				sqlassist.bb.journals.user_props);
			this.created_at = builder$.buildColumn(
				this,
				sqlassist.bb.journals.created_at);
			this.created_by = builder$.buildColumn(
				this,
				sqlassist.bb.journals.created_by);

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
			journals table$,
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
			journals table$,
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
		 * 外部キー名 journals_group_id_fkey<br>
		 * 項目名 group_id
		 * @return groups relationship
		 */
		public sqlassist.bb.groups.ExtAssist<T, Many<sqlassist.bb.journals.Row, M>> $groups() {
			return new sqlassist.bb.groups.ExtAssist<>(
				builder$,
				this,
				sqlassist.bb.journals.bb$groups$journals_group_id_fkey);
		}

		/**
		 * 参照先テーブル名 instances<br>
		 * 外部キー名 journals_instance_id_fkey<br>
		 * 項目名 instance_id
		 * @return instances relationship
		 */
		public sqlassist.bb.instances.ExtAssist<T, Many<sqlassist.bb.journals.Row, M>> $instances() {
			return new sqlassist.bb.instances.ExtAssist<>(
				builder$,
				this,
				sqlassist.bb.journals.bb$instances$journals_instance_id_fkey);
		}

		/**
		 * 参照先テーブル名 journals<br>
		 * 外部キー名 journals_denied_id_fkey<br>
		 * 項目名 denied_id
		 * @return journals relationship
		 */
		public sqlassist.bb.journals.ExtAssist<T, Many<sqlassist.bb.journals.Row, M>> $journals() {
			return new sqlassist.bb.journals.ExtAssist<>(
				builder$,
				this,
				sqlassist.bb.journals.bb$journals$journals_denied_id_fkey);
		}

		/**
		 * 参照先テーブル名 journal_batches<br>
		 * 外部キー名 journals_journal_batch_id_fkey<br>
		 * 項目名 journal_batch_id
		 * @return journal_batches relationship
		 */
		public sqlassist.bb.journal_batches.ExtAssist<T, Many<sqlassist.bb.journals.Row, M>> $journal_batches() {
			return new sqlassist.bb.journal_batches.ExtAssist<>(
				builder$,
				this,
				sqlassist.bb.journals.bb$journal_batches$journals_journal_batch_id_fkey);
		}

		/**
		 * 参照先テーブル名 users<br>
		 * 外部キー名 journals_created_by_fkey<br>
		 * 項目名 created_by
		 * @return users relationship
		 */
		public sqlassist.bb.users.ExtAssist<T, Many<sqlassist.bb.journals.Row, M>> $users() {
			return new sqlassist.bb.users.ExtAssist<>(
				builder$,
				this,
				sqlassist.bb.journals.bb$users$journals_created_by_fkey);
		}

	}

	/**
	 * SELECT 句用
	 */
	public static class SelectAssist extends ExtAssist<SelectCol, Void> implements SelectClauseAssist {

		private SelectAssist(
			journals table$,
			TableFacadeContext<SelectCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * SELECT 句用
	 */
	public static class ListSelectAssist extends SelectAssist implements ListSelectClauseAssist {

		private ListSelectAssist(
			journals table$,
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
			journals table$,
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
			journals table$,
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
			journals table$,
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
			journals table$,
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
			journals table$,
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
			journals table$,
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
			journals table$,
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
			journals table$,
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
			journals table$,
			TableFacadeContext<InsertCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * INSERT 用
	 */
	public static class ListInsertAssist extends InsertAssist implements ListInsertClauseAssist {

		private ListInsertAssist(
			journals table$,
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
			journals table$,
			TableFacadeContext<UpdateCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * INSERT 用
	 */
	public static class ListUpdateAssist extends UpdateAssist implements ListUpdateClauseAssist<DMSWhereAssist> {

		private ListUpdateAssist(
			journals table$,
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
			journals table$,
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