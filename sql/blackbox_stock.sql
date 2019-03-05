--===========================
--Blackbox Stock init DDL
--===========================

/*
このDDLを実行するために必要なもの
role
	blackbox_admin Blackbox管理者 SUPERUSER権限
	blackbox Blackbox利用者 一般権限
tablespace
	blackbox
		Blackboxアプリケーション用
		owner: blackbox
	blackbox_index
		Blackbox index用
		owner: blackbox
	log
		Blackbox log用
		owner: blackbox
database
	blackbox
		Blackboxアプリケーション用、tablespaceをblackboxにする
		owner: blackbox
*/

--全削除
DROP SCHEMA IF EXISTS bb_log CASCADE; --logはblackboxに依存しているのでDROP
DROP SCHEMA IF EXISTS bb_stock CASCADE;

/*
postgresql role
blackbox_admin Blackbox管理者 SUPERUSER権限
blackbox Blackbox利用者 一般権限
以下のDDLは全てblackbox_adminで実行すること
アプリケーションはblackboxロールで実行することを想定している
外部アプリケーションで既にユーザーが存在する場合は、blackboxと同じGRANT文を実行すること
*/

CREATE SCHEMA bb_stock;

COMMENT ON SCHEMA bb_stock IS 'Blackbox Stock Schema';

/*
--postgresql tablespace
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox';
*/

--===========================
--stock tables
--===========================

--アイテム
--SKU、個品
CREATE TABLE bb_stock.items (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);
--log対象

COMMENT ON TABLE bb_stock.items IS 'アイテム
在庫管理する対象となる「もの」、SKU、個品など';
COMMENT ON COLUMN bb_stock.items.id IS 'ID';
COMMENT ON COLUMN bb_stock.items.group_id IS 'グループID';
COMMENT ON COLUMN bb_stock.items.name IS '名称';
COMMENT ON COLUMN bb_stock.items.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb_stock.items.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb_stock.items.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb_stock.items.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb_stock.items.created_at IS '作成時刻';
COMMENT ON COLUMN bb_stock.items.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb_stock.items.updated_at IS '更新時刻';
COMMENT ON COLUMN bb_stock.items.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb_stock.items (
	id,
	group_id,
	name,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'NULL',
	0,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

CREATE TABLE bb_stock.items_tags (
	id uuid REFERENCES bb_stock.items ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--所有者
--顧客、委託者
CREATE TABLE bb_stock.owners (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);
--log対象

COMMENT ON TABLE bb_stock.owners IS '所有者
アイテムの所有者';
COMMENT ON COLUMN bb_stock.owners.id IS 'ID';
COMMENT ON COLUMN bb_stock.owners.group_id IS 'グループID';
COMMENT ON COLUMN bb_stock.owners.name IS '名称';
COMMENT ON COLUMN bb_stock.owners.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb_stock.owners.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb_stock.owners.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb_stock.owners.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb_stock.owners.created_at IS '作成時刻';
COMMENT ON COLUMN bb_stock.owners.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb_stock.owners.updated_at IS '更新時刻';
COMMENT ON COLUMN bb_stock.owners.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb_stock.owners (
	id,
	group_id,
	name,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'NULL',
	0,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

CREATE TABLE bb_stock.owners_tags (
	id uuid REFERENCES bb_stock.owners ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--置き場
--棚、現場
CREATE TABLE bb_stock.locations (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);
--log対象

COMMENT ON TABLE bb_stock.locations IS '置き場
アイテムの置き場';
COMMENT ON COLUMN bb_stock.locations.id IS 'ID';
COMMENT ON COLUMN bb_stock.locations.group_id IS 'グループID';
COMMENT ON COLUMN bb_stock.locations.name IS '名称';
COMMENT ON COLUMN bb_stock.locations.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb_stock.locations.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb_stock.locations.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb_stock.locations.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb_stock.locations.created_at IS '作成時刻';
COMMENT ON COLUMN bb_stock.locations.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb_stock.locations.updated_at IS '更新時刻';
COMMENT ON COLUMN bb_stock.locations.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb_stock.locations (
	id,
	group_id,
	name,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'NULL',
	0,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

CREATE TABLE bb_stock.locations_tags (
	id uuid REFERENCES bb_stock.locations ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--状態
CREATE TABLE bb_stock.statuses (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);
--log対象

COMMENT ON TABLE bb_stock.statuses IS '状態
Blackbox内でのアイテムの状態';
COMMENT ON COLUMN bb_stock.statuses.id IS 'ID';
COMMENT ON COLUMN bb_stock.statuses.group_id IS 'グループID';
COMMENT ON COLUMN bb_stock.statuses.name IS '名称';
COMMENT ON COLUMN bb_stock.statuses.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb_stock.statuses.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb_stock.statuses.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb_stock.statuses.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb_stock.statuses.created_at IS '作成時刻';
COMMENT ON COLUMN bb_stock.statuses.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb_stock.statuses.updated_at IS '更新時刻';
COMMENT ON COLUMN bb_stock.statuses.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb_stock.statuses (
	id,
	group_id,
	name,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'NULL',
	0,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

CREATE TABLE bb_stock.statuses_tags (
	id uuid REFERENCES bb_stock.statuses ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--在庫
CREATE TABLE bb_stock.stocks (
	id uuid REFERENCES bb.units PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	item_id uuid REFERENCES bb_stock.items NOT NULL,
	owner_id uuid REFERENCES bb_stock.owners NOT NULL,
	location_id uuid REFERENCES bb_stock.locations NOT NULL,
	status_id uuid REFERENCES bb_stock.statuses NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	UNIQUE (group_id, item_id, owner_id, location_id, status_id));
--log対象外
--一度登録されたら変更されない

COMMENT ON TABLE bb_stock.stocks IS '在庫
Blackboxで数量管理する在庫の最小単位';
COMMENT ON COLUMN bb_stock.stocks.id IS 'ID
管理対象IDに従属';
COMMENT ON COLUMN bb_stock.stocks.group_id IS '管理対象に持つグループID
この在庫の属するグループ';
COMMENT ON COLUMN bb_stock.stocks.item_id IS 'アイテムID';
COMMENT ON COLUMN bb_stock.stocks.owner_id IS '所有者ID';
COMMENT ON COLUMN bb_stock.stocks.location_id IS '置き場ID';
COMMENT ON COLUMN bb_stock.stocks.status_id IS '状態ID';
COMMENT ON COLUMN bb_stock.stocks.created_at IS '作成時刻';
COMMENT ON COLUMN bb_stock.stocks.created_by IS '作成ユーザー';

--NULLの代用(id=0)
INSERT INTO bb_stock.stocks (
	id,
	group_id,
	item_id,
	owner_id,
	location_id,
	status_id,
	created_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

--===========================
--indexes
--===========================

/*
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox_index';
*/

--items
CREATE INDEX ON bb_stock.items (group_id);
CREATE INDEX ON bb_stock.items (active);

--owners
CREATE INDEX ON bb_stock.owners (group_id);
CREATE INDEX ON bb_stock.owners (active);

--locations
CREATE INDEX ON bb_stock.locations (group_id);
CREATE INDEX ON bb_stock.locations (active);

--statuses
CREATE INDEX ON bb_stock.statuses (group_id);
CREATE INDEX ON bb_stock.statuses (active);

--stocks
CREATE INDEX ON bb_stock.stocks (group_id);
CREATE INDEX ON bb_stock.stocks (item_id);
CREATE INDEX ON bb_stock.stocks (owner_id);
CREATE INDEX ON bb_stock.stocks (location_id);
CREATE INDEX ON bb_stock.stocks (status_id);

--tags
CREATE INDEX ON bb_stock.items_tags (tag_id);
CREATE INDEX ON bb_stock.owners_tags (tag_id);
CREATE INDEX ON bb_stock.locations_tags (tag_id);
CREATE INDEX ON bb_stock.statuses_tags (tag_id);

--===========================
--privileges
--===========================

--スキーマ使用権を付与
GRANT USAGE ON SCHEMA bb_stock TO blackbox;

--シーケンス使用権を付与
GRANT USAGE ON ALL SEQUENCES IN SCHEMA bb_stock TO blackbox;

--全テーブルSELECT可能
GRANT SELECT ON ALL TABLES IN SCHEMA bb_stock TO blackbox;

GRANT INSERT, UPDATE, DELETE ON TABLE
	bb_stock.items,
	bb_stock.owners,
	bb_stock.locations,
	bb_stock.statuses
TO blackbox;

--tag関連はINSERT, DELETEのみ
GRANT INSERT, DELETE ON TABLE
	bb_stock.items_tags,
	bb_stock.owners_tags,
	bb_stock.locations_tags,
	bb_stock.statuses_tags
TO blackbox;

--stocksはINSERTのみ
GRANT INSERT ON TABLE
	bb_stock.stocks
TO blackbox;