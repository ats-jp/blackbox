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
import org.blendee.support.CriteriaContext;
import org.blendee.support.DataManipulationStatement;
import org.blendee.support.DataManipulationStatementBehavior;
import org.blendee.support.DataManipulator;
import org.blendee.support.DeleteStatementIntermediate;
import org.blendee.support.GroupByColumn;
import org.blendee.support.GroupByOfferFunction;
import org.blendee.support.GroupByRelationship;
import org.blendee.support.HavingColumn;
import org.blendee.support.HavingRelationship;
import org.blendee.support.InsertColumn;
import org.blendee.support.InsertOfferFunction;
import org.blendee.support.InsertRelationship;
import org.blendee.support.InsertStatementIntermediate;
import org.blendee.support.InstantOneToManyQuery;
import org.blendee.support.annotation.PrimaryKey;
import org.blendee.support.annotation.ForeignKey;
import org.blendee.support.Many;
import org.blendee.support.LogicalOperators;
import org.blendee.support.OnClause;
import org.blendee.support.OnLeftColumn;
import org.blendee.support.OnLeftRelationship;
import org.blendee.support.OnRightColumn;
import org.blendee.support.OnRightRelationship;
import org.blendee.support.OneToManyQuery;
import org.blendee.support.OneToManyRelationship;
import org.blendee.support.OrderByColumn;
import org.blendee.support.OrderByOfferFunction;
import org.blendee.support.OrderByRelationship;
import org.blendee.support.Query;
import org.blendee.support.RightTable;
import org.blendee.support.Row;
import org.blendee.support.RowIterator;
import org.blendee.support.SelectColumn;
import org.blendee.support.SelectOfferFunction;
import org.blendee.support.SelectRelationship;
import org.blendee.support.Statement;
import org.blendee.support.SelectStatement;
import org.blendee.support.SelectStatementBehavior;
import org.blendee.support.SelectStatementBehavior.PlaybackQuery;
import org.blendee.support.TableFacade;
import org.blendee.support.TableFacadeColumn;
import org.blendee.support.TableFacadeContext;
import org.blendee.support.TableFacadeRelationship;
import org.blendee.support.UpdateColumn;
import org.blendee.support.UpdateRelationship;
import org.blendee.support.UpdateStatementIntermediate;
import org.blendee.support.WhereColumn;
import org.blendee.support.WhereRelationship;
import org.blendee.support.SQLDecorators;
import org.blendee.support.annotation.Column;
import org.blendee.support.Paren;

import org.blendee.support.annotation.Table;

/**
 * 自動生成されたテーブル操作クラスです。
 * schema: bb<br>
 * name: statuses<br>
 * type: TABLE<br>
 * remarks: 状態<br>
 * Blackbox内でのアイテムの状態<br>
 */
@Table(name = "statuses", schema = "bb", type = "TABLE", remarks = "状態\nBlackbox内でのアイテムの状態")
@PrimaryKey(name = "statuses_pkey", columns = { "id" })
public class statuses
	extends java.lang.Object
	implements
	TableFacade<Row>,
	SelectStatement,
	SQLDecorators,
	Query<statuses.Iterator, statuses.Row>,
	RightTable<statuses.OnRightRel> {

	/**
	 * この定数クラスのスキーマ名
	 */
	public static final String SCHEMA = "bb";

	/**
	 * この定数クラスのテーブル名
	 */
	public static final String TABLE = "statuses";

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
	@Column(name = "id", type = -5, typeName = "bigserial", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "ID", defaultValue = "nextval('bb.statuses_id_seq'::regclass)", ordinalPosition = 1, notNull = true)
	public static final String id = "id";

	/**
	 * name: group_id<br>
	 * remarks: グループID<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "group_id", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "グループID", defaultValue = "null", ordinalPosition = 2, notNull = true)
	public static final String group_id = "group_id";

	/**
	 * name: name<br>
	 * remarks: 名称<br>
	 * type: text(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "name", type = 12, typeName = "text", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "名称", defaultValue = "null", ordinalPosition = 3, notNull = true)
	public static final String name = "name";

	/**
	 * name: revision<br>
	 * remarks: リビジョン番号<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "revision", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "リビジョン番号", defaultValue = "0", ordinalPosition = 4, notNull = true)
	public static final String revision = "revision";

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
	 * remarks: log保存用タグ<br>
	 * type: _text(2147483647)<br>
	 * not null: true<br>
	 */
	@Column(name = "tags", type = 2003, typeName = "_text", size = 2147483647, hasDecimalDigits = true, decimalDigits = 0, remarks = "log保存用タグ", defaultValue = "'{}'::text[]", ordinalPosition = 6, notNull = true)
	public static final String tags = "tags";

	/**
	 * name: active<br>
	 * remarks: アクティブフラグ<br>
	 * type: bool(1)<br>
	 * not null: true<br>
	 */
	@Column(name = "active", type = -7, typeName = "bool", size = 1, hasDecimalDigits = true, decimalDigits = 0, remarks = "アクティブフラグ", defaultValue = "true", ordinalPosition = 7, notNull = true)
	public static final String active = "active";

	/**
	 * name: created_at<br>
	 * remarks: 作成時刻<br>
	 * type: timestamptz(35, 6)<br>
	 * not null: true<br>
	 */
	@Column(name = "created_at", type = 93, typeName = "timestamptz", size = 35, hasDecimalDigits = true, decimalDigits = 6, remarks = "作成時刻", defaultValue = "now()", ordinalPosition = 8, notNull = true)
	public static final String created_at = "created_at";

	/**
	 * name: created_by<br>
	 * remarks: 作成ユーザー<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "created_by", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "作成ユーザー", defaultValue = "null", ordinalPosition = 9, notNull = true)
	public static final String created_by = "created_by";

	/**
	 * name: updated_at<br>
	 * remarks: 更新時刻<br>
	 * type: timestamptz(35, 6)<br>
	 * not null: true<br>
	 */
	@Column(name = "updated_at", type = 93, typeName = "timestamptz", size = 35, hasDecimalDigits = true, decimalDigits = 6, remarks = "更新時刻", defaultValue = "now()", ordinalPosition = 10, notNull = true)
	public static final String updated_at = "updated_at";

	/**
	 * name: updated_by<br>
	 * remarks: 更新ユーザー<br>
	 * type: int8(19)<br>
	 * not null: true<br>
	 */
	@Column(name = "updated_by", type = -5, typeName = "int8", size = 19, hasDecimalDigits = true, decimalDigits = 0, remarks = "更新ユーザー", defaultValue = "null", ordinalPosition = 11, notNull = true)
	public static final String updated_by = "updated_by";

	/**
	 * name: statuses_group_id_fkey<br>
	 * reference: groups<br>
	 * columns: group_id
	 */
	@ForeignKey(name = "statuses_group_id_fkey", references = "groups", columns = { "group_id" }, refColumns = { "id" })
	public static final String groups$statuses_group_id_fkey = "statuses_group_id_fkey";

	/**
	 * name: statuses_created_by_fkey<br>
	 * reference: users<br>
	 * columns: created_by
	 */
	@ForeignKey(name = "statuses_created_by_fkey", references = "users", columns = { "created_by" }, refColumns = { "id" })
	public static final String users$statuses_created_by_fkey = "statuses_created_by_fkey";

	/**
	 * name: statuses_updated_by_fkey<br>
	 * reference: users<br>
	 * columns: updated_by
	 */
	@ForeignKey(name = "statuses_updated_by_fkey", references = "users", columns = { "updated_by" }, refColumns = { "id" })
	public static final String users$statuses_updated_by_fkey = "statuses_updated_by_fkey";

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
		implements org.blendee.support.Row {

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
		 * name: name<br>
		* remarks: 名称<br>
		* type: text(2147483647)<br>
		* not null: true<br>
		 * @param value java.lang.String
		 */
		public void setName(java.lang.String value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("name").getType());
			data$.setValue("name", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: name<br>
		* remarks: 名称<br>
		* type: text(2147483647)<br>
		* not null: true<br>
		 * @return java.lang.String
		 */
		public java.lang.String getName() {
			Binder binder = data$.getValue("name");
			return (java.lang.String) binder.getValue();
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
		* remarks: log保存用タグ<br>
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
		* remarks: log保存用タグ<br>
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
		 * name: active<br>
		* remarks: アクティブフラグ<br>
		* type: bool(1)<br>
		* not null: true<br>
		 * @param value java.lang.Boolean
		 */
		public void setActive(java.lang.Boolean value) {
			Objects.requireNonNull(value);
			ValueExtractor valueExtractor = ContextManager.get(ValueExtractorsConfigure.class)
				.getValueExtractors()
				.selectValueExtractor(
					rowRel$.getColumn("active").getType());
			data$.setValue("active", valueExtractor.extractAsBinder(value));
		}

		/**
		 * getter
		 * name: active<br>
		* remarks: アクティブフラグ<br>
		* type: bool(1)<br>
		* not null: true<br>
		 * @return java.lang.Boolean
		 */
		public java.lang.Boolean getActive() {
			Binder binder = data$.getValue("active");
			return (java.lang.Boolean) binder.getValue();
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
		 * 外部キー名 statuses_group_id_fkey<br>
		 * 項目名 group_id
		 * @return 参照しているレコードの Row
		 */
		public jp.ats.blackbox.blendee.bb.groups.Row $groups() {
			return jp.ats.blackbox.blendee.bb.groups.row(
				data$.getDataObject(groups$statuses_group_id_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 users<br>
		 * 外部キー名 statuses_created_by_fkey<br>
		 * 項目名 created_by
		 * @return 参照しているレコードの Row
		 */
		public jp.ats.blackbox.blendee.bb.users.Row $users$statuses_created_by_fkey() {
			return jp.ats.blackbox.blendee.bb.users.row(
				data$.getDataObject(users$statuses_created_by_fkey));
		}

		/**
		 * このレコードが参照しているレコードの Row を返します。<br>
		 * 参照先テーブル名 users<br>
		 * 外部キー名 statuses_updated_by_fkey<br>
		 * 項目名 updated_by
		 * @return 参照しているレコードの Row
		 */
		public jp.ats.blackbox.blendee.bb.users.Row $users$statuses_updated_by_fkey() {
			return jp.ats.blackbox.blendee.bb.users.row(
				data$.getDataObject(users$statuses_updated_by_fkey));
		}

	}

	private static final TableFacadeContext<SelectCol> selectContext$ = (relationship, name) -> new SelectCol(relationship, name);

	private static final TableFacadeContext<GroupByCol> groupByContext$ = (relationship, name) -> new GroupByCol(relationship, name);

	private static final TableFacadeContext<OrderByCol> orderByContext$ = (relationship, name) -> new OrderByCol(relationship, name);

	private static final TableFacadeContext<InsertCol> insertContext$ = (relationship, name) -> new InsertCol(relationship, name);

	private static final TableFacadeContext<UpdateCol> updateContext$ = (relationship, name) -> new UpdateCol(relationship, name);

	private static final TableFacadeContext<WhereColumn<WhereLogicalOperators>> whereContext$ = TableFacadeContext.newWhereBuilder();

	private static final TableFacadeContext<HavingColumn<HavingLogicalOperators>> havingContext$ = TableFacadeContext.newHavingBuilder();

	private static final TableFacadeContext<OnLeftColumn<OnLeftLogicalOperators>> onLeftContext$ = TableFacadeContext.newOnLeftBuilder();

	private static final TableFacadeContext<OnRightColumn<OnRightLogicalOperators>> onRightContext$ = TableFacadeContext.newOnRightBuilder();

	private static final TableFacadeContext<WhereColumn<DMSWhereLogicalOperators>> dmsWhereContext$ = TableFacadeContext.newDMSWhereBuilder();

	/**
	 * WHERE 句 で使用する AND, OR です。
	 */
	public class WhereLogicalOperators implements LogicalOperators<WhereRel> {

		private WhereLogicalOperators() {}

		/**
		 * WHERE 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final WhereRel OR = new WhereRel(
			statuses.this,
			whereContext$,
			CriteriaContext.OR,
			null);

		/**
		 * WHERE 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final WhereRel AND = new WhereRel(
			statuses.this,
			whereContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public WhereRel defaultOperator() {
			return AND;
		}
	}

	/**
	 * HAVING 句 で使用する AND, OR です。
	 */
	public class HavingLogicalOperators implements LogicalOperators<HavingRel> {

		private HavingLogicalOperators() {}

		/**
		 * HAVING 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final HavingRel OR = new HavingRel(
			statuses.this,
			havingContext$,
			CriteriaContext.OR,
			null);

		/**
		 * HAVING 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final HavingRel AND = new HavingRel(
			statuses.this,
			havingContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public HavingRel defaultOperator() {
			return AND;
		}
	}

	/**
	 * ON 句 (LEFT) で使用する AND, OR です。
	 */
	public class OnLeftLogicalOperators implements LogicalOperators<OnLeftRel> {

		private OnLeftLogicalOperators() {}

		/**
		 * ON 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final OnLeftRel OR = new OnLeftRel(
			statuses.this,
			onLeftContext$,
			CriteriaContext.OR,
			null);

		/**
		 * ON 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final OnLeftRel AND = new OnLeftRel(
			statuses.this,
			onLeftContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public OnLeftRel defaultOperator() {
			return AND;
		}
	}

	/**
	 * ON 句 (RIGHT) で使用する AND, OR です。
	 */
	public class OnRightLogicalOperators implements LogicalOperators<OnRightRel> {

		private OnRightLogicalOperators() {}

		/**
		 * ON 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final OnRightRel OR = new OnRightRel(
			statuses.this,
			onRightContext$,
			CriteriaContext.OR,
			null);

		/**
		 * ON 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final OnRightRel AND = new OnRightRel(
			statuses.this,
			onRightContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public OnRightRel defaultOperator() {
			return AND;
		}
	}

	/**
	 * WHERE 句 で使用する AND, OR です。
	 */
	public class DMSWhereLogicalOperators implements LogicalOperators<DMSWhereRel> {

		private DMSWhereLogicalOperators() {}

		/**
		 * WHERE 句に OR 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final DMSWhereRel OR = new DMSWhereRel(
			statuses.this,
			dmsWhereContext$,
			CriteriaContext.OR,
			null);

		/**
		 * WHERE 句に AND 結合する条件用のカラムを選択するための {@link TableFacadeRelationship} です。
		 */
		public final DMSWhereRel AND = new DMSWhereRel(
			statuses.this,
			dmsWhereContext$,
			CriteriaContext.AND,
			OR);

		@Override
		public DMSWhereRel defaultOperator() {
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

	private class SelectBehavior extends SelectStatementBehavior<SelectRel, GroupByRel, WhereRel, HavingRel, OrderByRel, OnLeftRel> {

		private SelectBehavior() {
			super($TABLE, getRuntimeId(), statuses.this);
		}

		@Override
		protected SelectRel newSelect() {
			return new SelectRel(
				statuses.this,
				selectContext$);
		}

		@Override
		protected GroupByRel newGroupBy() {
			return new GroupByRel(
				statuses.this,
				groupByContext$);
		}

		@Override
		protected OrderByRel newOrderBy() {
			return new OrderByRel(
				statuses.this,
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

	private class DMSBehavior extends DataManipulationStatementBehavior<InsertRel, UpdateRel, DMSWhereRel> {

		public DMSBehavior() {
			super($TABLE, statuses.this.getRuntimeId(), statuses.this);
		}

		@Override
		protected InsertRel newInsert() {
			return new InsertRel(
				statuses.this,
				insertContext$);
		}

		@Override
		protected UpdateRel newUpdate() {
			return new UpdateRel(
				statuses.this,
				updateContext$);
		}

		@Override
		protected LogicalOperators<DMSWhereRel> newWhereOperators() {
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
	public static statuses of(String id) {
		if (id == null || id.equals(""))
			throw new IllegalArgumentException("id が空です");

		return new statuses(getUsing(new Throwable().getStackTrace()[1]), id);
	}

	/**
	 * 空のインスタンスを生成します。
	 */
	public statuses() {}

	/**
	 * このクラスのインスタンスを生成します。<br>
	 * このコンストラクタで生成されたインスタンス の SELECT 句で使用されるカラムは、 パラメータの {@link Optimizer} に依存します。
	 * @param optimizer SELECT 句を決定する
	 */
	public statuses(Optimizer optimizer) {
		selectBehavior().setOptimizer(Objects.requireNonNull(optimizer));
	}

	private statuses(Class<?> using, String id) {
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
	 * この {@link SelectStatement} のテーブルを表す {@link TableFacadeRelationship} を参照するためのインスタンスです。
	 * @return rel
	 */
	public ExtRel<TableFacadeColumn, Void> rel() {
		return new ExtRel<>(this, TableFacadeContext.OTHER, CriteriaContext.NULL);
	}

	/**
	 * SELECT 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public statuses SELECT(
		SelectOfferFunction<SelectRel> function) {
		selectBehavior().SELECT(function);
		return this;
	}

	/**
	 * DISTINCT を使用した SELECT 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public statuses SELECT_DISTINCT(
		SelectOfferFunction<SelectRel> function) {
		selectBehavior().SELECT_DISTINCT(function);
		return this;
	}

	/**
	 * COUNT(*) を使用した SELECT 句を記述します。
	 * @return この {@link SelectStatement}
	 */
	public statuses SELECT_COUNT() {
		selectBehavior().SELECT_COUNT();
		return this;
	}

	/**
	 * GROUP BY 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public statuses GROUP_BY(
		GroupByOfferFunction<GroupByRel> function) {
		selectBehavior().GROUP_BY(function);
		return this;
	}

	/**
	 * WHERE 句を記述します。
	 * @param consumers
	 * @return この {@link SelectStatement}
	 */
	@SafeVarargs
	public final statuses WHERE(
		Consumer<WhereRel>... consumers) {
		selectBehavior().WHERE(consumers);
		return this;
	}

	/**
	 * WHERE 句で使用できる {@link  Criteria} を作成します。
	 * @param consumer {@link Consumer}
	 * @return {@link Criteria}
	 */
	public Criteria createWhereCriteria(
		Consumer<WhereRel> consumer) {
		return selectBehavior().createWhereCriteria(consumer);
	}

	/**
	 * HAVING 句を記述します。
	 * @param consumers
	 * @return この {@link SelectStatement}
	 */
	@SafeVarargs
	public final statuses HAVING(
		Consumer<HavingRel>... consumers) {
		selectBehavior().HAVING(consumers);
		return this;
	}

	/**
	 * HAVING 句で使用できる {@link  Criteria} を作成します。
	 * @param consumer {@link Consumer}
	 * @return {@link Criteria}
	 */
	public Criteria createHavingCriteria(
		Consumer<HavingRel> consumer) {
		return selectBehavior().createHavingCriteria(consumer);
	}

	/**
	 * このクエリに INNER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightRelationship<?>> OnClause<OnLeftRel, R, statuses> INNER_JOIN(RightTable<R> right) {
		return selectBehavior().INNER_JOIN(right, this);
	}

	/**
	 * このクエリに LEFT OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightRelationship<?>> OnClause<OnLeftRel, R, statuses> LEFT_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().LEFT_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに RIGHT OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightRelationship<?>> OnClause<OnLeftRel, R, statuses> RIGHT_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().RIGHT_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに FULL OUTER JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return ON
	 */
	public <R extends OnRightRelationship<?>> OnClause<OnLeftRel, R, statuses> FULL_OUTER_JOIN(RightTable<R> right) {
		return selectBehavior().FULL_OUTER_JOIN(right, this);
	}

	/**
	 * このクエリに CROSS JOIN で別テーブルを結合します。
	 * @param right 別クエリ
	 * @return この {@link SelectStatement}
	 */
	public <R extends OnRightRelationship<?>> statuses CROSS_JOIN(RightTable<R> right) {
		selectBehavior().CROSS_JOIN(right, this);
		return this;
	}

	/**
	 * UNION するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select UNION 対象
	 * @return この {@link SelectStatement}
	 */
	public statuses UNION(SelectStatement select) {
		selectBehavior().UNION(select);
		return this;
	}

	/**
	 * UNION ALL するクエリを追加します。<br>
	 * 追加する側のクエリには ORDER BY 句を設定することはできません。
	 * @param select UNION ALL 対象
	 * @return この {@link SelectStatement}
	 */
	public statuses UNION_ALL(SelectStatement select) {
		selectBehavior().UNION_ALL(select);
		return this;
	}

	/**
	 * ORDER BY 句を記述します。
	 * @param function
	 * @return この {@link SelectStatement}
	 */
	public statuses ORDER_BY(
		OrderByOfferFunction<OrderByRel> function) {
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
	public statuses groupBy(GroupByClause clause) {
		selectBehavior().setGroupByClause(clause);
		return this;
	}

	/**
	 * 新規に ORDER BY 句をセットします。
	 * @param clause 新 ORDER BY 句
	 * @return {@link SelectStatement} 自身
	 * @throws IllegalStateException 既に ORDER BY 句がセットされている場合
	 */
	public statuses orderBy(OrderByClause clause) {
		selectBehavior().setOrderByClause(clause);
		return this;
	}

	/**
	 * 現時点の WHERE 句に新たな条件を AND 結合します。<br>
	 * AND 結合する対象がなければ、新条件としてセットされます。
	 * @param criteria AND 結合する新条件
	 * @return {@link SelectStatement} 自身
	 */
	public statuses and(Criteria criteria) {
		selectBehavior().and(criteria);
		return this;
	}

	/**
	 * 現時点の WHERE 句に新たな条件を OR 結合します。<br>
	 * OR 結合する対象がなければ、新条件としてセットされます。
	 * @param criteria OR 結合する新条件
	 * @return {@link SelectStatement} 自身
	 */
	public statuses or(Criteria criteria) {
		selectBehavior().or(criteria);
		return this;
	}

	/**
	 * 生成された SQL 文を加工する {SQLDecorator} を設定します。
	 * @param decorators {@link SQLDecorator}
	 * @return {@link SelectStatement} 自身
	 */
	@Override
	public statuses apply(SQLDecorator... decorators) {
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
	public LogicalOperators<WhereRel> getWhereLogicalOperators() {
		return selectBehavior().whereOperators();
	}

	@Override
	public LogicalOperators<HavingRel> getHavingLogicalOperators() {
		return selectBehavior().havingOperators();
	}

	@Override
	public LogicalOperators<OnLeftRel> getOnLeftLogicalOperators() {
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
		org.blendee.support.Query.super.aggregate(consumer);
	}

	@Override
	public <T> T aggregateAndGet(Function<BResultSet, T> function) {
		selectBehavior().quitRowMode();
		return org.blendee.support.Query.super.aggregateAndGet(function);
	}

	@Override
	public ResultSetIterator aggregate() {
		selectBehavior().quitRowMode();
		return org.blendee.support.Query.super.aggregate();
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
	public statuses resetWhere() {
		selectBehavior().resetWhere();
		return this;
	}

	/**
	 * 現在保持している HAVING 句をリセットします。
	 * @return このインスタンス
	 */
	public statuses resetHaving() {
		selectBehavior().resetHaving();
		return this;
	}

	/**
	 * 現在保持している SELECT 句をリセットします。
	 * @return このインスタンス
	 */
	public statuses resetSelect() {
		selectBehavior().resetSelect();
		return this;
	}

	/**
	 * 現在保持している GROUP BY 句をリセットします。
	 * @return このインスタンス
	 */
	public statuses resetGroupBy() {
		selectBehavior().resetGroupBy();
		return this;
	}

	/**
	 * 現在保持している ORDER BY 句をリセットします。
	 * @return このインスタンス
	 */
	public statuses resetOrderBy() {
		selectBehavior().resetOrderBy();
		return this;
	}

	/**
	 * 現在保持している UNION をリセットします。
	 * @return このインスタンス
	 */
	public statuses resetUnions() {
		selectBehavior().resetUnions();
		return this;
	}

	/**
	 * 現在保持している JOIN をリセットします。
	 * @return このインスタンス
	 */
	public statuses resetJoins() {
		selectBehavior().resetJoins();
		return this;
	}

	/**
	 * 現在保持している INSERT 文のカラムをリセットします。
	 * @return このインスタンス
	 */
	public statuses resetInsert() {
		dmsBehavior().resetInsert();
		return this;
	}

	/**
	 * 現在保持している UPDATE 文の更新要素をリセットします。
	 * @return このインスタンス
	 */
	public statuses resetUpdate() {
		dmsBehavior().resetUpdate();
		return this;
	}

	/**
	 * 現在保持している SET 文の更新要素をリセットします。
	 * @return このインスタンス
	 */
	public statuses resetDelete() {
		dmsBehavior().resetDelete();
		return this;
	}

	/**
	 * 現在保持している {@link SQLDecorator} をリセットします。
	 * @return このインスタンス
	 */
	public statuses resetDecorators() {
		decorators$.clear();
		return this;
	}

	/**
	 * 現在保持している条件、並び順をリセットします。
	 * @return このインスタンス
	 */
	public statuses reset() {
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
	public OnRightRel joint() {
		return getOnRightLogicalOperators().AND;
	}

	@Override
	public SelectStatement getSelectStatement() {
		return this;
	}

	/**
	 * INSERT 文を生成します。
	 * @param function function
	 * @return {@link InsertStatementIntermediate}
	 */
	public InsertStatementIntermediate INSERT(InsertOfferFunction<InsertRel> function) {
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
	public DataManipulator INSERT(InsertOfferFunction<InsertRel> function, SelectStatement select) {
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
	public UpdateStatementIntermediate<DMSWhereRel> UPDATE(Consumer<UpdateRel> consumer) {
		return dmsBehavior().UPDATE(consumer);
	}

	/**
	 * UPDATE 文を生成します。
	 * @return {@link UpdateStatementIntermediate}
	 */
	public UpdateStatementIntermediate<DMSWhereRel> UPDATE() {
		return dmsBehavior().UPDATE();
	}

	/**
	 * DELETE 文を生成します。
	 * @return {@link DeleteStatementIntermediate}
	 */
	public final DeleteStatementIntermediate<DMSWhereRel> DELETE() {
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
	 * 自動生成された {@link TableFacadeRelationship} の実装クラスです。<br>
	 * 条件として使用できるカラムを内包しており、それらを使用して検索 SQL を生成可能にします。
	 * @param <T> 使用されるカラムのタイプにあった型
	 * @param <M> Many 一対多の多側の型連鎖
	 */
	public static class Rel<T, M> implements TableFacadeRelationship {

		private final statuses table$;

		private final CriteriaContext context$;

		private final TableFacadeRelationship parent$;

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
		 * 項目名 name
		 */
		public final T name;

		/**
		 * 項目名 revision
		 */
		public final T revision;

		/**
		 * 項目名 extension
		 */
		public final T extension;

		/**
		 * 項目名 tags
		 */
		public final T tags;

		/**
		 * 項目名 active
		 */
		public final T active;

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

		/**
		 * 直接使用しないでください。
		 * @param builder$ builder
		 * @param parent$ parent
		 * @param fkName$ fkName
		 */
		public Rel(
			TableFacadeContext<T> builder$,
			TableFacadeRelationship parent$,
			String fkName$) {
			table$ = null;
			context$ = null;
			this.parent$ = parent$;
			this.fkName$ = fkName$;

			this.id = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.id);
			this.group_id = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.group_id);
			this.name = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.name);
			this.revision = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.revision);
			this.extension = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.extension);
			this.tags = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.tags);
			this.active = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.active);
			this.created_at = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.created_at);
			this.created_by = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.created_by);
			this.updated_at = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.updated_at);
			this.updated_by = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.updated_by);

		}

		private Rel(
			statuses table$,
			TableFacadeContext<T> builder$,
			CriteriaContext context$) {
			this.table$ = table$;
			this.context$ = context$;
			parent$ = null;
			fkName$ = null;

			this.id = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.id);
			this.group_id = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.group_id);
			this.name = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.name);
			this.revision = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.revision);
			this.extension = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.extension);
			this.tags = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.tags);
			this.active = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.active);
			this.created_at = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.created_at);
			this.created_by = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.created_by);
			this.updated_at = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.updated_at);
			this.updated_by = builder$.buildColumn(
				this,
				jp.ats.blackbox.blendee.bb.statuses.updated_by);

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
			if (!(o instanceof TableFacadeRelationship)) return false;
			return getRelationship()
				.equals(((TableFacadeRelationship) o).getRelationship());
		}

		@Override
		public int hashCode() {
			return getRelationship().hashCode();
		}

		@Override
		public OneToManyRelationship getOneToManyRelationship() {
			return new OneToManyRelationship(
				parent$ == null ? null : parent$.getOneToManyRelationship(),
				Rel.this.getRelationship(),
				data -> new Row(data),
				table$ != null ? table$.id$ : parent$.getSelectStatement().getRuntimeId());
		}
	}

	/**
	 * 自動生成された {@link TableFacadeRelationship} の実装クラスです。<br>
	 * 条件として使用できるカラムと、参照しているテーブルを内包しており、それらを使用して検索 SQL を生成可能にします。
	 * @param <T> 使用されるカラムのタイプにあった型
	 * @param <M> Many 一対多の多側の型連鎖
	 */
	public static class ExtRel<T, M> extends Rel<T, M> {

		private final TableFacadeContext<T> builder$;

		/**
		 * 直接使用しないでください。
		 * @param builder$ builder
		 * @param parent$ parent
		 * @param fkName$ fkName
		 */
		public ExtRel(
			TableFacadeContext<T> builder$,
			TableFacadeRelationship parent$,
			String fkName$) {
			super(builder$, parent$, fkName$);
			this.builder$ = builder$;
		}

		private ExtRel(
			statuses table$,
			TableFacadeContext<T> builder$,
			CriteriaContext context$) {
			super(table$, builder$, context$);
			this.builder$ = builder$;
		}

		/**
		 * この {@link TableFacadeRelationship} が表すテーブルの Row を一とし、多をもつ検索結果を生成する {@link OneToManyQuery} を返します。
		 * @return {@link OneToManyQuery}
		 */
		public OneToManyQuery<Row, M> intercept() {
			if (super.table$ != null) throw new IllegalStateException("このインスタンスでは直接使用することはできません");
			if (!getSelectStatement().rowMode()) throw new IllegalStateException("集計モードでは実行できない処理です");
			return new InstantOneToManyQuery<>(this, getSelectStatement().decorators());
		}

		/**
		 * 参照先テーブル名 groups<br>
		 * 外部キー名 statuses_group_id_fkey<br>
		 * 項目名 group_id
		 * @return groups relationship
		 */
		public jp.ats.blackbox.blendee.bb.groups.ExtRel<T, Many<jp.ats.blackbox.blendee.bb.statuses.Row, M>> $groups() {
			return new jp.ats.blackbox.blendee.bb.groups.ExtRel<>(
				builder$,
				this,
				jp.ats.blackbox.blendee.bb.statuses.groups$statuses_group_id_fkey);
		}

		/**
		 * 参照先テーブル名 users<br>
		 * 外部キー名 statuses_created_by_fkey<br>
		 * 項目名 created_by
		 * @return users relationship
		 */
		public jp.ats.blackbox.blendee.bb.users.ExtRel<T, Many<jp.ats.blackbox.blendee.bb.statuses.Row, M>> $users$statuses_created_by_fkey() {
			return new jp.ats.blackbox.blendee.bb.users.ExtRel<>(
				builder$,
				this,
				jp.ats.blackbox.blendee.bb.statuses.users$statuses_created_by_fkey);
		}

		/**
		 * 参照先テーブル名 users<br>
		 * 外部キー名 statuses_updated_by_fkey<br>
		 * 項目名 updated_by
		 * @return users relationship
		 */
		public jp.ats.blackbox.blendee.bb.users.ExtRel<T, Many<jp.ats.blackbox.blendee.bb.statuses.Row, M>> $users$statuses_updated_by_fkey() {
			return new jp.ats.blackbox.blendee.bb.users.ExtRel<>(
				builder$,
				this,
				jp.ats.blackbox.blendee.bb.statuses.users$statuses_updated_by_fkey);
		}

	}

	/**
	 * SELECT 句用
	 */
	public static class SelectRel extends ExtRel<SelectCol, Void> implements SelectRelationship {

		private SelectRel(
			statuses table$,
			TableFacadeContext<SelectCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * SELECT 文 WHERE 句用
	 */
	public static class WhereRel extends ExtRel<WhereColumn<WhereLogicalOperators>, Void> implements WhereRelationship<WhereRel> {

		/**
		 * 条件接続 OR
		 */
		public final WhereRel OR;

		private WhereRel(
			statuses table$,
			TableFacadeContext<WhereColumn<WhereLogicalOperators>> builder$,
			CriteriaContext context$,
			WhereRel or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public WhereLogicalOperators paren(Consumer<WhereRel> consumer) {
			SelectStatement statement = getSelectStatement();
			Paren.execute(statement.getRuntimeId(), getContext(), consumer, this);
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
	public static class GroupByRel extends ExtRel<GroupByCol, Void> implements GroupByRelationship {

		private GroupByRel(
			statuses table$,
			TableFacadeContext<GroupByCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * HAVING 句用
	 */
	public static class HavingRel extends ExtRel<HavingColumn<HavingLogicalOperators>, Void> implements HavingRelationship<HavingRel> {

		/**
		 * 条件接続 OR
		 */
		public final HavingRel OR;

		private HavingRel(
			statuses table$,
			TableFacadeContext<HavingColumn<HavingLogicalOperators>> builder$,
			CriteriaContext context$,
			HavingRel or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public HavingLogicalOperators paren(Consumer<HavingRel> consumer) {
			SelectStatement statement = getSelectStatement();
			Paren.execute(statement.getRuntimeId(), getContext(), consumer, this);
			return (HavingLogicalOperators) statement.getHavingLogicalOperators();
		}
	}

	/**
	 * ORDER BY 句用
	 */
	public static class OrderByRel extends ExtRel<OrderByCol, Void> implements OrderByRelationship {

		private OrderByRel(
			statuses table$,
			TableFacadeContext<OrderByCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}

		@Override
		public OrderByClause getOrderByClause() {
			return getSelectStatement().getOrderByClause();
		}
	}

	/**
	 * ON 句 (LEFT) 用
	 */
	public static class OnLeftRel extends ExtRel<OnLeftColumn<OnLeftLogicalOperators>, Void> implements OnLeftRelationship<OnLeftRel> {

		/**
		 * 条件接続 OR
		 */
		public final OnLeftRel OR;

		private OnLeftRel(
			statuses table$,
			TableFacadeContext<OnLeftColumn<OnLeftLogicalOperators>> builder$,
			CriteriaContext context$,
			OnLeftRel or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public OnLeftLogicalOperators paren(Consumer<OnLeftRel> consumer) {
			SelectStatement statement = getSelectStatement();
			Paren.execute(statement.getRuntimeId(), getContext(), consumer, this);
			return (OnLeftLogicalOperators) statement.getOnLeftLogicalOperators();
		}
	}

	/**
	 * ON 句 (RIGHT) 用
	 */
	public static class OnRightRel extends Rel<OnRightColumn<OnRightLogicalOperators>, Void> implements OnRightRelationship<OnRightRel> {

		/**
		 * 条件接続 OR
		 */
		public final OnRightRel OR;

		private OnRightRel(
			statuses table$,
			TableFacadeContext<OnRightColumn<OnRightLogicalOperators>> builder$,
			CriteriaContext context$,
			OnRightRel or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public OnRightLogicalOperators paren(Consumer<OnRightRel> consumer) {
			SelectStatement statement = getSelectStatement();
			Paren.execute(statement.getRuntimeId(), getContext(), consumer, this);
			return (OnRightLogicalOperators) statement.getOnRightLogicalOperators();
		}
	}

	/**
	 * INSERT 用
	 */
	public static class InsertRel extends Rel<InsertCol, Void> implements InsertRelationship {

		private InsertRel(
			statuses table$,
			TableFacadeContext<InsertCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * UPDATE 用
	 */
	public static class UpdateRel extends Rel<UpdateCol, Void> implements UpdateRelationship {

		private UpdateRel(
			statuses table$,
			TableFacadeContext<UpdateCol> builder$) {
			super(table$, builder$, CriteriaContext.NULL);
		}
	}

	/**
	 * UPDATE, DELETE 文 WHERE 句用
	 */
	public static class DMSWhereRel extends Rel<WhereColumn<DMSWhereLogicalOperators>, Void> implements WhereRelationship<DMSWhereRel> {

		/**
		 * 条件接続 OR
		 */
		public final DMSWhereRel OR;

		private DMSWhereRel(
			statuses table$,
			TableFacadeContext<WhereColumn<DMSWhereLogicalOperators>> builder$,
			CriteriaContext context$,
			DMSWhereRel or$) {
			super(table$, builder$, context$);
			OR = or$ == null ? this : or$;
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
		public DMSWhereLogicalOperators paren(Consumer<DMSWhereRel> consumer) {
			DataManipulationStatement statement = getDataManipulationStatement();
			Paren.execute(statement.getRuntimeId(), getContext(), consumer, this);
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

		private SelectCol(TableFacadeRelationship relationship, String name) {
			super(relationship, name);
		}
	}

	/**
	 * GROUP BY 句用
	 */
	public static class GroupByCol extends GroupByColumn {

		private GroupByCol(TableFacadeRelationship relationship, String name) {
			super(relationship, name);
		}
	}

	/**
	 * ORDER BY 句用
	 */
	public static class OrderByCol extends OrderByColumn {

		private OrderByCol(TableFacadeRelationship relationship, String name) {
			super(relationship, name);
		}
	}

	/**
	 * INSERT 文用
	 */
	public static class InsertCol extends InsertColumn {

		private InsertCol(TableFacadeRelationship relationship, String name) {
			super(relationship, name);
		}
	}

	/**
	 * UPDATE 文用
	 */
	public static class UpdateCol extends UpdateColumn {

		private UpdateCol(TableFacadeRelationship relationship, String name) {
			super(relationship, name);
		}
	}

	/**
	 * Query
	 */
	public class Query implements org.blendee.support.Query<Iterator, Row> {

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
