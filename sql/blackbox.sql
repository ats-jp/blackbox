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
id=0のデータ
	マスタでid=0のデータは、いわゆるNULLデータとして扱う
*/

CREATE SCHEMA bb;

COMMENT ON SCHEMA bb IS 'Blackbox Main Schema';

/*
--postgresql tablespace
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox';
*/

--組織
CREATE TABLE bb.orgs (
	id bigserial PRIMARY KEY,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint NOT NULL, --あとでREFERENCES usersに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint NOT NULL); --あとでREFERENCES usersに
--システム利用者最大単位
--log対象

COMMENT ON TABLE bb.orgs IS '組織
Blackboxを使用する組織';
COMMENT ON COLUMN bb.orgs.id IS 'ID';
COMMENT ON COLUMN bb.orgs.name IS '名称';
COMMENT ON COLUMN bb.orgs.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.orgs.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.orgs.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb.orgs.created_at IS '作成時刻';
COMMENT ON COLUMN bb.orgs.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.orgs.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.orgs.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.orgs (
	id,
	name,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (0, 'NULL', 0, '{}', 0, 0);

--システム用
INSERT INTO bb.orgs (
	name,
	revision,
	extension,
	created_by,
	updated_by
) VALUES ('Blackbox', 0, '{}', 1, 1);

----------

CREATE TABLE bb.tags (
	id bigserial PRIMARY KEY,
	tag text UNIQUE NOT NULL);

--グループ
CREATE TABLE bb.groups (
	id bigserial PRIMARY KEY,
	org_id bigint REFERENCES bb.orgs NOT NULL,
	name text NOT NULL,
	parent_id bigint REFERENCES bb.groups NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint NOT NULL, --あとでREFERENCES userに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint NOT NULL); --あとでREFERENCES userに
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
) VALUES (0, 0, 'NULL', 0, 0, '{}', 0, 0);

--システム用
INSERT INTO bb.groups (
	org_id,
	name,
	parent_id,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (1, 'Superusers', 0, 0, '{}', 1, 1);

CREATE TABLE bb.group_tags (
	group_id bigint REFERENCES bb.groups ON DELETE CASCADE NOT NULL,
	tag_id bigint REFERENCES bb.tags ON DELETE CASCADE NOT NULL);

----------

--グループ親子関係
CREATE TABLE bb.relationships (
	id bigserial PRIMARY KEY,
	parent_id bigint REFERENCES bb.groups ON DELETE CASCADE NOT NULL,
	child_id bigint REFERENCES bb.groups ON DELETE CASCADE NOT NULL,
	cascade_id bigint NOT NULL, --あとでREFERENCES relationshipsに
	UNIQUE (parent_id, child_id));
--log対象外
--groupsから復元できるデータであるが、更新頻度は高くないと思われるのでWAL対象
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
	(3, 親, 子, 0)
孫を登録
	(4, 孫, 孫, 0)
	(5, 子, 孫, 0)
	(6, 親, 孫, 5)
曾孫を登録
	(7, 曾孫, 曾孫, 0)
	(8, 孫, 曾孫, 0)
	(9, 子, 曾孫, 8)
	(10, 親, 曾孫, 9)
親 <- 子の連携を変更する場合、一旦子の関係する連携を削除し、その後再度子以下の連携を登録することで実現する
削除のフェーズにおいて、子のIDを持つ全行を削除すると
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
) VALUES (0, 0, 0, 0);

ALTER TABLE bb.relationships ADD CONSTRAINT relationships_cascade_id_fkey FOREIGN KEY (cascade_id) REFERENCES bb.relationships ON DELETE CASCADE;

----------

--ユーザー
CREATE TABLE bb.users (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	role smallint CHECK (role IN (0, 1, 2, 3, 9)) NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint NOT NULL, --あとでREFERENCES usersに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint NOT NULL); --あとでREFERENCES usersに
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
) VALUES (0, 0, 'NULL', 9, 0, '{}', 0, 0);

--Superuser
INSERT INTO bb.users (
	group_id,
	name,
	role,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (1, 'superuser', 0, 0, '{}', 1, 1);

ALTER TABLE bb.orgs ADD FOREIGN KEY (created_by) REFERENCES bb.users;
ALTER TABLE bb.orgs ADD FOREIGN KEY (updated_by) REFERENCES bb.users;
ALTER TABLE bb.groups ADD FOREIGN KEY (created_by) REFERENCES bb.users;
ALTER TABLE bb.groups ADD FOREIGN KEY (updated_by) REFERENCES bb.users;
ALTER TABLE bb.users ADD FOREIGN KEY (created_by) REFERENCES bb.users;
ALTER TABLE bb.users ADD FOREIGN KEY (updated_by) REFERENCES bb.users;

CREATE TABLE bb.user_tags (
	user_id bigint REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	tag_id bigint REFERENCES bb.tags ON DELETE CASCADE NOT NULL);

----------

--ロック中グループ
CREATE UNLOGGED TABLE bb.locking_groups (
	id bigint PRIMARY KEY REFERENCES bb.groups ON DELETE CASCADE,
	user_id bigint REFERENCES bb.users NOT NULL,
	locked_at timestamptz DEFAULT now() NOT NULL);
--log対象外
--WAL対象外
--ロックのたびにグループの親子関係を展開してINSERTし、解放時はROLLBACKかDELETEで行う
--PKの一意制約を利用して他でロック中であればINSERTできないことで排他を行う

COMMENT ON TABLE bb.locking_groups IS 'ロック中グループ';

--===========================
--master tables
--===========================

--もの
CREATE TABLE bb.items (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);
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
) VALUES (0, 0, 'NULL', 0, '{}', 0, 0);

CREATE TABLE bb.item_tags (
	item_id bigint REFERENCES bb.items ON DELETE CASCADE NOT NULL,
	tag_id bigint REFERENCES bb.tags ON DELETE CASCADE NOT NULL);

----------

--所有者
CREATE TABLE bb.owners (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);
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
) VALUES (0, 0, 'NULL', 0, '{}', 0, 0);

CREATE TABLE bb.owner_tags (
	owner_id bigint REFERENCES bb.owners ON DELETE CASCADE NOT NULL,
	tag_id bigint REFERENCES bb.tags ON DELETE CASCADE NOT NULL);

----------

--置き場
CREATE TABLE bb.locations (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);
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
) VALUES (0, 0, 'NULL', 0, '{}', 0, 0);

CREATE TABLE bb.location_tags (
	location_id bigint REFERENCES bb.locations ON DELETE CASCADE NOT NULL,
	tag_id bigint REFERENCES bb.tags ON DELETE CASCADE NOT NULL);

----------

--状態
CREATE TABLE bb.statuses (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);
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
) VALUES (0, 0, 'NULL', 0, '{}', 0, 0);

CREATE TABLE bb.status_tags (
	status_id bigint REFERENCES bb.statuses ON DELETE CASCADE NOT NULL,
	tag_id bigint REFERENCES bb.tags ON DELETE CASCADE NOT NULL);

----------

--締め
CREATE TABLE bb.closings (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	closed_at timestamptz NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL);
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
	id bigint PRIMARY KEY REFERENCES bb.groups ON DELETE CASCADE,
	closing_id bigint REFERENCES bb.closings ON DELETE CASCADE NOT NULL,
	closed_at timestamptz NOT NULL);
--log対象外
--WAL対象外
--締め済のグループを、親子関係を展開して登録し、締済みかどうかを高速に判定できるようにする
--先に子が締めを行い、その後親で締めを行った場合、親側で上書きするためclosing_idは親側に変更する
--具体的には、指定されたグループIDの子すべてをこのテーブルから一旦削除し、全部の子分を追加することで実現する

COMMENT ON TABLE bb.last_closings IS 'グループ最終締め情報';
COMMENT ON COLUMN bb.last_closings.id IS 'ID
グループIDに従属';
COMMENT ON COLUMN bb.last_closings.closing_id IS '締めID';
COMMENT ON COLUMN bb.last_closings.closed_at IS '締め時刻';

----------

--在庫
CREATE TABLE bb.stocks (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	item_id bigint REFERENCES bb.items NOT NULL,
	owner_id bigint REFERENCES bb.owners NOT NULL,
	location_id bigint REFERENCES bb.locations NOT NULL,
	status_id bigint REFERENCES bb.statuses NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	UNIQUE (group_id, item_id, owner_id, location_id, status_id));
--log対象外

COMMENT ON TABLE bb.stocks IS '在庫
Blackboxで数量管理する在庫の最小単位';
COMMENT ON COLUMN bb.stocks.id IS 'ID';
COMMENT ON COLUMN bb.stocks.group_id IS 'グループID';
COMMENT ON COLUMN bb.stocks.item_id IS 'アイテムID';
COMMENT ON COLUMN bb.stocks.owner_id IS '所有者ID';
COMMENT ON COLUMN bb.stocks.location_id IS '置き場ID';
COMMENT ON COLUMN bb.stocks.status_id IS '状態ID';
COMMENT ON COLUMN bb.stocks.created_at IS '作成時刻';
COMMENT ON COLUMN bb.stocks.created_by IS '作成ユーザー';

--===========================
--transfer tables
--===========================

--移動伝票
CREATE TABLE bb.transfers (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	denied_id bigint REFERENCES bb.transfers DEFAULT 0 NOT NULL,
	transferred_at timestamptz NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	org_extension jsonb NOT NULL,
	group_extension jsonb NOT NULL,
	user_extension jsonb NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL);
--log対象外

COMMENT ON TABLE bb.transfers IS '移動伝票';
COMMENT ON COLUMN bb.transfers.id IS 'ID';
COMMENT ON COLUMN bb.transfers.group_id IS 'グループID';
COMMENT ON COLUMN bb.transfers.denied_id IS '取消元伝票ID
訂正後の伝票が訂正前の伝票のIDを持つ
ここに入っているIDが指す伝票は、取り消されたものとなる';
COMMENT ON COLUMN bb.transfers.transferred_at IS '移動時刻';
COMMENT ON COLUMN bb.transfers.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.transfers.org_extension IS '組織のextension';
COMMENT ON COLUMN bb.transfers.group_extension IS 'グループのextension';
COMMENT ON COLUMN bb.transfers.user_extension IS '作成ユーザーのextension';
COMMENT ON COLUMN bb.transfers.tags IS '保存用タグ';
COMMENT ON COLUMN bb.transfers.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transfers.created_by IS '作成ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.transfers (
	id,
	group_id,
	denied_id,
	transferred_at,
	extension,
	org_extension,
	group_extension,
	user_extension,
	created_by
) VALUES (0, 0, 0, '1900-1-1'::timestamptz, '{}', '{}', '{}', '{}', 0);

--締め済グループチェック
CREATE FUNCTION bb.closed_check() RETURNS TRIGGER AS $$
	DECLARE closed_at timestamptz;
	BEGIN
		SELECT INTO closed_at closed_at FROM bb.last_closings WHERE id = NEW.group_id;
		IF closed_at IS NOT NULL AND NEW.transferred_at < closed_at THEN
			RAISE EXCEPTION 'closed_check(): group id=[%] closed at %', NEW.group_id, closed_at;
		END IF;
		RETURN NEW;
	END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER closed_checktrigger BEFORE INSERT ON bb.transfers
FOR EACH ROW EXECUTE PROCEDURE bb.closed_check();

CREATE TABLE bb.transfer_tags (
	transfer_id bigint REFERENCES bb.transfers ON DELETE CASCADE NOT NULL,
	tag_id bigint REFERENCES bb.tags ON DELETE CASCADE NOT NULL);

----------

--移動伝票明細
CREATE TABLE bb.bundles (
	id bigserial PRIMARY KEY,
	transfer_id bigint REFERENCES bb.transfers NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL);
--log対象外

COMMENT ON TABLE bb.bundles IS '移動伝票明細
移動の単体
移動伝票とノードの関連付けテーブル
出庫ノードと入庫ノードを束ねる';
COMMENT ON COLUMN bb.bundles.id IS 'ID';
COMMENT ON COLUMN bb.bundles.transfer_id IS '移動伝票ID';
COMMENT ON COLUMN bb.bundles.extension IS '外部アプリケーション情報JSON';

----------

--移動ノード
CREATE TABLE bb.nodes (
	id bigserial PRIMARY KEY,
	bundle_id bigint REFERENCES bb.bundles NOT NULL,
	stock_id bigint REFERENCES bb.stocks NOT NULL,
	in_out "char" CHECK (in_out IN ('I', 'O')) NOT NULL,
	quantity numeric CHECK (quantity >= 0) NOT NULL,
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
COMMENT ON COLUMN bb.nodes.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.nodes.group_extension IS 'グループのextension';
COMMENT ON COLUMN bb.nodes.item_extension IS 'アイテムのextension';
COMMENT ON COLUMN bb.nodes.owner_extension IS '所有者のextension';
COMMENT ON COLUMN bb.nodes.location_extension IS '置き場のextension';
COMMENT ON COLUMN bb.nodes.status_extension IS '状態のextension';

--transfer系のテーブルはINSERTのみなので、autovacuumは行わない
--ただし、ANALYZEがかからなくなるので、定期的に実施する必要がある
ALTER TABLE bb.transfers SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb.bundles SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb.stocks SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb.nodes SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);

----------

--移動ノード状態
CREATE UNLOGGED TABLE bb.snapshots (
	id bigint PRIMARY KEY REFERENCES bb.nodes,
	total numeric CHECK (total >= 0) NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users ON DELETE CASCADE NOT NULL);
--log対象外
--WAL対象外のため、クラッシュ時transfersから復元する必要あり
--頻繁に参照、更新されることが予想されるので締め済のデータは削除する

COMMENT ON TABLE bb.snapshots IS '移動ノード状態
transferred_at時点でのstockの状態';
COMMENT ON COLUMN bb.snapshots.id IS 'ID
nodes.node_idに従属';
COMMENT ON COLUMN bb.snapshots.total IS 'この時点の在庫総数';
COMMENT ON COLUMN bb.snapshots.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.snapshots.updated_by IS '更新ユーザー';

----------

--現在在庫
CREATE UNLOGGED TABLE bb.current_stocks (
	id bigserial PRIMARY KEY REFERENCES bb.stocks, --stockは削除されないのでCASCADEなし
	total numeric CHECK (total >= 0) NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL);
--log対象外
--WAL対象外のため、クラッシュ時transfersから復元する必要あり
--totalの更新は常にポーリング処理から行われるためcreated_byを持たない

COMMENT ON TABLE bb.current_stocks IS '現在在庫
在庫の現在数を保持';
COMMENT ON COLUMN bb.current_stocks.id IS 'ID
stocks.stock_idに従属';
COMMENT ON COLUMN bb.current_stocks.total IS '現時点の在庫総数';
COMMENT ON COLUMN bb.current_stocks.updated_at IS '更新時刻';

----------

--締め在庫
CREATE TABLE bb.closed_stocks (
	id bigint REFERENCES bb.stocks, --stockは削除されないのでCASCADEなし
	closing_id bigint REFERENCES bb.closings ON DELETE CASCADE NOT NULL,
	total numeric CHECK (total >= 0) NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users ON DELETE CASCADE NOT NULL);
--log対象外
--締め完了後の在庫数を保持
--クラッシュ時、ここからsnapshotsとcurrent_stocksを復元する

COMMENT ON TABLE bb.closed_stocks IS '締め在庫';
COMMENT ON COLUMN bb.closed_stocks.id IS 'ID
在庫IDに従属';
COMMENT ON COLUMN bb.closed_stocks.closing_id IS '締めID';
COMMENT ON COLUMN bb.closed_stocks.total IS '締め後の在庫総数';
COMMENT ON COLUMN bb.closed_stocks.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.closed_stocks.updated_by IS '更新ユーザー';

--===========================
--job tables
--===========================

--追加処理トリガ
CREATE TABLE bb.triggers (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	fqcn text NOT NULL,
	name text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL);
--簡略化のためログ不要
--簡略化のため更新無し
--削除可能
--更新が必要になれば別行として追加していく

COMMENT ON TABLE bb.triggers IS '追加処理トリガ';
COMMENT ON COLUMN bb.triggers.id IS 'ID';
COMMENT ON COLUMN bb.triggers.group_id IS 'グループID';
COMMENT ON COLUMN bb.triggers.fqcn IS '実施FQCN';
COMMENT ON COLUMN bb.triggers.name IS '処理名';
COMMENT ON COLUMN bb.triggers.created_at IS '作成時刻';
COMMENT ON COLUMN bb.triggers.created_by IS '作成ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.triggers (
	id,
	group_id,
	fqcn,
	name,
	created_by
) VALUES (0, 0, '', 'NULL', 0);

----------

--現在在庫数量反映ジョブ
CREATE TABLE bb.jobs (
	id bigserial PRIMARY KEY REFERENCES bb.transfers,
	completed boolean DEFAULT false NOT NULL,
	trigger_id bigint REFERENCES bb.triggers DEFAULT 0 NOT NULL,
	parameter jsonb DEFAULT '{}' NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);
--transfer毎に作成
--transfers.transferred_atに実行
--currentの更新は必ず実行するためactive=falseによる無効が行えないようにactiveは無し

COMMENT ON TABLE bb.jobs IS '現在在庫数量反映ジョブ';
COMMENT ON COLUMN bb.jobs.id IS 'ID
transfers.transfers_idに従属';
COMMENT ON COLUMN bb.jobs.completed IS '実施済フラグ';
COMMENT ON COLUMN bb.jobs.trigger_id IS '追加処理ID';
COMMENT ON COLUMN bb.jobs.parameter IS 'triggerパラメータ';
COMMENT ON COLUMN bb.jobs.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.jobs.created_at IS '作成時刻';
COMMENT ON COLUMN bb.jobs.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.jobs.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.jobs.updated_by IS '更新ユーザー';

--===========================
--transient tables
--===========================

--一時作業
CREATE TABLE bb.transients (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES bb.groups NOT NULL,
	user_id bigint REFERENCES bb.users NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);

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

CREATE TABLE bb.transient_tags (
	transient_id bigint REFERENCES bb.transients ON DELETE CASCADE NOT NULL,
	tag_id bigint REFERENCES bb.tags ON DELETE CASCADE NOT NULL);

----------

--一時作業移動伝票
CREATE TABLE bb.transient_transfers (
	id bigserial PRIMARY KEY,
	transient_id bigint REFERENCES bb.transients NOT NULL,
	group_id bigint REFERENCES bb.groups NOT NULL,
	transferred_at timestamptz NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	completed boolean DEFAULT false NOT NULL,
	trigger_id bigint REFERENCES bb.triggers DEFAULT 0 NOT NULL,
	parameter jsonb DEFAULT '{}' NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);

COMMENT ON TABLE bb.transient_transfers IS '一時作業移動伝票';
COMMENT ON COLUMN bb.transient_transfers.id IS 'ID';
COMMENT ON COLUMN bb.transient_transfers.transient_id IS '一時作業ID';
COMMENT ON COLUMN bb.transient_transfers.group_id IS 'グループID';
COMMENT ON COLUMN bb.transient_transfers.transferred_at IS '移動時刻';
COMMENT ON COLUMN bb.transient_transfers.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.transient_transfers.tags IS '保存用タグ';
COMMENT ON COLUMN bb.transient_transfers.completed IS '実施済フラグ';
COMMENT ON COLUMN bb.transient_transfers.trigger_id IS '追加処理ID';
COMMENT ON COLUMN bb.transient_transfers.parameter IS 'triggerパラメータ';
COMMENT ON COLUMN bb.transient_transfers.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.transient_transfers.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_transfers.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transient_transfers.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transient_transfers.updated_by IS '更新ユーザー';

--締め済グループチェック
CREATE FUNCTION bb.transient_closed_check() RETURNS TRIGGER AS $$
	DECLARE closed_at timestamptz;
	BEGIN
		SELECT INTO closed_at closed_at FROM bb.last_closings WHERE id = NEW.group_id;
		IF closed_at IS NOT NULL AND NEW.transferred_at < closed_at THEN
			RAISE EXCEPTION 'closed_check(): group id=[%] closed at %', NEW.group_id, closed_at;
		END IF;
		RETURN NEW;
	END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER transient_closed_checktrigger BEFORE INSERT ON bb.transient_transfers
FOR EACH ROW EXECUTE PROCEDURE bb.transient_closed_check();

CREATE TABLE bb.transient_transfer_tags (
	transient_transfer_id bigint REFERENCES bb.transient_transfers ON DELETE CASCADE NOT NULL,
	tag_id bigint REFERENCES bb.tags ON DELETE CASCADE NOT NULL);

----------

--一時作業移動伝票明細
CREATE TABLE bb.transient_bundles (
	id bigserial PRIMARY KEY,
	transient_transfer_id bigint REFERENCES bb.transient_transfers NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,-- 編集でtransient_bundlesだけ追加することもあるので必要
	created_by bigint REFERENCES bb.users NOT NULL);

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
	id bigserial PRIMARY KEY,
	transient_bundle_id bigint REFERENCES bb.transient_bundles NOT NULL,
	stock_id bigint REFERENCES bb.stocks NOT NULL,
	in_out "char" CHECK (in_out IN ('I', 'O')) NOT NULL,
	quantity numeric CHECK (quantity >= 0) NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);

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
	id bigint PRIMARY KEY REFERENCES bb.transient_nodes,
	total numeric CHECK (total >= 0) NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);

COMMENT ON TABLE bb.transient_snapshots IS '一時作業移動ノード状態';
COMMENT ON COLUMN bb.transient_snapshots.id IS 'ID';
COMMENT ON COLUMN bb.transient_snapshots.total IS 'この時点の在庫総数';
COMMENT ON COLUMN bb.transient_snapshots.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_snapshots.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transient_snapshots.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transient_snapshots.updated_by IS '更新ユーザー';

----------

--一時作業現在在庫
CREATE TABLE bb.transient_current_stocks (
	id bigint PRIMARY KEY REFERENCES bb.stocks, --先にstocksにデータを作成してからこのテーブルにデータ作成
	transient_id bigint REFERENCES bb.transients NOT NULL,
	total numeric CHECK (total >= 0) NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES bb.users NOT NULL);

COMMENT ON TABLE bb.transient_current_stocks IS '一時作業現在在庫';
COMMENT ON COLUMN bb.transient_current_stocks.id IS 'ID
stocks.stock_idに従属';
COMMENT ON COLUMN bb.transient_current_stocks.transient_id IS '一時作業ID';
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

--triggers

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
CREATE INDEX ON bb.group_tags (tag_id);
CREATE INDEX ON bb.user_tags (tag_id);
CREATE INDEX ON bb.item_tags (tag_id);
CREATE INDEX ON bb.owner_tags (tag_id);
CREATE INDEX ON bb.location_tags (tag_id);
CREATE INDEX ON bb.status_tags (tag_id);
CREATE INDEX ON bb.transfer_tags (tag_id);
CREATE INDEX ON bb.transient_tags (tag_id);

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
	bb.current_stocks,
	bb.snapshots,
	bb.jobs,
	bb.transients,
	bb.transient_current_stocks,
	bb.transient_transfers,
	bb.transient_bundles,
	bb.transient_nodes,
	bb.transient_snapshots
TO blackbox;

--closings, triggersはINSERT, DELETEのみ
GRANT INSERT, DELETE ON TABLE
	bb.tags,
	bb.closings,
	bb.triggers,
	bb.group_tags,
	bb.user_tags,
	bb.item_tags,
	bb.owner_tags,
	bb.location_tags,
	bb.status_tags,
	bb.transfer_tags,
	bb.transient_tags
TO blackbox;

--transfers関連はINSERTのみ
GRANT INSERT ON TABLE
	bb.stocks,
	bb.transfers,
	bb.bundles,
	bb.nodes
TO blackbox;
