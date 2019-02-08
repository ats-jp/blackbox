--===========================
--Blackbox init DDL
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
DROP SCHEMA IF EXISTS bb CASCADE;

--UUID生成用
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

--===========================
--security tables
--===========================

/*
postgresql role
blackbox_admin Blackbox管理者 SUPERUSER権限
blackbox Blackbox利用者 一般権限
以下のDDLは全てblackbox_adminで実行すること
アプリケーションはblackboxロールで実行することを想定している
外部アプリケーションで既にユーザーが存在する場合は、blackboxと同じGRANT文を実行すること
*/

/*
共通仕様
id
	org単位でデータベース間の移行が可能なようにUUIDを使用
name
	各オブジェクトの名称
	システム的には特別な意味を持たない
revision
	楽観的排他制御のためのリビジョン番号
	更新時にデータ参照時のリビジョン番号で更新し、失敗したら既に他で更新されていると判断する
extension
	外部アプリケーションがBlackboxに保存させておくための情報をJSON形式にしたもの
	基本的にマスタテーブルが持ち、集約テーブルにはその時のマスタテーブルのextensionを合成して持つ
active
	Blackboxでは、基本的にデータの削除は発生しないが、システム的に不要になったデータを除外する必要がある場合、このフラグを使用する
	falseの場合、通常の検索からは除外される
	何をもってアクティブかを定義するのは、外部アプリケーションが行う
created_at
	データ作成時刻
created_by
	データ作成ユーザー
updated_at
	データ更新時刻
	新規登録時はデータ作成時刻と同じ値がセットされる
updated_by
	データ更新ユーザー
	新規登録時はデータ作成ユーザーと同じ値がセットされる
id='00000000-0000-0000-0000-000000000000'のデータ
	マスタでid='00000000-0000-0000-0000-000000000000'のデータは、いわゆるNULLデータとして扱う
*/

CREATE SCHEMA bb;

COMMENT ON SCHEMA bb IS 'Blackbox Main Schema';

/*
--postgresql tablespace
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox';
*/

--Blackboxインスタンス
CREATE TABLE bb.instances (
	id uuid PRIMARY KEY,
	name text NOT NULL,
	principal boolean NOT NULL,
	description text NOT NULL);

COMMENT ON TABLE bb.instances IS 'Blackbox運用インスタンス
org単位でデータを移行する際の発生元を表す';
COMMENT ON COLUMN bb.instances.id IS 'ID';
COMMENT ON COLUMN bb.instances.name IS '名称';
COMMENT ON COLUMN bb.instances.principal IS 'この実行インスタンスを表す行
一行のみtrueでなければならず、他から移設してきたインスタンスデータはfalse';

INSERT INTO bb.instances VALUES ('00000000-0000-0000-0000-000000000000', 'NULL', false, 'nullの代用、移行不可');
INSERT INTO bb.instances VALUES (
	gen_random_uuid(),
	COALESCE(current_database(), 'unknown_database') || ' [' || COALESCE(inet_server_addr()::text, 'unknown_addr') || ':' || COALESCE(inet_server_port()::text, 'unknown_port') || ']',
	true,
	'');

--組織
CREATE TABLE bb.orgs (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	name text NOT NULL,
	instance_id uuid REFERENCES bb.instances NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid NOT NULL, --あとでREFERENCES usersに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid NOT NULL); --あとでREFERENCES usersに
--システム利用者最大単位
--log対象

COMMENT ON TABLE bb.orgs IS '組織
Blackboxを使用する組織';
COMMENT ON COLUMN bb.orgs.id IS 'ID';
COMMENT ON COLUMN bb.orgs.name IS '名称';
COMMENT ON COLUMN bb.orgs.instance_id IS '発生元インスタンスのID';
COMMENT ON COLUMN bb.orgs.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.orgs.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.orgs.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb.orgs.created_at IS '作成時刻';
COMMENT ON COLUMN bb.orgs.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.orgs.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.orgs.updated_by IS '更新ユーザー';

--NULLの代用(id=00000000-0000-0000-0000-000000000000)
INSERT INTO bb.orgs (
	id,
	name,
	instance_id,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'NULL',
	'00000000-0000-0000-0000-000000000000',
	0,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

--システム用
INSERT INTO bb.orgs (
	id,
	name,
	instance_id,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (
	'11111111-1111-1111-1111-111111111111',
	'Blackbox',
	'00000000-0000-0000-0000-000000000000',
	0,
	'{}',
	'11111111-1111-1111-1111-111111111111',
	'11111111-1111-1111-1111-111111111111');

----------

CREATE TABLE bb.tags (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	tag text UNIQUE NOT NULL);

--グループ
CREATE TABLE bb.groups (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	org_id uuid REFERENCES bb.orgs NOT NULL,
	name text NOT NULL,
	parent_id uuid REFERENCES bb.groups NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid NOT NULL, --あとでREFERENCES userに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid NOT NULL); --あとでREFERENCES userに
--組織配下の中でのまとまり
--権限コントロールのためのテーブル
--全てのオブジェクトは何らかのgroupに属するように
--グループの定義、運用はBlackboxの外部アプリケーションが行う
--log対象

COMMENT ON TABLE bb.groups IS 'グループ
組織配下の中でのまとまり';
COMMENT ON COLUMN bb.groups.id IS 'ID';
COMMENT ON COLUMN bb.groups.org_id IS '組織ID';
COMMENT ON COLUMN bb.groups.name IS '名称';
COMMENT ON COLUMN bb.groups.parent_id IS '親グループID';
COMMENT ON COLUMN bb.groups.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.groups.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.groups.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb.groups.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb.groups.created_at IS '作成時刻';
COMMENT ON COLUMN bb.groups.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.groups.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.groups.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.groups (
	id,
	org_id,
	name,
	parent_id,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'NULL',
	'00000000-0000-0000-0000-000000000000',
	0,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

--システム用
INSERT INTO bb.groups (
	id,
	org_id,
	name,
	parent_id,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (
	'11111111-1111-1111-1111-111111111111',
	'11111111-1111-1111-1111-111111111111',
	'Superusers',
	'00000000-0000-0000-0000-000000000000',
	0,
	'{}',
	'11111111-1111-1111-1111-111111111111',
	'11111111-1111-1111-1111-111111111111');

CREATE TABLE bb.groups_tags (
	id uuid REFERENCES bb.groups ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--グループ親子関係
CREATE TABLE bb.relationships (
	id bigserial PRIMARY KEY,
	parent_id uuid REFERENCES bb.groups ON DELETE CASCADE NOT NULL,
	child_id uuid REFERENCES bb.groups ON DELETE CASCADE NOT NULL,
	cascade_id bigint NOT NULL, --あとでREFERENCES relationshipsに
	UNIQUE (parent_id, child_id));
--log対象外
--groupsから復元できるデータであるが、更新頻度は高くないと思われるのでWAL対象
--groupsから再生できるため、IDはbigserial
--親IDが指定されたら子も対象とするための補助テーブル
--groupの親子関係を物理的に展開し、検索を高速化することが目的
--子グループから辿れるすべての親(0を除く)と自分自身にこのエントリを作成する

/*
例)
親 <- 子 <- 孫という関係性の場合
	(親, 親)
	(親, 子)
	(親, 孫)
	(子, 子)
	(子, 孫)
	(孫, 孫)
が必要となる

cascade_idの用途は、世代が離れた連携を削除するために、一つ上のrelationshipのIDをもつ
例)
親 <- 子 <- 孫 <- 曾孫という関係性の場合
親を登録
	(1, 親, 親, 0)
子を登録
	(2, 子, 子, 0)
	(3, 親, 子, 2)
孫を登録
	(4, 孫, 孫, 0)
	(5, 子, 孫, 4)
	(6, 親, 孫, 5)
曾孫を登録
	(7, 曾孫, 曾孫, 0)
	(8, 孫, 曾孫, 7)
	(9, 子, 曾孫, 8)
	(10, 親, 曾孫, 9)
親 <- 子の連携を変更する場合、一旦子の関係する連携を削除し、その後再度子以下の連携を登録することで実現する
削除のフェーズにおいて、relationshipsのid、parent_idに子のIDを持つ全行を削除すると
2, 3, 5, 9が削除される
5が削除された際、カスケードで6も削除
9が削除された際、カスケードで10も削除される
	(1, 親, 親, 0)
	(4, 孫, 孫, 0)
	(7, 曾孫, 曾孫, 0)
	(8, 孫, 曾孫, 0)
が残る
*/

COMMENT ON TABLE bb.relationships IS 'グループ親子関係
親IDが指定されたら子も対象とするための補助テーブル';
COMMENT ON COLUMN bb.relationships.id IS 'ID';
COMMENT ON COLUMN bb.relationships.parent_id IS '親グループID';
COMMENT ON COLUMN bb.relationships.child_id IS '子グループID
親グループIDに対して、親自身と親から辿れるすべての子が登録されている';
COMMENT ON COLUMN bb.relationships.cascade_id IS 'カスケード削除用ID
自身が依存する親のIDを持っておき、親が削除されたときに連鎖的にすべて削除するための項目';

--NULLの代用(id=0)
INSERT INTO bb.relationships (
	id,
	parent_id,
	child_id,
	cascade_id
) VALUES (
	0,
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	0);

ALTER TABLE bb.relationships ADD CONSTRAINT relationships_cascade_id_fkey FOREIGN KEY (cascade_id) REFERENCES bb.relationships ON DELETE CASCADE;

----------

--ユーザー
CREATE TABLE bb.users (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	role smallint CHECK (role IN (0, 1, 2, 3, 9)) NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid NOT NULL, --あとでREFERENCES usersに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid NOT NULL); --あとでREFERENCES usersに
--log対象

COMMENT ON TABLE bb.users IS 'ユーザー
Blackboxの操作者';
COMMENT ON COLUMN bb.users.id IS 'ID';
COMMENT ON COLUMN bb.users.group_id IS 'グループID';
COMMENT ON COLUMN bb.users.name IS '名称';
COMMENT ON COLUMN bb.users.role IS '役割
0=SYSTEM_ADMIN, 1=ORG_ADMIN, 2=GROUP_ADMIN, 3=USER, 9=NONE';
COMMENT ON COLUMN bb.users.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.users.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.users.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb.users.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb.users.created_at IS '作成時刻';
COMMENT ON COLUMN bb.users.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.users.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.users.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.users (
	id,
	group_id,
	name,
	role,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'NULL',
	9,
	0,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

--Superuser
INSERT INTO bb.users (
	id,
	group_id,
	name,
	role,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (
	'11111111-1111-1111-1111-111111111111',
	'11111111-1111-1111-1111-111111111111',
	'superuser',
	0,
	0,
	'{}',
	'11111111-1111-1111-1111-111111111111',
	'11111111-1111-1111-1111-111111111111');

ALTER TABLE bb.orgs ADD FOREIGN KEY (created_by) REFERENCES bb.users;
ALTER TABLE bb.orgs ADD FOREIGN KEY (updated_by) REFERENCES bb.users;
ALTER TABLE bb.groups ADD FOREIGN KEY (created_by) REFERENCES bb.users;
ALTER TABLE bb.groups ADD FOREIGN KEY (updated_by) REFERENCES bb.users;
ALTER TABLE bb.users ADD FOREIGN KEY (created_by) REFERENCES bb.users;
ALTER TABLE bb.users ADD FOREIGN KEY (updated_by) REFERENCES bb.users;

CREATE TABLE bb.users_tags (
	id uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--ロック中グループ
CREATE UNLOGGED TABLE bb.locking_groups (
	id uuid PRIMARY KEY REFERENCES bb.groups ON DELETE CASCADE,
	cascade_id uuid NOT NULL, --あとでREFERENCES locking_groupsに
	user_id uuid REFERENCES bb.users NOT NULL,
	locking_transaction_id uuid NOT NULL,
	locked_at timestamptz DEFAULT now() NOT NULL);
--log対象外
--WAL対象外
--ロックのたびにグループの親子関係を展開してINSERTし、解放時はROLLBACKかDELETEで行う
--PKの一意制約を利用して他でロック中であればINSERTできないことで排他を行う

COMMENT ON TABLE bb.locking_groups IS 'ロック中グループ';
COMMENT ON COLUMN bb.locking_groups.id IS 'グループID
ロック中のグループを表す';
COMMENT ON COLUMN bb.locking_groups.cascade_id IS 'カスケード削除用ID
登録更新処理の起点となったgroupのIDとなる';
COMMENT ON COLUMN bb.locking_groups.user_id IS 'ユーザーID
ロックを行っているユーザーを表す';
COMMENT ON COLUMN bb.locking_groups.locking_transaction_id IS 'ロック処理ID
ロック処理を一意で表すID
循環を検出する際に使用する';
COMMENT ON COLUMN bb.locking_groups.locked_at IS 'ロック開始時刻';

ALTER TABLE bb.locking_groups ADD CONSTRAINT locking_groups_cascade_id_fkey FOREIGN KEY (cascade_id) REFERENCES bb.locking_groups ON DELETE CASCADE;

--===========================
--master tables
--===========================

--もの
CREATE TABLE bb.items (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);
--log対象

COMMENT ON TABLE bb.items IS 'アイテム
在庫管理する対象となる「もの」';
COMMENT ON COLUMN bb.items.id IS 'ID';
COMMENT ON COLUMN bb.items.group_id IS 'グループID';
COMMENT ON COLUMN bb.items.name IS '名称';
COMMENT ON COLUMN bb.items.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.items.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.items.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb.items.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb.items.created_at IS '作成時刻';
COMMENT ON COLUMN bb.items.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.items.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.items.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.items (
	id,
	group_id,
	name,
	revision,
	extension,
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

CREATE TABLE bb.items_tags (
	id uuid REFERENCES bb.items ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--所有者
CREATE TABLE bb.owners (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);
--log対象

COMMENT ON TABLE bb.owners IS '所有者
アイテムの所有者';
COMMENT ON COLUMN bb.owners.id IS 'ID';
COMMENT ON COLUMN bb.owners.group_id IS 'グループID';
COMMENT ON COLUMN bb.owners.name IS '名称';
COMMENT ON COLUMN bb.owners.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.owners.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.owners.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb.owners.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb.owners.created_at IS '作成時刻';
COMMENT ON COLUMN bb.owners.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.owners.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.owners.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.owners (
	id,
	group_id,
	name,
	revision,
	extension,
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

CREATE TABLE bb.owners_tags (
	id uuid REFERENCES bb.owners ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--置き場
CREATE TABLE bb.locations (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);
--log対象

COMMENT ON TABLE bb.locations IS '置き場
アイテムの置き場';
COMMENT ON COLUMN bb.locations.id IS 'ID';
COMMENT ON COLUMN bb.locations.group_id IS 'グループID';
COMMENT ON COLUMN bb.locations.name IS '名称';
COMMENT ON COLUMN bb.locations.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.locations.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.locations.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb.locations.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb.locations.created_at IS '作成時刻';
COMMENT ON COLUMN bb.locations.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.locations.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.locations.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.locations (
	id,
	group_id,
	name,
	revision,
	extension,
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

CREATE TABLE bb.locations_tags (
	id uuid REFERENCES bb.locations ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--状態
CREATE TABLE bb.statuses (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);
--log対象

COMMENT ON TABLE bb.statuses IS '状態
Blackbox内でのアイテムの状態';
COMMENT ON COLUMN bb.statuses.id IS 'ID';
COMMENT ON COLUMN bb.statuses.group_id IS 'グループID';
COMMENT ON COLUMN bb.statuses.name IS '名称';
COMMENT ON COLUMN bb.statuses.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.statuses.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.statuses.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb.statuses.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb.statuses.created_at IS '作成時刻';
COMMENT ON COLUMN bb.statuses.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.statuses.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.statuses.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.statuses (
	id,
	group_id,
	name,
	revision,
	extension,
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

CREATE TABLE bb.statuses_tags (
	id uuid REFERENCES bb.statuses ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--締め
CREATE TABLE bb.closings (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	closed_at timestamptz NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL);
--更新不可

COMMENT ON TABLE bb.closings IS '締め';
COMMENT ON COLUMN bb.closings.id IS 'ID';
COMMENT ON COLUMN bb.closings.group_id IS 'グループID';
COMMENT ON COLUMN bb.closings.closed_at IS '締め時刻';
COMMENT ON COLUMN bb.closings.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.closings.created_at IS '作成時刻';
COMMENT ON COLUMN bb.closings.created_by IS '作成ユーザー';

--締め済グループ
CREATE UNLOGGED TABLE bb.last_closings (
	id uuid PRIMARY KEY REFERENCES bb.groups ON DELETE CASCADE,
	closing_id uuid REFERENCES bb.closings ON DELETE CASCADE NOT NULL,
	closed_at timestamptz NOT NULL);
--log対象外
--WAL対象外
--締め済のグループを、親子関係を展開して登録し、締済みかどうかを高速に判定できるようにする
--先に子が締めを行い、その後親で締めを行った場合、親側で上書きするためclosing_idは親側に変更する

COMMENT ON TABLE bb.last_closings IS 'グループ最終締め情報';
COMMENT ON COLUMN bb.last_closings.id IS 'ID
グループIDに従属';
COMMENT ON COLUMN bb.last_closings.closing_id IS '締めID';
COMMENT ON COLUMN bb.last_closings.closed_at IS '締め時刻';

----------

--在庫
CREATE TABLE bb.stocks (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	item_id uuid REFERENCES bb.items NOT NULL,
	owner_id uuid REFERENCES bb.owners NOT NULL,
	location_id uuid REFERENCES bb.locations NOT NULL,
	status_id uuid REFERENCES bb.statuses NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	UNIQUE (group_id, item_id, owner_id, location_id, status_id));
--log対象外
--一度登録されたら変更されない

COMMENT ON TABLE bb.stocks IS '在庫
Blackboxで数量管理する在庫の最小単位';
COMMENT ON COLUMN bb.stocks.id IS 'ID';
COMMENT ON COLUMN bb.stocks.group_id IS 'グループID
この在庫の属するグループ';
COMMENT ON COLUMN bb.stocks.item_id IS 'アイテムID';
COMMENT ON COLUMN bb.stocks.owner_id IS '所有者ID';
COMMENT ON COLUMN bb.stocks.location_id IS '置き場ID';
COMMENT ON COLUMN bb.stocks.status_id IS '状態ID';
COMMENT ON COLUMN bb.stocks.created_at IS '作成時刻';
COMMENT ON COLUMN bb.stocks.created_by IS '作成ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.stocks (
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
--transfer tables
--===========================

--移動伝票
CREATE TABLE bb.transfers (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	transferred_at timestamptz NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	instance_id uuid REFERENCES bb.instances NOT NULL,
	denied_id uuid REFERENCES bb.transfers DEFAULT '00000000-0000-0000-0000-000000000000' NOT NULL,
	deny_reason text DEFAULT '' NOT NULL,
	org_extension jsonb NOT NULL,
	group_extension jsonb NOT NULL,
	user_extension jsonb NOT NULL,
	created_at timestamptz DEFAULT transaction_timestamp() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	UNIQUE (group_id, created_at)); 
--log対象外
--created_atをUNIQUEにするために一件毎にcommitすること
--順序を一意付けするためにcreated_atをUNIQUE化、他DBから移行してきたtransferのcreated_atと重複しないようにgroup_idも含める
--groupは単一instance内でのみ動かすのでcreated_atが重複することはないはず
--transaction_timestamp()はnow()と同等だが、仕様として何を必要としているかを明確にするために使用している

COMMENT ON TABLE bb.transfers IS '移動伝票';
COMMENT ON COLUMN bb.transfers.id IS 'ID';
COMMENT ON COLUMN bb.transfers.group_id IS 'グループID
この伝票の属するグループ';
COMMENT ON COLUMN bb.transfers.transferred_at IS '移動時刻';
COMMENT ON COLUMN bb.transfers.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.transfers.tags IS '保存用タグ';
COMMENT ON COLUMN bb.transfers.instance_id IS '発生元インスタンスのID';
COMMENT ON COLUMN bb.transfers.denied_id IS '取消元伝票ID
訂正後の伝票が訂正前の伝票のIDを持つ
ここに入っているIDが指す伝票は、取り消されたものとなる';
COMMENT ON COLUMN bb.transfers.deny_reason IS '取消理由';
COMMENT ON COLUMN bb.transfers.org_extension IS '組織のextension';
COMMENT ON COLUMN bb.transfers.group_extension IS 'グループのextension';
COMMENT ON COLUMN bb.transfers.user_extension IS '作成ユーザーのextension';
COMMENT ON COLUMN bb.transfers.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transfers.created_by IS '作成ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.transfers (
	id,
	group_id,
	transferred_at,
	extension,
	instance_id,
	org_extension,
	group_extension,
	user_extension,
	created_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'1900-1-1'::timestamptz,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'{}',
	'{}',
	'{}',
	'00000000-0000-0000-0000-000000000000');

--締め済グループチェック
CREATE FUNCTION bb.closed_check() RETURNS TRIGGER AS $$
	DECLARE closed_at_local timestamptz;
	BEGIN
		SELECT INTO closed_at_local closed_at FROM bb.last_closings WHERE id = NEW.group_id;
		IF closed_at_local IS NOT NULL AND NEW.transferred_at < closed_at_local THEN
			RAISE EXCEPTION 'closed_check(): group id=[%] closed at %', NEW.group_id, closed_at_local;
		END IF;
		RETURN NEW;
	END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER closed_checktrigger BEFORE INSERT ON bb.transfers
FOR EACH ROW EXECUTE PROCEDURE bb.closed_check();

CREATE TABLE bb.transfers_tags (
	id uuid REFERENCES bb.transfers ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--移動伝票明細
CREATE TABLE bb.bundles (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	transfer_id uuid REFERENCES bb.transfers NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL);
--log対象外

COMMENT ON TABLE bb.bundles IS '移動伝票明細
移動の単体
移動伝票とノードの関連付けテーブル
出庫ノードと入庫ノードを束ねる';
COMMENT ON COLUMN bb.bundles.id IS 'ID';
COMMENT ON COLUMN bb.bundles.transfer_id IS '移動伝票ID';
COMMENT ON COLUMN bb.bundles.extension IS '外部アプリケーション情報JSON';

--NULLの代用(id=0)
INSERT INTO bb.bundles (
	id,
	transfer_id
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

----------

--移動ノード
CREATE TABLE bb.nodes (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	bundle_id uuid REFERENCES bb.bundles NOT NULL,
	stock_id uuid REFERENCES bb.stocks NOT NULL,
	in_out "char" CHECK (in_out IN ('I', 'O')) NOT NULL,
	seq integer NOT NULL,
	quantity numeric CHECK (quantity >= 0) NOT NULL,
	grants_unlimited boolean DEFAULT false NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	group_extension jsonb DEFAULT '{}' NOT NULL,
	item_extension jsonb DEFAULT '{}' NOT NULL,
	owner_extension jsonb DEFAULT '{}' NOT NULL,
	location_extension jsonb DEFAULT '{}' NOT NULL,
	status_extension jsonb DEFAULT '{}' NOT NULL);
--移動伝票明細片側
--log対象外

COMMENT ON TABLE bb.nodes IS '移動ノード
一移動の中の入庫もしくは出庫を表す';
COMMENT ON COLUMN bb.nodes.id IS 'ID';
COMMENT ON COLUMN bb.nodes.bundle_id IS '移動ID';
COMMENT ON COLUMN bb.nodes.stock_id IS '在庫ID';
COMMENT ON COLUMN bb.nodes.in_out IS '入出庫区分';
COMMENT ON COLUMN bb.nodes.quantity IS '移動数量';
COMMENT ON COLUMN bb.nodes.grants_unlimited IS '数量無制限の許可
trueの場合、以降のsnapshotは数量がマイナスになってもエラーにならない';
COMMENT ON COLUMN bb.nodes.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.nodes.group_extension IS 'グループのextension';
COMMENT ON COLUMN bb.nodes.item_extension IS 'アイテムのextension';
COMMENT ON COLUMN bb.nodes.owner_extension IS '所有者のextension';
COMMENT ON COLUMN bb.nodes.location_extension IS '置き場のextension';
COMMENT ON COLUMN bb.nodes.status_extension IS '状態のextension';

--transfer系のテーブルはINSERTのみなので、autovacuumは行わない場合は実行
--ただし、ANALYZEがかからなくなるので、定期的に実施する必要がある
--ALTER TABLE bb.transfers SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
--ALTER TABLE bb.bundles SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
--ALTER TABLE bb.stocks SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
--ALTER TABLE bb.nodes SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);

--NULLの代用(id=0)
INSERT INTO bb.nodes (
	id,
	bundle_id,
	stock_id,
	in_out,
	seq,
	quantity
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'I',
	0,
	0);

----------

--移動ノード状態
CREATE UNLOGGED TABLE bb.snapshots (
	id uuid PRIMARY KEY REFERENCES bb.nodes,
	unlimited boolean NOT NULL,
	in_search_scope boolean DEFAULT true NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	transfer_group_id uuid REFERENCES bb.groups NOT NULL,
	stock_id uuid REFERENCES bb.stocks NOT NULL,
	transferred_at timestamptz NOT NULL,
	created_at timestamptz DEFAULT transaction_timestamp() NOT NULL, 
	node_seq integer NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL);
--log対象外
--WAL対象外のため、クラッシュ時transfersから復元する必要あり
--頻繁に参照、更新されることが予想されるので締め済のデータは削除する
--transaction_timestamp()はnow()と同等だが、仕様として何を必要としているかを明確にするために使用している
--トランザクション開始時刻を返すのでtransfers.created_atと同一となるため、transfers.created_atを参照する必要がなくなる

COMMENT ON TABLE bb.snapshots IS '移動ノード状態
transferred_at時点でのstockの状態';
COMMENT ON COLUMN bb.snapshots.id IS 'ID
nodes.node_idに従属';
COMMENT ON COLUMN bb.snapshots.unlimited IS '在庫無制限
trueの場合、totalがマイナスでもエラーとならない';
COMMENT ON COLUMN bb.snapshots.in_search_scope IS '在庫数量検索対象
snapshotの検索対象を少なくすることで直近数量の取得検索を高速化する
締められた場合、締め時刻以下の最新のsnapshotを起点に直前の在庫数を取得するので、それ以前のsnapshotはfalseとなる';
COMMENT ON COLUMN bb.snapshots.total IS 'この時点の在庫総数';
COMMENT ON COLUMN bb.snapshots.transfer_group_id IS '移動伝票のグループID
検索高速化のためtransfers.group_idをここに持つ';
COMMENT ON COLUMN bb.snapshots.stock_id IS '在庫ID
検索高速化のためnodes.stock_idをここに持つ';
COMMENT ON COLUMN bb.snapshots.transferred_at IS '移動時刻
検索高速化のためtransfers.transferred_atをここに持つ';
COMMENT ON COLUMN bb.snapshots.created_at IS '登録時刻
検索高速化のためtransfers.created_atをここに持つ';
COMMENT ON COLUMN bb.snapshots.node_seq IS '移動ノードの登録順
検索高速化のためnodes.seqをここに持つ';
COMMENT ON COLUMN bb.snapshots.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.snapshots.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.snapshots (
	id,
	unlimited,
	in_search_scope,
	total,
	transfer_group_id,
	stock_id,
	transferred_at,
	node_seq,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	false,
	false,
	0,
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'1900-1-1'::timestamptz,
	0,
	'00000000-0000-0000-0000-000000000000');

----------

--現在在庫
CREATE UNLOGGED TABLE bb.current_stocks (
	id uuid PRIMARY KEY REFERENCES bb.stocks, --stockは削除されないのでCASCADEなし
	unlimited boolean NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	snapshot_id uuid REFERENCES bb.snapshots NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL);
--log対象外
--WAL対象外のため、クラッシュ時transfersから復元する必要あり
--totalの更新は常にポーリング処理から行われるためupdated_byを持たない

COMMENT ON TABLE bb.current_stocks IS '現在在庫
在庫の現在数を保持';
COMMENT ON COLUMN bb.current_stocks.id IS 'ID
stocks.stock_idに従属';
COMMENT ON COLUMN bb.current_stocks.unlimited IS '在庫無制限
trueの場合、totalがマイナスでもエラーとならない';
COMMENT ON COLUMN bb.current_stocks.total IS '現時点の在庫総数';
COMMENT ON COLUMN bb.current_stocks.snapshot_id IS 'スナップショットID
現時点の数量を変更した伝票';
COMMENT ON COLUMN bb.current_stocks.updated_at IS '更新時刻';

----------

--締め在庫
CREATE TABLE bb.closed_stocks (
	id uuid PRIMARY KEY REFERENCES bb.stocks, --stockは削除されないのでCASCADEなし
	closing_id uuid REFERENCES bb.closings ON DELETE CASCADE NOT NULL,
	unlimited boolean NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL);
--log対象外
--締め完了後の在庫数を保持
--クラッシュ時、ここからsnapshotsとcurrent_stocksを復元する

COMMENT ON TABLE bb.closed_stocks IS '締め在庫';
COMMENT ON COLUMN bb.closed_stocks.id IS 'ID
在庫IDに従属';
COMMENT ON COLUMN bb.closed_stocks.closing_id IS '締めID';
COMMENT ON COLUMN bb.closed_stocks.unlimited IS '在庫無制限
trueの場合、totalがマイナスでもエラーとならない';
COMMENT ON COLUMN bb.closed_stocks.total IS '締め後の在庫総数';
COMMENT ON COLUMN bb.closed_stocks.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.closed_stocks.updated_by IS '更新ユーザー';

--現在在庫数量反映ジョブ
CREATE TABLE bb.jobs (
	id uuid PRIMARY KEY REFERENCES bb.transfers,
	completed boolean DEFAULT false NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL);
--transfer毎に作成
--transfers.transferred_atに実行
--currentの更新は必ず実行するためactive=falseによる無効が行えないようにactiveは無し
--completedの更新は常にポーリング処理から行われるためupdated_byを持たない

COMMENT ON TABLE bb.jobs IS '現在在庫数量反映ジョブ';
COMMENT ON COLUMN bb.jobs.id IS 'ID
transfers.transfers_idに従属';
COMMENT ON COLUMN bb.jobs.completed IS '実施済フラグ';
COMMENT ON COLUMN bb.jobs.updated_at IS '更新時刻';

----------

--transfer登録時に発生したエラー
CREATE TABLE bb.transfer_errors (
	transfer_id uuid NOT NULL,
	message text NOT NULL,
	stack_trace text NOT NULL,
	sql_state text NOT NULL,
	user_id uuid REFERENCES bb.users NOT NULL,
	request jsonb DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL);

COMMENT ON TABLE bb.transfer_errors IS 'transfer登録時に発生したエラー';
COMMENT ON COLUMN bb.transfer_errors.transfer_id IS 'transferに使用される予定だったID';
COMMENT ON COLUMN bb.transfer_errors.message IS 'エラーメッセージ';
COMMENT ON COLUMN bb.transfer_errors.stack_trace IS 'スタックトレース';
COMMENT ON COLUMN bb.transfer_errors.sql_state IS 'DBエラーコード';
COMMENT ON COLUMN bb.transfer_errors.user_id IS '登録ユーザー';
COMMENT ON COLUMN bb.transfer_errors.request IS '登録リクエスト内容
取消処理の場合、{}';
COMMENT ON COLUMN bb.transfer_errors.created_at IS '登録時刻';

--===========================
--transient tables
--===========================

--一時作業
CREATE TABLE bb.transients (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	user_id uuid REFERENCES bb.users NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);

COMMENT ON TABLE bb.transients IS '一時作業';
COMMENT ON COLUMN bb.transients.id IS 'ID';
COMMENT ON COLUMN bb.transients.group_id IS 'この一時作業のオーナーグループ
0の場合、オーナーグループはいない';
COMMENT ON COLUMN bb.transients.user_id IS 'この一時作業のオーナーユーザー
0の場合、オーナーユーザーはいない';
COMMENT ON COLUMN bb.transients.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.transients.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transients.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transients.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transients.updated_by IS '更新ユーザー';

CREATE TABLE bb.transients_tags (
	id uuid REFERENCES bb.transients ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--一時作業移動伝票
CREATE TABLE bb.transient_transfers (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	transient_id uuid REFERENCES bb.transients NOT NULL,
	group_id uuid REFERENCES bb.groups NOT NULL,
	transferred_at timestamptz NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	completed boolean DEFAULT false NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);

COMMENT ON TABLE bb.transient_transfers IS '一時作業移動伝票';
COMMENT ON COLUMN bb.transient_transfers.id IS 'ID';
COMMENT ON COLUMN bb.transient_transfers.transient_id IS '一時作業ID';
COMMENT ON COLUMN bb.transient_transfers.group_id IS 'グループID';
COMMENT ON COLUMN bb.transient_transfers.transferred_at IS '移動時刻';
COMMENT ON COLUMN bb.transient_transfers.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.transient_transfers.tags IS '保存用タグ';
COMMENT ON COLUMN bb.transient_transfers.completed IS '実施済フラグ';
COMMENT ON COLUMN bb.transient_transfers.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.transient_transfers.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_transfers.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transient_transfers.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transient_transfers.updated_by IS '更新ユーザー';

--締め済グループチェック
CREATE FUNCTION bb.transient_closed_check() RETURNS TRIGGER AS $$
	DECLARE closed_at_local timestamptz;
	BEGIN
		SELECT INTO closed_at_local closed_at FROM bb.last_closings WHERE id = NEW.group_id;
		IF closed_at_local IS NOT NULL AND NEW.transferred_at < closed_at_local THEN
			RAISE EXCEPTION 'closed_check(): group id=[%] closed at %', NEW.group_id, closed_at_local;
		END IF;
		RETURN NEW;
	END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER transient_closed_checktrigger BEFORE INSERT ON bb.transient_transfers
FOR EACH ROW EXECUTE PROCEDURE bb.transient_closed_check();

CREATE TABLE bb.transient_transfers_tags (
	id uuid REFERENCES bb.transient_transfers ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--一時作業移動伝票明細
CREATE TABLE bb.transient_bundles (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	transient_transfer_id uuid REFERENCES bb.transient_transfers NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,-- 編集でtransient_bundlesだけ追加することもあるので必要
	created_by uuid REFERENCES bb.users NOT NULL);

COMMENT ON TABLE bb.transient_bundles IS '一時作業移動伝票明細';
COMMENT ON COLUMN bb.transient_bundles.id IS 'ID';
COMMENT ON COLUMN bb.transient_bundles.transient_transfer_id IS '一時作業移動伝票ID';
COMMENT ON COLUMN bb.transient_bundles.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.transient_bundles.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.transient_bundles.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_bundles.created_by IS '作成ユーザー';

----------

--一時作業移動ノード
CREATE TABLE bb.transient_nodes (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	transient_bundle_id uuid REFERENCES bb.transient_bundles NOT NULL,
	stock_id uuid REFERENCES bb.stocks NOT NULL,
	in_out "char" CHECK (in_out IN ('I', 'O')) NOT NULL,
	quantity numeric CHECK (quantity >= 0) NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);

COMMENT ON TABLE bb.transient_nodes IS '一時作業移動ノード';
COMMENT ON COLUMN bb.transient_nodes.id IS 'ID';
COMMENT ON COLUMN bb.transient_nodes.transient_bundle_id IS '移動ID';
COMMENT ON COLUMN bb.transient_nodes.stock_id IS '在庫ID';
COMMENT ON COLUMN bb.transient_nodes.in_out IS '入出庫区分';
COMMENT ON COLUMN bb.transient_nodes.quantity IS '移動数量';
COMMENT ON COLUMN bb.transient_nodes.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.transient_nodes.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.transient_nodes.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_nodes.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transient_nodes.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transient_nodes.updated_by IS '更新ユーザー';

----------

--一時作業移動ノード状態
CREATE TABLE bb.transient_snapshots (
	id uuid PRIMARY KEY REFERENCES bb.transient_nodes,
	unlimited boolean NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);

COMMENT ON TABLE bb.transient_snapshots IS '一時作業移動ノード状態';
COMMENT ON COLUMN bb.transient_snapshots.id IS 'ID';
COMMENT ON COLUMN bb.transient_snapshots.unlimited IS '在庫無制限
trueの場合、totalがマイナスでもエラーとならない';
COMMENT ON COLUMN bb.transient_snapshots.total IS 'この時点の在庫総数';
COMMENT ON COLUMN bb.transient_snapshots.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_snapshots.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transient_snapshots.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transient_snapshots.updated_by IS '更新ユーザー';

----------

--一時作業現在在庫
CREATE TABLE bb.transient_current_stocks (
	id uuid PRIMARY KEY REFERENCES bb.stocks, --先にstocksにデータを作成してからこのテーブルにデータ作成
	transient_id uuid REFERENCES bb.transients NOT NULL,
	unlimited boolean NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL);

COMMENT ON TABLE bb.transient_current_stocks IS '一時作業現在在庫';
COMMENT ON COLUMN bb.transient_current_stocks.id IS 'ID
stocks.stock_idに従属';
COMMENT ON COLUMN bb.transient_current_stocks.transient_id IS '一時作業ID';
COMMENT ON COLUMN bb.transient_current_stocks.unlimited IS '在庫無制限
trueの場合、totalがマイナスでもエラーとならない';
COMMENT ON COLUMN bb.transient_current_stocks.total IS '現時点の在庫総数';
COMMENT ON COLUMN bb.transient_current_stocks.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_current_stocks.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transient_current_stocks.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transient_current_stocks.updated_by IS '更新ユーザー';

--===========================
--indexes
--===========================

/*
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox_index';
*/

--orgs
CREATE INDEX ON bb.orgs (active);

--groups
CREATE INDEX ON bb.groups (org_id);
CREATE INDEX ON bb.groups (active);
--parent_idで検索することはないのでindex不要

--relationships
CREATE INDEX ON bb.relationships (parent_id);

--users
CREATE INDEX ON bb.users (group_id);
CREATE INDEX ON bb.users (role);
CREATE INDEX ON bb.users (active);

--items
CREATE INDEX ON bb.items (group_id);
CREATE INDEX ON bb.items (active);

--owners
CREATE INDEX ON bb.owners (group_id);
CREATE INDEX ON bb.owners (active);

--locations
CREATE INDEX ON bb.locations (group_id);
CREATE INDEX ON bb.locations (active);

--statuses
CREATE INDEX ON bb.statuses (group_id);
CREATE INDEX ON bb.statuses (active);

--stocks
CREATE INDEX ON bb.stocks (group_id);
CREATE INDEX ON bb.stocks (item_id);
CREATE INDEX ON bb.stocks (owner_id);
CREATE INDEX ON bb.stocks (location_id);
CREATE INDEX ON bb.stocks (status_id);

--current_stocks

--transfers
CREATE INDEX ON bb.transfers (group_id);
CREATE INDEX ON bb.transfers (transferred_at);
CREATE INDEX ON bb.transfers (created_at);

--bundles
CREATE INDEX ON bb.bundles (transfer_id);

--nodes
CREATE INDEX ON bb.nodes (bundle_id);
CREATE INDEX ON bb.nodes (stock_id);

--snapshots
CREATE INDEX ON bb.snapshots (in_search_scope);
CREATE INDEX ON bb.snapshots (stock_id);
CREATE INDEX ON bb.snapshots (transferred_at);

--jobs
CREATE INDEX ON bb.jobs (completed);
--worker_idで検索することはないのでindex不要

--transients
CREATE INDEX ON bb.transients (group_id);
CREATE INDEX ON bb.transients (user_id);

--transient_current_stocks
CREATE INDEX ON bb.transient_current_stocks (transient_id);

--transient_transfers
CREATE INDEX ON bb.transient_transfers (transient_id);
CREATE INDEX ON bb.transient_transfers (group_id);
CREATE INDEX ON bb.transient_transfers (transferred_at);
CREATE INDEX ON bb.transient_transfers (created_at);

--transient_bundles
CREATE INDEX ON bb.transient_bundles (transient_transfer_id);

--transient_nodes
CREATE INDEX ON bb.transient_nodes (transient_bundle_id);
CREATE INDEX ON bb.transient_nodes (stock_id);

--transient_snapshots

--tags
CREATE INDEX ON bb.groups_tags (tag_id);
CREATE INDEX ON bb.users_tags (tag_id);
CREATE INDEX ON bb.items_tags (tag_id);
CREATE INDEX ON bb.owners_tags (tag_id);
CREATE INDEX ON bb.locations_tags (tag_id);
CREATE INDEX ON bb.statuses_tags (tag_id);
CREATE INDEX ON bb.transfers_tags (tag_id);
CREATE INDEX ON bb.transients_tags (tag_id);
CREATE INDEX ON bb.transient_transfers_tags (tag_id);

--===========================
--privileges
--===========================

--スキーマ使用権を付与
GRANT USAGE ON SCHEMA bb TO blackbox;

--シーケンス使用権を付与
GRANT USAGE ON ALL SEQUENCES IN SCHEMA bb TO blackbox;

--全テーブルSELECT可能
GRANT SELECT ON ALL TABLES IN SCHEMA bb TO blackbox;

GRANT INSERT, UPDATE, DELETE ON TABLE
	bb.orgs,
	bb.groups,
	bb.relationships,
	bb.users,
	bb.items,
	bb.owners,
	bb.locations,
	bb.statuses,
	bb.transients,
	bb.transient_current_stocks,
	bb.transient_transfers,
	bb.transient_bundles,
	bb.transient_nodes,
	bb.transient_snapshots
TO blackbox;

GRANT INSERT, UPDATE ON TABLE
	bb.last_closings,
	bb.current_stocks,
	bb.snapshots,
	bb.closed_stocks,
	bb.jobs
TO blackbox;

--tag関連はINSERT, DELETEのみ
GRANT INSERT, DELETE ON TABLE
	bb.tags,
	bb.locking_groups,
	bb.groups_tags,
	bb.users_tags,
	bb.items_tags,
	bb.owners_tags,
	bb.locations_tags,
	bb.statuses_tags,
	bb.transfers_tags,
	bb.transients_tags,
	bb.transient_transfers_tags
TO blackbox;

--closings, transfers関連はINSERTのみ
GRANT INSERT ON TABLE
	bb.closings,
	bb.stocks,
	bb.transfers,
	bb.bundles,
	bb.nodes,
	bb.transfer_errors
TO blackbox;
