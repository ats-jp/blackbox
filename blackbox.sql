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
DROP SCHEMA IF EXISTS log CASCADE; --logはblackboxに依存しているのでDROP
DROP SCHEMA IF EXISTS main CASCADE;

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

CREATE SCHEMA main;

COMMENT ON SCHEMA main IS 'Blackbox Main Schema';

--postgresql tablespace
--開発環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox';

--組織
CREATE TABLE main.orgs (
	id bigserial PRIMARY KEY,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint NOT NULL, --あとでREFERENCES userに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint NOT NULL); --あとでREFERENCES userに
--システム利用者最大単位
--log対象

COMMENT ON TABLE main.orgs IS '組織
Blackboxを使用する組織';
COMMENT ON COLUMN main.orgs.id IS 'ID';
COMMENT ON COLUMN main.orgs.name IS '名称';
COMMENT ON COLUMN main.orgs.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.orgs.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.orgs.active IS 'アクティブフラグ';
COMMENT ON COLUMN main.orgs.created_at IS '作成時刻';
COMMENT ON COLUMN main.orgs.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.orgs.updated_at IS '更新時刻';
COMMENT ON COLUMN main.orgs.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO main.orgs (
	id,
	name,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (0, 'NULL', 0, '{}', 0, 0);

--システム用
INSERT INTO main.orgs (
	name,
	revision,
	extension,
	created_by,
	updated_by
) VALUES ('Blackbox', 0, '{}', 1, 1);

----------

--グループ
CREATE TABLE main.groups (
	id bigserial PRIMARY KEY,
	org_id bigint REFERENCES main.orgs NOT NULL,
	name text NOT NULL,
	parent_id bigint REFERENCES main.groups NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
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

COMMENT ON TABLE main.groups IS 'グループ
組織配下の中でのまとまり';
COMMENT ON COLUMN main.groups.id IS 'ID';
COMMENT ON COLUMN main.groups.org_id IS '組織ID';
COMMENT ON COLUMN main.groups.name IS '名称';
COMMENT ON COLUMN main.groups.parent_id IS '親グループID';
COMMENT ON COLUMN main.groups.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.groups.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.groups.active IS 'アクティブフラグ';
COMMENT ON COLUMN main.groups.created_at IS '作成時刻';
COMMENT ON COLUMN main.groups.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.groups.updated_at IS '更新時刻';
COMMENT ON COLUMN main.groups.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO main.groups (
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
INSERT INTO main.groups (
	org_id,
	name,
	parent_id,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (1, 'Superuser Groups', 0, 0, '{}', 1, 1);

----------

--グループ親子関係
CREATE TABLE main.relationships (
	parent_id bigint REFERENCES main.groups NOT NULL,
	child_id bigint REFERENCES main.groups NOT NULL,
	UNIQUE (parent_id, child_id));
--log対象外
--親IDが指定されたら子も対象とするための補助テーブル
--groupの親子関係を物理的に展開し、検索を高速化することが目的
--子グループから辿れるすべての親(0を除く)と自分自身にこのエントリを作成する
--例)
--親 <- 子 <- 孫という関係性の場合
--(親, 親)
--(親, 子)
--(親, 孫)
--(子, 子)
--(子, 孫)
--(孫, 孫)
--が必要となる

----------

--ユーザー
CREATE TABLE main.users (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES main.groups NOT NULL,
	name text NOT NULL,
	role smallint CHECK (role IN (0, 1, 2, 3, 9)) NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint NOT NULL, --あとでREFERENCES userに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint NOT NULL); --あとでREFERENCES userに
--log対象

COMMENT ON TABLE main.users IS 'ユーザー
Blackboxの操作者';
COMMENT ON COLUMN main.users.id IS 'ID';
COMMENT ON COLUMN main.users.group_id IS 'グループID';
COMMENT ON COLUMN main.users.name IS '名称';
COMMENT ON COLUMN main.users.role IS '役割
0=SYSTEM_ADMIN, 1=ORG_ADMIN, 2=GROUP_ADMIN, 3=USER, 9=NONE';
COMMENT ON COLUMN main.users.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.users.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.users.active IS 'アクティブフラグ';
COMMENT ON COLUMN main.users.created_at IS '作成時刻';
COMMENT ON COLUMN main.users.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.users.updated_at IS '更新時刻';
COMMENT ON COLUMN main.users.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO main.users (
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
INSERT INTO main.users (
	group_id,
	name,
	role,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (1, 'Superuser', 0, 0, '{}', 1, 1);

ALTER TABLE main.orgs ADD FOREIGN KEY (created_by) REFERENCES main.users;
ALTER TABLE main.orgs ADD FOREIGN KEY (updated_by) REFERENCES main.users;
ALTER TABLE main.groups ADD FOREIGN KEY (created_by) REFERENCES main.users;
ALTER TABLE main.groups ADD FOREIGN KEY (updated_by) REFERENCES main.users;
ALTER TABLE main.users ADD FOREIGN KEY (created_by) REFERENCES main.users;
ALTER TABLE main.users ADD FOREIGN KEY (updated_by) REFERENCES main.users;

--===========================
--master tables
--===========================

--もの
CREATE TABLE main.items (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES main.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);
--log対象

COMMENT ON TABLE main.items IS 'アイテム
在庫管理する対象となる「もの」';
COMMENT ON COLUMN main.items.id IS 'ID';
COMMENT ON COLUMN main.items.group_id IS 'グループID';
COMMENT ON COLUMN main.items.name IS '名称';
COMMENT ON COLUMN main.items.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.items.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.items.active IS 'アクティブフラグ';
COMMENT ON COLUMN main.items.created_at IS '作成時刻';
COMMENT ON COLUMN main.items.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.items.updated_at IS '更新時刻';
COMMENT ON COLUMN main.items.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO main.items (
	id,
	group_id,
	name,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (0, 0, 'NULL', 0, '{}', 0, 0);

----------

--所有者
CREATE TABLE main.owners (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES main.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);
--log対象

COMMENT ON TABLE main.owners IS '所有者
アイテムの所有者';
COMMENT ON COLUMN main.owners.id IS 'ID';
COMMENT ON COLUMN main.owners.group_id IS 'グループID';
COMMENT ON COLUMN main.owners.name IS '名称';
COMMENT ON COLUMN main.owners.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.owners.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.owners.active IS 'アクティブフラグ';
COMMENT ON COLUMN main.owners.created_at IS '作成時刻';
COMMENT ON COLUMN main.owners.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.owners.updated_at IS '更新時刻';
COMMENT ON COLUMN main.owners.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO main.owners (
	id,
	group_id,
	name,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (0, 0, 'NULL', 0, '{}', 0, 0);

----------

--置き場
CREATE TABLE main.locations (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES main.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);
--log対象

COMMENT ON TABLE main.locations IS '置き場
アイテムの置き場';
COMMENT ON COLUMN main.locations.id IS 'ID';
COMMENT ON COLUMN main.locations.group_id IS 'グループID';
COMMENT ON COLUMN main.locations.name IS '名称';
COMMENT ON COLUMN main.locations.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.locations.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.locations.active IS 'アクティブフラグ';
COMMENT ON COLUMN main.locations.created_at IS '作成時刻';
COMMENT ON COLUMN main.locations.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.locations.updated_at IS '更新時刻';
COMMENT ON COLUMN main.locations.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO main.locations (
	id,
	group_id,
	name,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (0, 0, 'NULL', 0, '{}', 0, 0);

----------

--状態
CREATE TABLE main.statuses (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES main.groups NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);
--log対象

COMMENT ON TABLE main.statuses IS '状態
Blackbox内でのアイテムの状態';
COMMENT ON COLUMN main.statuses.id IS 'ID';
COMMENT ON COLUMN main.statuses.group_id IS 'グループID';
COMMENT ON COLUMN main.statuses.name IS '名称';
COMMENT ON COLUMN main.statuses.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.statuses.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.statuses.active IS 'アクティブフラグ';
COMMENT ON COLUMN main.statuses.created_at IS '作成時刻';
COMMENT ON COLUMN main.statuses.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.statuses.updated_at IS '更新時刻';
COMMENT ON COLUMN main.statuses.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO main.statuses (
	id,
	group_id,
	name,
	revision,
	extension,
	created_by,
	updated_by
) VALUES (0, 0, 'NULL', 0, '{}', 0, 0);

--===========================
--transfer tables
--===========================

--移動伝票
CREATE TABLE main.transfers (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES main.groups NOT NULL,
	denied_id bigint REFERENCES main.transfers DEFAULT 0 NOT NULL,
	transferred_at timestamptz NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	org_extension jsonb NOT NULL,
	group_extension jsonb NOT NULL,
	user_extension jsonb NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL);
--log対象外

COMMENT ON TABLE main.transfers IS '移動伝票';
COMMENT ON COLUMN main.transfers.id IS 'ID';
COMMENT ON COLUMN main.transfers.group_id IS 'グループID';
COMMENT ON COLUMN main.transfers.transferred_at IS '移動時刻';
COMMENT ON COLUMN main.transfers.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.transfers.org_extension IS '組織のextension';
COMMENT ON COLUMN main.transfers.group_extension IS 'グループのextension';
COMMENT ON COLUMN main.transfers.user_extension IS '作成ユーザーのextension';
COMMENT ON COLUMN main.transfers.created_at IS '作成時刻';
COMMENT ON COLUMN main.transfers.created_by IS '作成ユーザー';

--NULLの代用(id=0)
INSERT INTO main.transfers (
	id,
	group_id,
	denied_id,
	transferred_at,
	extension,
	org_extension,
	group_extension,
	user_extension,
	created_by
) VALUES (0, 0, 0, '0-1-1'::timestamptz, '{}', '{}', '{}', '{}', 0);

----------

--移動伝票明細
CREATE TABLE main.bundles (
	id bigserial PRIMARY KEY,
	transfer_id bigint REFERENCES main.transfers NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL);
--log対象外

COMMENT ON TABLE main.bundles IS '移動伝票明細
移動の単体
移動伝票とノードの関連付けテーブル
出庫ノードと入庫ノードを束ねる';
COMMENT ON COLUMN main.bundles.id IS 'ID';
COMMENT ON COLUMN main.bundles.transfer_id IS '移動伝票ID';
COMMENT ON COLUMN main.bundles.extension IS '外部アプリケーション情報JSON';

----------

--在庫
CREATE TABLE main.stocks (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES main.groups NOT NULL,
	item_id bigint REFERENCES main.items NOT NULL,
	owner_id bigint REFERENCES main.owners NOT NULL,
	location_id bigint REFERENCES main.locations NOT NULL,
	status_id bigint REFERENCES main.statuses NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	UNIQUE (group_id, item_id, owner_id, location_id, status_id));
--log対象外

COMMENT ON TABLE main.stocks IS '在庫
Blackboxで数量管理する在庫の最小単位';
COMMENT ON COLUMN main.stocks.id IS 'ID';
COMMENT ON COLUMN main.stocks.group_id IS 'グループID';
COMMENT ON COLUMN main.stocks.item_id IS 'アイテムID';
COMMENT ON COLUMN main.stocks.owner_id IS '所有者ID';
COMMENT ON COLUMN main.stocks.location_id IS '置き場ID';
COMMENT ON COLUMN main.stocks.status_id IS '状態ID';
COMMENT ON COLUMN main.stocks.created_at IS '作成時刻';
COMMENT ON COLUMN main.stocks.created_by IS '作成ユーザー';

----------

--移動ノード
CREATE TABLE main.nodes (
	id bigserial PRIMARY KEY,
	bundle_id bigint REFERENCES main.bundles NOT NULL,
	stock_id bigint REFERENCES main.stocks NOT NULL,
	in_out "char" CHECK (in_out IN ('I', 'O')) NOT NULL,
	quantity numeric CHECK (quantity >= 0) NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	item_extension jsonb DEFAULT '{}' NOT NULL,
	owner_extension jsonb DEFAULT '{}' NOT NULL,
	location_extension jsonb DEFAULT '{}' NOT NULL,
	status_extension jsonb DEFAULT '{}' NOT NULL);
--移動伝票明細片側
--log対象外

COMMENT ON TABLE main.nodes IS '移動ノード
一移動の中の入庫もしくは出庫を表す';
COMMENT ON COLUMN main.nodes.id IS 'ID';
COMMENT ON COLUMN main.nodes.bundle_id IS '移動ID';
COMMENT ON COLUMN main.nodes.stock_id IS '在庫ID';
COMMENT ON COLUMN main.nodes.in_out IS '入出庫区分';
COMMENT ON COLUMN main.nodes.quantity IS '移動数量';
COMMENT ON COLUMN main.nodes.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.nodes.item_extension IS 'アイテムのextension';
COMMENT ON COLUMN main.nodes.owner_extension IS '所有者のextension';
COMMENT ON COLUMN main.nodes.location_extension IS '置き場のextension';
COMMENT ON COLUMN main.nodes.status_extension IS '状態のextension';

--transfer系のテーブルはINSERTのみなので、autovacuumは行わない
--ただし、ANALYZEがかからなくなるので、定期的に実施する必要がある
ALTER TABLE main.transfers SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE main.bundles SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE main.stocks SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE main.nodes SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);

----------

--移動ノード状態
CREATE TABLE main.snapshots (
	id bigint PRIMARY KEY REFERENCES main.nodes,
	total numeric CHECK (total >= 0) NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);
--log対象外

COMMENT ON TABLE main.snapshots IS '移動ノード状態
transferred_at時点でのstockの状態';
COMMENT ON COLUMN main.snapshots.id IS 'ID
nodes.node_idに従属';
COMMENT ON COLUMN main.snapshots.total IS 'この時点の在庫総数';
COMMENT ON COLUMN main.snapshots.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.snapshots.updated_at IS '更新時刻';
COMMENT ON COLUMN main.snapshots.updated_by IS '更新ユーザー';

----------

--現在在庫
CREATE TABLE main.current_stocks (
	id bigserial PRIMARY KEY REFERENCES main.stocks,
	total numeric CHECK (total >= 0) NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);
--log対象外

COMMENT ON TABLE main.current_stocks IS '現在在庫
在庫の現在数を保持';
COMMENT ON COLUMN main.current_stocks.id IS 'ID
stocks.stock_idに従属';
COMMENT ON COLUMN main.current_stocks.total IS '現時点の在庫総数';
COMMENT ON COLUMN main.current_stocks.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.current_stocks.updated_at IS '更新時刻';
COMMENT ON COLUMN main.current_stocks.updated_by IS '更新ユーザー';

--===========================
--job tables
--===========================

--追加処理トリガ
CREATE TABLE main.triggers (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES main.groups NOT NULL,
	fqcn text NOT NULL,
	name text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL);
--簡略化のためログ不要
--簡略化のため更新無し
--削除可能
--更新が必要になれば別行として追加していく

COMMENT ON TABLE main.triggers IS '追加処理トリガ';
COMMENT ON COLUMN main.triggers.id IS 'ID';
COMMENT ON COLUMN main.triggers.group_id IS 'グループID';
COMMENT ON COLUMN main.triggers.fqcn IS '実施FQCN';
COMMENT ON COLUMN main.triggers.name IS '処理名';
COMMENT ON COLUMN main.triggers.created_at IS '作成時刻';
COMMENT ON COLUMN main.triggers.created_by IS '作成ユーザー';

--NULLの代用(id=0)
INSERT INTO main.triggers (
	id,
	group_id,
	fqcn,
	name,
	created_by
) VALUES (0, 0, '', 'NULL', 0);

----------

--現在在庫数量反映ジョブ
CREATE TABLE main.jobs (
	id bigserial PRIMARY KEY REFERENCES main.transfers,
	completed boolean DEFAULT false NOT NULL,
	trigger_id bigint REFERENCES main.triggers DEFAULT 0 NOT NULL,
	parameter jsonb DEFAULT '{}' NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);
--transfer毎に作成
--transfers.transferred_atに実行
--currentの更新は必ず実行するためactive=falseによる無効が行えないようにactiveは無し

COMMENT ON TABLE main.jobs IS '現在在庫数量反映ジョブ';
COMMENT ON COLUMN main.jobs.id IS 'ID
transfers.transfers_idに従属';
COMMENT ON COLUMN main.jobs.completed IS '実施済フラグ';
COMMENT ON COLUMN main.jobs.trigger_id IS '追加処理ID';
COMMENT ON COLUMN main.jobs.parameter IS 'triggerパラメータ';
COMMENT ON COLUMN main.jobs.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.jobs.created_at IS '作成時刻';
COMMENT ON COLUMN main.jobs.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.jobs.updated_at IS '更新時刻';
COMMENT ON COLUMN main.jobs.updated_by IS '更新ユーザー';

--===========================
--transient tables
--===========================

--一時作業
CREATE TABLE main.transients (
	id bigserial PRIMARY KEY,
	group_id bigint REFERENCES main.groups NOT NULL,
	user_id bigint REFERENCES main.users NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);

COMMENT ON TABLE main.transients IS '一時作業';
COMMENT ON COLUMN main.transients.id IS 'ID';
COMMENT ON COLUMN main.transients.group_id IS 'この一時作業のオーナーグループ
0の場合、オーナーグループはいない';
COMMENT ON COLUMN main.transients.user_id IS 'この一時作業のオーナーユーザー
0の場合、オーナーユーザーはいない';
COMMENT ON COLUMN main.transients.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.transients.created_at IS '作成時刻';
COMMENT ON COLUMN main.transients.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.transients.updated_at IS '更新時刻';
COMMENT ON COLUMN main.transients.updated_by IS '更新ユーザー';

----------

--一時作業移動伝票
CREATE TABLE main.transient_transfers (
	id bigserial PRIMARY KEY,
	transient_id bigint REFERENCES main.transients NOT NULL,
	group_id bigint REFERENCES main.groups NOT NULL,
	transferred_at timestamptz NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	completed boolean DEFAULT false NOT NULL,
	trigger_id bigint REFERENCES main.triggers DEFAULT 0 NOT NULL,
	parameter jsonb DEFAULT '{}' NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);

COMMENT ON TABLE main.transient_transfers IS '一時作業移動伝票';
COMMENT ON COLUMN main.transient_transfers.id IS 'ID';
COMMENT ON COLUMN main.transient_transfers.transient_id IS '一時作業ID';
COMMENT ON COLUMN main.transient_transfers.group_id IS 'グループID';
COMMENT ON COLUMN main.transient_transfers.transferred_at IS '移動時刻';
COMMENT ON COLUMN main.transient_transfers.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.transient_transfers.completed IS '実施済フラグ';
COMMENT ON COLUMN main.transient_transfers.trigger_id IS '追加処理ID';
COMMENT ON COLUMN main.transient_transfers.parameter IS 'triggerパラメータ';
COMMENT ON COLUMN main.transient_transfers.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.transient_transfers.created_at IS '作成時刻';
COMMENT ON COLUMN main.transient_transfers.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.transient_transfers.updated_at IS '更新時刻';
COMMENT ON COLUMN main.transient_transfers.updated_by IS '更新ユーザー';

----------

--一時作業移動伝票明細
CREATE TABLE main.transient_bundles (
	id bigserial PRIMARY KEY,
	transient_transfer_id bigint REFERENCES main.transient_transfers NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,-- 編集でtransient_bundlesだけ追加することもあるので必要
	created_by bigint REFERENCES main.users NOT NULL);

COMMENT ON TABLE main.transient_bundles IS '一時作業移動伝票明細';
COMMENT ON COLUMN main.transient_bundles.id IS 'ID';
COMMENT ON COLUMN main.transient_bundles.transient_transfer_id IS '一時作業移動伝票ID';
COMMENT ON COLUMN main.transient_bundles.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.transient_bundles.created_at IS '作成時刻';
COMMENT ON COLUMN main.transient_bundles.created_by IS '作成ユーザー';

----------

--一時作業移動ノード
CREATE TABLE main.transient_nodes (
	id bigserial PRIMARY KEY,
	transient_bundle_id bigint REFERENCES main.transient_bundles NOT NULL,
	stock_id bigint REFERENCES main.stocks NOT NULL,
	in_out "char" CHECK (in_out IN ('I', 'O')) NOT NULL,
	quantity numeric CHECK (quantity >= 0) NOT NULL,
	extension jsonb DEFAULT '{}' NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);

COMMENT ON TABLE main.transient_nodes IS '一時作業移動ノード';
COMMENT ON COLUMN main.transient_nodes.id IS 'ID';
COMMENT ON COLUMN main.transient_nodes.transient_bundle_id IS '移動ID';
COMMENT ON COLUMN main.transient_nodes.stock_id IS '在庫ID';
COMMENT ON COLUMN main.transient_nodes.in_out IS '入出庫区分';
COMMENT ON COLUMN main.transient_nodes.quantity IS '移動数量';
COMMENT ON COLUMN main.transient_nodes.extension IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN main.transient_nodes.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.transient_nodes.created_at IS '作成時刻';
COMMENT ON COLUMN main.transient_nodes.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.transient_nodes.updated_at IS '更新時刻';
COMMENT ON COLUMN main.transient_nodes.updated_by IS '更新ユーザー';

----------

--一時作業移動ノード状態
CREATE TABLE main.transient_snapshots (
	id bigint PRIMARY KEY REFERENCES main.transient_nodes,
	total numeric CHECK (total >= 0) NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);

COMMENT ON TABLE main.transient_snapshots IS '一時作業移動ノード状態';
COMMENT ON COLUMN main.transient_snapshots.id IS 'ID';
COMMENT ON COLUMN main.transient_snapshots.total IS 'この時点の在庫総数';
COMMENT ON COLUMN main.transient_snapshots.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.transient_snapshots.created_at IS '作成時刻';
COMMENT ON COLUMN main.transient_snapshots.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.transient_snapshots.updated_at IS '更新時刻';
COMMENT ON COLUMN main.transient_snapshots.updated_by IS '更新ユーザー';

----------

--一時作業現在在庫
CREATE TABLE main.transient_current_stocks (
	id bigint PRIMARY KEY REFERENCES main.stocks, --先にstocksにデータを作成してからこのテーブルにデータ作成
	transient_id bigint REFERENCES main.transients NOT NULL,
	total numeric CHECK (total >= 0) NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by bigint REFERENCES main.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by bigint REFERENCES main.users NOT NULL);

COMMENT ON TABLE main.transient_current_stocks IS '一時作業現在在庫';
COMMENT ON COLUMN main.transient_current_stocks.id IS 'ID
stocks.stock_idに従属';
COMMENT ON COLUMN main.transient_current_stocks.transient_id IS '一時作業ID';
COMMENT ON COLUMN main.transient_current_stocks.total IS '現時点の在庫総数';
COMMENT ON COLUMN main.transient_current_stocks.revision IS 'リビジョン番号';
COMMENT ON COLUMN main.transient_current_stocks.created_at IS '作成時刻';
COMMENT ON COLUMN main.transient_current_stocks.created_by IS '作成ユーザー';
COMMENT ON COLUMN main.transient_current_stocks.updated_at IS '更新時刻';
COMMENT ON COLUMN main.transient_current_stocks.updated_by IS '更新ユーザー';

--===========================
--indexes
--===========================

--開発環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox_index';

--orgs
CREATE INDEX ON main.orgs (active);

--groups
CREATE INDEX ON main.groups (org_id);
CREATE INDEX ON main.groups (active);
--parent_idで検索することはないのでindex不要

--relationships
CREATE INDEX ON main.relationships (parent_id);

--users
CREATE INDEX ON main.users (group_id);
CREATE INDEX ON main.users (role);
CREATE INDEX ON main.users (active);

--items
CREATE INDEX ON main.items (group_id);
CREATE INDEX ON main.items (active);

--owners
CREATE INDEX ON main.owners (group_id);
CREATE INDEX ON main.owners (active);

--locations
CREATE INDEX ON main.locations (group_id);
CREATE INDEX ON main.locations (active);

--statuses
CREATE INDEX ON main.statuses (group_id);
CREATE INDEX ON main.statuses (active);

--stocks
CREATE INDEX ON main.stocks (group_id);
CREATE INDEX ON main.stocks (item_id);
CREATE INDEX ON main.stocks (owner_id);
CREATE INDEX ON main.stocks (location_id);
CREATE INDEX ON main.stocks (status_id);

--current_stocks

--transfers
CREATE INDEX ON main.transfers (group_id);
CREATE INDEX ON main.transfers (transferred_at);
CREATE INDEX ON main.transfers (created_at);

--bundles
CREATE INDEX ON main.bundles (transfer_id);

--nodes
CREATE INDEX ON main.nodes (bundle_id);
CREATE INDEX ON main.nodes (stock_id);

--snapshots

--triggers

--jobs
CREATE INDEX ON main.jobs (completed);
--worker_idで検索することはないのでindex不要

--transients
CREATE INDEX ON main.transients (group_id);
CREATE INDEX ON main.transients (user_id);

--transient_current_stocks
CREATE INDEX ON main.transient_current_stocks (transient_id);

--transient_transfers
CREATE INDEX ON main.transient_transfers (transient_id);
CREATE INDEX ON main.transient_transfers (group_id);
CREATE INDEX ON main.transient_transfers (transferred_at);
CREATE INDEX ON main.transient_transfers (created_at);

--transient_bundles
CREATE INDEX ON main.transient_bundles (transient_transfer_id);

--transient_nodes
CREATE INDEX ON main.transient_nodes (transient_bundle_id);
CREATE INDEX ON main.transient_nodes (stock_id);

--transient_snapshots

--===========================
--privileges
--===========================

--全テーブルSELECT可能
GRANT SELECT ON ALL TABLES IN SCHEMA main TO blackbox;

GRANT INSERT, UPDATE, DELETE ON TABLE
	main.orgs,
	main.groups,
	main.relationships,
	main.users,
	main.items,
	main.owners,
	main.locations,
	main.statuses,
	main.current_stocks,
	main.snapshots,
	main.jobs,
	main.transients,
	main.transient_current_stocks,
	main.transient_transfers,
	main.transient_bundles,
	main.transient_nodes,
	main.transient_snapshots
TO blackbox;

--triggersはINSERT, DELETEのみ
GRANT INSERT, DELETE ON TABLE
	main.triggers
TO blackbox;

--transfers関連はINSERTのみ
GRANT INSERT ON TABLE
	main.stocks,
	main.transfers,
	main.bundles,
	main.nodes
TO blackbox;
