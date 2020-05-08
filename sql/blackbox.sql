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
props
	外部アプリケーションがBlackboxに保存させておくための情報をJSON形式にしたもの
	基本的にマスタテーブルが持ち、集約テーブルにはその時のマスタテーブルのpropsを合成して持つ
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

COMMENT ON SCHEMA bb IS 'Blackbox Core Schema Ver. 0.3';

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

----------

--組織
CREATE TABLE bb.orgs (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	instance_id uuid REFERENCES bb.instances NOT NULL,
	seq bigint NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid NOT NULL, --あとでREFERENCES usersに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid NOT NULL, --あとでREFERENCES usersに
	UNIQUE (instance_id, seq));
--システム利用者最大単位
--log対象

COMMENT ON TABLE bb.orgs IS '組織
Blackboxを使用する組織';
COMMENT ON COLUMN bb.orgs.id IS 'ID';
COMMENT ON COLUMN bb.orgs.instance_id IS '発生元インスタンスのID';
COMMENT ON COLUMN bb.orgs.seq IS 'インスタンス内連番';
COMMENT ON COLUMN bb.orgs.name IS '名称';
COMMENT ON COLUMN bb.orgs.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.orgs.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.orgs.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb.orgs.created_at IS '作成時刻';
COMMENT ON COLUMN bb.orgs.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.orgs.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.orgs.updated_by IS '更新ユーザー';

--NULLの代用(id=00000000-0000-0000-0000-000000000000)
INSERT INTO bb.orgs (
	id,
	instance_id,
	seq,
	name,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	0,
	'NULL',
	0,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

--システム用
INSERT INTO bb.orgs (
	id,
	instance_id,
	seq,
	name,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'11111111-1111-1111-1111-111111111111',
	'00000000-0000-0000-0000-000000000000',
	1,
	'Blackbox',
	0,
	'{}',
	'11111111-1111-1111-1111-111111111111',
	'11111111-1111-1111-1111-111111111111');

----------

CREATE TABLE bb.tags (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	tag text UNIQUE NOT NULL);

----------

--グループ
CREATE TABLE bb.groups (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	org_id uuid REFERENCES bb.orgs NOT NULL,
	seq bigint NOT NULL,
	name text NOT NULL,
	parent_id uuid REFERENCES bb.groups NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid NOT NULL, --あとでREFERENCES userに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid NOT NULL, --あとでREFERENCES userに
	UNIQUE (org_id, seq));
--組織配下の中でのまとまり
--権限コントロールのためのテーブル
--全てのオブジェクトは何らかのgroupに属するように
--グループの定義、運用はBlackboxの外部アプリケーションが行う
--log対象

COMMENT ON TABLE bb.groups IS 'グループ
組織配下の中でのまとまり';
COMMENT ON COLUMN bb.groups.id IS 'ID';
COMMENT ON COLUMN bb.groups.org_id IS '組織ID';
COMMENT ON COLUMN bb.groups.seq IS '組織内連番';
COMMENT ON COLUMN bb.groups.name IS '名称';
COMMENT ON COLUMN bb.groups.parent_id IS '親グループID';
COMMENT ON COLUMN bb.groups.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.groups.props IS '外部アプリケーション情報JSON';
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
	seq,
	name,
	parent_id,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	0,
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
	seq,
	name,
	parent_id,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'11111111-1111-1111-1111-111111111111',
	'11111111-1111-1111-1111-111111111111',
	0,
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
	seq bigint NOT NULL,
	name text NOT NULL,
	role smallint CHECK (role IN (0, 1, 2, 3, 9)) NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid NOT NULL, --あとでREFERENCES usersに
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid NOT NULL, --あとでREFERENCES usersに
	UNIQUE (group_id, seq));
--log対象

COMMENT ON TABLE bb.users IS 'ユーザー
Blackboxの操作者';
COMMENT ON COLUMN bb.users.id IS 'ID';
COMMENT ON COLUMN bb.users.group_id IS 'グループID';
COMMENT ON COLUMN bb.users.seq IS 'グループ内連番';
COMMENT ON COLUMN bb.users.name IS '名称';
COMMENT ON COLUMN bb.users.role IS '役割
値の小さいほうが強い権限となる
0=SYSTEM_ADMIN, 1=ORG_ADMIN, 2=GROUP_ADMIN, 3=USER, 9=NONE';
COMMENT ON COLUMN bb.users.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb.users.props IS '外部アプリケーション情報JSON';
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
	seq,
	name,
	role,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	0,
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
	seq,
	name,
	role,
	revision,
	props,
	created_by,
	updated_by
) VALUES (
	'11111111-1111-1111-1111-111111111111',
	'11111111-1111-1111-1111-111111111111',
	0,
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
COMMENT ON COLUMN bb.locking_groups.locked_at IS 'ロック開始時刻';

ALTER TABLE bb.locking_groups ADD CONSTRAINT locking_groups_cascade_id_fkey FOREIGN KEY (cascade_id) REFERENCES bb.locking_groups ON DELETE CASCADE;

----------

--締め
CREATE TABLE bb.closings (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	seq bigint NOT NULL,
	closed_at timestamptz NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	UNIQUE (group_id, seq));
--更新不可

COMMENT ON TABLE bb.closings IS '締め';
COMMENT ON COLUMN bb.closings.id IS 'ID';
COMMENT ON COLUMN bb.closings.group_id IS 'グループID';
COMMENT ON COLUMN bb.closings.seq IS 'グループ内連番';
COMMENT ON COLUMN bb.closings.closed_at IS '締め時刻';
COMMENT ON COLUMN bb.closings.props IS '外部アプリケーション情報JSON';
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

--管理対象
CREATE TABLE bb.units (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL);
--log対象外
--一度登録されたら変更されない

COMMENT ON TABLE bb.units IS '管理対象
Blackboxで数量管理する管理対象の最小単位';
COMMENT ON COLUMN bb.units.id IS 'ID';
COMMENT ON COLUMN bb.units.created_at IS '作成時刻';
COMMENT ON COLUMN bb.units.created_by IS '作成ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.units (id, created_by) VALUES ('00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000000');

--===========================
--journal tables
--===========================

--伝票一括登録
CREATE TABLE bb.journal_batches (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL);
--log対象外

COMMENT ON TABLE bb.journal_batches IS '伝票一括登録';
COMMENT ON COLUMN bb.journal_batches.id IS 'ID';
COMMENT ON COLUMN bb.journal_batches.created_at IS '作成時刻';
COMMENT ON COLUMN bb.journal_batches.created_by IS '作成ユーザー';

INSERT INTO bb.journal_batches (id, created_by) VALUES ('00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000000');

----------

--伝票
CREATE TABLE bb.journals (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	seq bigserial NOT NULL,
	journal_batch_id uuid REFERENCES bb.journal_batches NOT NULL,
	fixed_at timestamptz NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	instance_id uuid REFERENCES bb.instances NOT NULL,
	denied_id uuid REFERENCES bb.journals DEFAULT '00000000-0000-0000-0000-000000000000' NOT NULL,
	deny_reason text DEFAULT '' NOT NULL,
	org_props jsonb NOT NULL,
	group_props jsonb NOT NULL,
	user_props jsonb NOT NULL,
	created_at timestamptz NOT NULL, --Javaから指定するためDEFAULTなし
	created_by uuid REFERENCES bb.users NOT NULL,
	UNIQUE (group_id, seq),
	UNIQUE (group_id, created_at)); 
--log対象外
--順序を一意付けするためにcreated_atをUNIQUE化、他DBから移行してきたjournalのcreated_atと重複しないようにgroup_idも含める
--groupは単一instance内でのみ動かすのでcreated_atが重複することはないはず

COMMENT ON TABLE bb.journals IS '伝票';
COMMENT ON COLUMN bb.journals.id IS 'ID';
COMMENT ON COLUMN bb.journals.group_id IS 'グループID
この伝票の属するグループ';
COMMENT ON COLUMN bb.journals.seq IS 'グループ内連番';
COMMENT ON COLUMN bb.journals.journal_batch_id IS '移動伝票一括登録ID';
COMMENT ON COLUMN bb.journals.fixed_at IS '確定時刻';
COMMENT ON COLUMN bb.journals.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.journals.tags IS '保存用タグ';
COMMENT ON COLUMN bb.journals.instance_id IS '発生元インスタンスのID';
COMMENT ON COLUMN bb.journals.denied_id IS '取消元伝票ID
訂正後の伝票が訂正前の伝票のIDを持つ
ここに入っているIDが指す伝票は、取り消されたものとなる';
COMMENT ON COLUMN bb.journals.deny_reason IS '取消理由';
COMMENT ON COLUMN bb.journals.org_props IS '組織のprops';
COMMENT ON COLUMN bb.journals.group_props IS 'グループのprops';
COMMENT ON COLUMN bb.journals.user_props IS '作成ユーザーのprops';
COMMENT ON COLUMN bb.journals.created_at IS '作成時刻';
COMMENT ON COLUMN bb.journals.created_by IS '作成ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.journals (
	id,
	group_id,
	journal_batch_id,
	fixed_at,
	props,
	instance_id,
	org_props,
	group_props,
	user_props,
	created_at,
	created_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'1900-1-1'::timestamptz,
	'{}',
	'00000000-0000-0000-0000-000000000000',
	'{}',
	'{}',
	'{}',
	now(),
	'00000000-0000-0000-0000-000000000000');

--締め済グループチェック
CREATE FUNCTION bb.closed_check() RETURNS TRIGGER AS $$
	DECLARE closed_at_local timestamptz;
	BEGIN
		SELECT INTO closed_at_local closed_at FROM bb.last_closings WHERE id = NEW.group_id;
		IF closed_at_local IS NOT NULL AND NEW.fixed_at <= closed_at_local THEN
			RAISE EXCEPTION 'closed_check(): {"id":"%", "group_id":"%", "fixed_at":"%", "closed_at":"%"}', NEW.id, NEW.group_id, NEW.fixed_at, closed_at_local;
		END IF;
		RETURN NEW;
	END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER closed_checktrigger BEFORE INSERT ON bb.journals
FOR EACH ROW EXECUTE PROCEDURE bb.closed_check();

CREATE TABLE bb.journals_tags (
	id uuid REFERENCES bb.journals ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--伝票明細
CREATE TABLE bb.details (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	journal_id uuid REFERENCES bb.journals NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL);
--log対象外

COMMENT ON TABLE bb.details IS '伝票明細
伝票とノードの関連付けテーブル
出ノードと入ノードを束ねる';
COMMENT ON COLUMN bb.details.id IS 'ID';
COMMENT ON COLUMN bb.details.journal_id IS '伝票ID';
COMMENT ON COLUMN bb.details.props IS '外部アプリケーション情報JSON';

--NULLの代用(id=0)
INSERT INTO bb.details (
	id,
	journal_id
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000');

----------

--伝票明細ノード
CREATE TABLE bb.nodes (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	detail_id uuid REFERENCES bb.details NOT NULL,
	unit_id uuid REFERENCES bb.units NOT NULL,
	in_out smallint CHECK (in_out IN (1, -1)) NOT NULL, --そのまま計算に使用できるように
	seq integer CHECK (seq <= 999999) NOT NULL,
	quantity numeric CHECK (quantity >= 0) NOT NULL,
	grants_unlimited boolean DEFAULT false NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	unit_props jsonb DEFAULT '{}' NOT NULL);
--log対象外

COMMENT ON TABLE bb.nodes IS '伝票明細ノード
一伝票の中の入もしくは出を表す';
COMMENT ON COLUMN bb.nodes.id IS 'ID';
COMMENT ON COLUMN bb.nodes.detail_id IS '明細ID';
COMMENT ON COLUMN bb.nodes.unit_id IS '管理対象ID';
COMMENT ON COLUMN bb.nodes.in_out IS '入出区分
IN=1, OUT=-1';
COMMENT ON COLUMN bb.nodes.seq IS '伝票内連番
最大値999999';
COMMENT ON COLUMN bb.nodes.quantity IS '数量';
COMMENT ON COLUMN bb.nodes.grants_unlimited IS '数量無制限の許可
trueの場合、以降のsnapshotは数量がマイナスになってもエラーにならない';
COMMENT ON COLUMN bb.nodes.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.nodes.unit_props IS '管理対象のprops';

--NULLの代用(id=0)
INSERT INTO bb.nodes (
	id,
	detail_id,
	unit_id,
	in_out,
	seq,
	quantity
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	-1,
	0,
	0);

----------

--journal系のテーブルはINSERTのみなので、autovacuumは行わない場合は実行
--ただし、ANALYZEがかからなくなるので、定期的に実施する必要がある
--ALTER TABLE bb.units SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
--ALTER TABLE bb.journals SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
--ALTER TABLE bb.details SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
--ALTER TABLE bb.nodes SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);

----------

--移動ノード状態
CREATE UNLOGGED TABLE bb.snapshots (
	id uuid PRIMARY KEY REFERENCES bb.nodes,
	unlimited boolean NOT NULL,
	in_search_scope boolean DEFAULT true NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	journal_group_id uuid REFERENCES bb.groups NOT NULL,
	unit_id uuid REFERENCES bb.units NOT NULL,
	fixed_at timestamptz NOT NULL,
	seq char(36) NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	UNIQUE (journal_group_id, seq));
--log対象外
--WAL対象外のため、クラッシュ時journalから復元する必要あり
--頻繁に参照、更新されることが予想されるので締め済のデータは削除も可

COMMENT ON TABLE bb.snapshots IS '移動ノード状態
fixed_at時点でのunitの状態';
COMMENT ON COLUMN bb.snapshots.id IS 'ID
nodes.node_idに従属';
COMMENT ON COLUMN bb.snapshots.unlimited IS '数量無制限
trueの場合、totalがマイナスでもエラーとならない';
COMMENT ON COLUMN bb.snapshots.in_search_scope IS '数量検索対象
snapshotの検索対象を少なくすることで直近数量の取得検索を高速化する
締められた場合、締め時刻以下の最新のsnapshotを起点に直前の在庫数を取得するので、それ以前のsnapshotはfalseとなる';
COMMENT ON COLUMN bb.snapshots.total IS 'この時点の総数';
COMMENT ON COLUMN bb.snapshots.journal_group_id IS '伝票のグループID
検索高速化のためjournals.group_idをここに持つ';
COMMENT ON COLUMN bb.snapshots.unit_id IS '管理対象ID
検索高速化のためnodes.unit_idをここに持つ';
COMMENT ON COLUMN bb.snapshots.fixed_at IS '確定時刻
検索高速化のためjournals.fixed_atをここに持つ';
COMMENT ON COLUMN bb.snapshots.seq IS '移動ノード状態の登録順
検索高速化のためfixedAt, created_at, nodes.seqを連結しここに持つ
グループ内で一意であり順序として使用できる';
COMMENT ON COLUMN bb.snapshots.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.snapshots.updated_by IS '更新ユーザー';

--NULLの代用(id=0)
INSERT INTO bb.snapshots (
	id,
	unlimited,
	in_search_scope,
	total,
	journal_group_id,
	unit_id,
	fixed_at,
	seq,
	updated_by
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	false,
	false,
	0,
	'00000000-0000-0000-0000-000000000000',
	'00000000-0000-0000-0000-000000000000',
	'1900-1-1'::timestamptz,
	'000000000000000000000000000000000000',
	'00000000-0000-0000-0000-000000000000');

----------

--現在在庫
CREATE UNLOGGED TABLE bb.current_units (
	id uuid PRIMARY KEY REFERENCES bb.units, --unitは削除されないのでCASCADEなし
	unlimited boolean NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	snapshot_id uuid REFERENCES bb.snapshots ON DELETE CASCADE NOT NULL, --snapshot再作成のためsnapshotを削除した際に削除
	updated_at timestamptz DEFAULT now() NOT NULL);
--log対象外
--WAL対象外のため、クラッシュ時journalsから復元する必要あり
--totalの更新は常にポーリング処理から行われるためupdated_byを持たない

COMMENT ON TABLE bb.current_units IS '現時点管理対象
管理対象の現在数を保持';
COMMENT ON COLUMN bb.current_units.id IS 'ID
units.unit_idに従属';
COMMENT ON COLUMN bb.current_units.unlimited IS '数量無制限
trueの場合、totalがマイナスでもエラーとならない';
COMMENT ON COLUMN bb.current_units.total IS '現時点の総数';
COMMENT ON COLUMN bb.current_units.snapshot_id IS 'スナップショットID
現時点の数量を変更した伝票';
COMMENT ON COLUMN bb.current_units.updated_at IS '更新時刻';

--NULLの代用(id=0)
INSERT INTO bb.current_units (
	id,
	unlimited,
	total,
	snapshot_id,
	updated_at
) VALUES (
	'00000000-0000-0000-0000-000000000000',
	false,
	0,
	'00000000-0000-0000-0000-000000000000',
	now());

----------

--締め管理対象
CREATE TABLE bb.closed_units (
	id uuid PRIMARY KEY REFERENCES bb.units, --unitは削除されないのでCASCADEなし
	closing_id uuid REFERENCES bb.closings ON DELETE CASCADE NOT NULL,
	unlimited boolean NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL);
--log対象外
--締め完了後の在庫数を保持
--クラッシュ時、ここからsnapshotsとcurrent_unitsを復元する

COMMENT ON TABLE bb.closed_units IS '締め在庫';
COMMENT ON COLUMN bb.closed_units.id IS 'ID
在庫IDに従属';
COMMENT ON COLUMN bb.closed_units.closing_id IS '締めID';
COMMENT ON COLUMN bb.closed_units.unlimited IS '在庫無制限
trueの場合、totalがマイナスでもエラーとならない';
COMMENT ON COLUMN bb.closed_units.total IS '締め後の在庫総数';
COMMENT ON COLUMN bb.closed_units.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.closed_units.updated_by IS '更新ユーザー';

--現在在庫数量反映ジョブ
CREATE TABLE bb.jobs (
	id uuid PRIMARY KEY REFERENCES bb.journals,
	completed boolean DEFAULT false NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL);
--journal毎に作成
--journals.fixed_atに実行
--currentの更新は必ず実行するためactive=falseによる無効が行えないようにactiveは無し
--completedの更新は常にポーリング処理から行われるためupdated_byを持たない

COMMENT ON TABLE bb.jobs IS '現在在庫数量反映ジョブ';
COMMENT ON COLUMN bb.jobs.id IS 'ID
journals.journal_idに従属';
COMMENT ON COLUMN bb.jobs.completed IS '実施済フラグ';
COMMENT ON COLUMN bb.jobs.updated_at IS '更新時刻';

----------

--journal登録時に発生したエラー
CREATE TABLE bb.journal_errors (
	abandoned_id uuid NOT NULL,
	command_type text CHECK (command_type IN (
		'JOURNAL_REGISTER',
		'JOURNAL_LAZY_REGISTER',
		'JOURNAL_DENY',
		'OVERWRITE',
		'PAUSE',
		'RESUME',
		'GET_PAUSING_GROUPS',
		'CLOSE',
		'TRANSIENT_MOVE')) NOT NULL,
	error_type text NOT NULL,
	message text NOT NULL,
	stack_trace text NOT NULL,
	sql_state text NOT NULL,
	user_id uuid REFERENCES bb.users NOT NULL,
	request jsonb DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL);

COMMENT ON TABLE bb.journal_errors IS 'journal登録時に発生したエラー';
COMMENT ON COLUMN bb.journal_errors.abandoned_id IS 'journalもしくはclosingもしくはjournal_batchに使用される予定だったID';
COMMENT ON COLUMN bb.journal_errors.command_type IS '処理のタイプ
JOURNAL_REGISTER=journal登録
JOURNAL_LAZY_REGISTER=journal数量整合性チェック遅延登録
JOURNAL_DENY=journal取消
OVERWRITE=journal書き換え
PAUSE=仮締め
RESUME=仮締めキャンセル
GET_PAUSING_GROUPS=仮締め中グループの取得
CLOSE=締め';
COMMENT ON COLUMN bb.journal_errors.error_type IS 'エラーの種類';
COMMENT ON COLUMN bb.journal_errors.message IS 'エラーメッセージ';
COMMENT ON COLUMN bb.journal_errors.stack_trace IS 'スタックトレース';
COMMENT ON COLUMN bb.journal_errors.sql_state IS 'DBエラーコード';
COMMENT ON COLUMN bb.journal_errors.user_id IS '登録ユーザー';
COMMENT ON COLUMN bb.journal_errors.request IS '登録リクエスト内容
取消処理の場合、{}';
COMMENT ON COLUMN bb.journal_errors.created_at IS '登録時刻';

--===========================
--transient tables
--===========================

--一時作業
CREATE TABLE bb.transients (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id uuid REFERENCES bb.groups ON DELETE CASCADE NOT NULL,
	seq_in_group bigint NOT NULL,
	user_id uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	seq_in_user bigint NOT NULL,
	revision bigint DEFAULT 0 NOT NULL, --以下のテーブルのすべてのrevisionを兼ねる
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	UNIQUE (group_id, seq_in_group),
	UNIQUE (user_id, seq_in_user));

COMMENT ON TABLE bb.transients IS '一時作業';
COMMENT ON COLUMN bb.transients.id IS 'ID';
COMMENT ON COLUMN bb.transients.group_id IS 'この一時作業のオーナーグループ';
COMMENT ON COLUMN bb.transients.seq_in_group IS 'グループ内連番';
COMMENT ON COLUMN bb.transients.user_id IS 'この一時作業のオーナーユーザー';
COMMENT ON COLUMN bb.transients.seq_in_user IS 'ユーザー内連番';
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
CREATE TABLE bb.transient_journals (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	transient_id uuid REFERENCES bb.transients ON DELETE CASCADE NOT NULL, --transientが削除されたら削除
	seq_in_transient bigint NOT NULL,
	group_id uuid REFERENCES bb.groups ON DELETE CASCADE NOT NULL,
	fixed_at timestamptz NOT NULL,
	seq_in_db bigserial NOT NULL, --DB内生成順を保証
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	UNIQUE (transient_id, seq_in_transient));

COMMENT ON TABLE bb.transient_journals IS '一時作業移動伝票';
COMMENT ON COLUMN bb.transient_journals.id IS 'ID';
COMMENT ON COLUMN bb.transient_journals.transient_id IS '一時作業ID';
COMMENT ON COLUMN bb.transient_journals.seq_in_transient IS '一時作業内連番';
COMMENT ON COLUMN bb.transient_journals.group_id IS 'グループID';
COMMENT ON COLUMN bb.transient_journals.fixed_at IS '移動時刻';
COMMENT ON COLUMN bb.transient_journals.seq_in_db IS 'DB内生成順
fixed_atが同一の場合、優先順を決定';
COMMENT ON COLUMN bb.transient_journals.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.transient_journals.tags IS '保存用タグ';
COMMENT ON COLUMN bb.transient_journals.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_journals.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transient_journals.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transient_journals.updated_by IS '更新ユーザー';

--締め済グループチェック
CREATE FUNCTION bb.transient_closed_check() RETURNS TRIGGER AS $$
	DECLARE closed_at_local timestamptz;
	BEGIN
		SELECT INTO closed_at_local closed_at FROM bb.last_closings WHERE id = NEW.group_id;
		IF closed_at_local IS NOT NULL AND NEW.fixed_at < closed_at_local THEN
			RAISE EXCEPTION 'closed_check(): group id=[%] closed at %', NEW.group_id, closed_at_local;
		END IF;
		RETURN NEW;
	END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER transient_closed_checktrigger BEFORE INSERT ON bb.transient_journals
FOR EACH ROW EXECUTE PROCEDURE bb.transient_closed_check();

CREATE TABLE bb.transient_journals_tags (
	id uuid REFERENCES bb.transient_journals ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--一時作業伝票明細
CREATE TABLE bb.transient_details (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	transient_journal_id uuid REFERENCES bb.transient_journals ON DELETE CASCADE NOT NULL,
	seq_in_journal integer NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,-- 編集でtransient_detailsだけ追加することもあるので必要
	created_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL);

COMMENT ON TABLE bb.transient_details IS '一時作業伝票明細';
COMMENT ON COLUMN bb.transient_details.id IS 'ID';
COMMENT ON COLUMN bb.transient_details.transient_journal_id IS '一時作業伝票ID';
COMMENT ON COLUMN bb.transient_details.seq_in_journal IS '伝票内連番';
COMMENT ON COLUMN bb.transient_details.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.transient_details.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_details.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transient_details.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transient_details.updated_by IS '更新ユーザー';

----------

--一時作業移動ノード
CREATE TABLE bb.transient_nodes (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	transient_detail_id uuid REFERENCES bb.transient_details ON DELETE CASCADE NOT NULL,
	unit_id uuid REFERENCES bb.units NOT NULL, --unitは削除されない
	in_out smallint CHECK (in_out IN (1, -1)) NOT NULL, --そのまま計算に使用できるように
	seq_in_detail integer NOT NULL,
	quantity numeric CHECK (quantity >= 0) NOT NULL,
	grants_unlimited boolean DEFAULT false NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL);

COMMENT ON TABLE bb.transient_nodes IS '一時作業伝票ノード';
COMMENT ON COLUMN bb.transient_nodes.id IS 'ID';
COMMENT ON COLUMN bb.transient_nodes.transient_detail_id IS '移動ID';
COMMENT ON COLUMN bb.transient_nodes.unit_id IS '管理対象ID';
COMMENT ON COLUMN bb.transient_nodes.in_out IS '入出庫区分';
COMMENT ON COLUMN bb.transient_nodes.seq_in_detail IS '伝票明細内連番';
COMMENT ON COLUMN bb.transient_nodes.quantity IS '移動数量';
COMMENT ON COLUMN bb.transient_nodes.grants_unlimited IS '数量無制限の許可';
COMMENT ON COLUMN bb.transient_nodes.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb.transient_nodes.created_at IS '作成時刻';
COMMENT ON COLUMN bb.transient_nodes.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb.transient_nodes.updated_at IS '更新時刻';
COMMENT ON COLUMN bb.transient_nodes.updated_by IS '更新ユーザー';

--===========================
--indexes
--===========================

/*
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox_index';
*/

--orgs
CREATE INDEX ON bb.orgs (active);
CREATE INDEX ON bb.orgs (seq);

--groups
CREATE INDEX ON bb.groups (org_id);
CREATE INDEX ON bb.groups (seq);
CREATE INDEX ON bb.groups (active);
--parent_idで検索することはないのでindex不要

--relationships
CREATE INDEX ON bb.relationships (parent_id);

--users
CREATE INDEX ON bb.users (group_id);
CREATE INDEX ON bb.users (seq);
CREATE INDEX ON bb.users (role);
CREATE INDEX ON bb.users (active);

--journals
CREATE INDEX ON bb.journals (group_id);
CREATE INDEX ON bb.journals (seq);
CREATE INDEX ON bb.journals (fixed_at);
CREATE INDEX ON bb.journals (created_at);

--details
CREATE INDEX ON bb.details (journal_id);

--nodes
CREATE INDEX ON bb.nodes (detail_id);
CREATE INDEX ON bb.nodes (unit_id);

--snapshots
CREATE INDEX ON bb.snapshots (in_search_scope);
CREATE INDEX ON bb.snapshots (total);
CREATE INDEX ON bb.snapshots (journal_group_id);
CREATE INDEX ON bb.snapshots (unit_id);
CREATE INDEX ON bb.snapshots (fixed_at);
CREATE INDEX ON bb.snapshots (seq);

--current_units
CREATE INDEX ON bb.current_units (snapshot_id);

--jobs
CREATE INDEX ON bb.jobs (completed);
--worker_idで検索することはないのでindex不要

--transients
CREATE INDEX ON bb.transients (group_id);
CREATE INDEX ON bb.transients (seq_in_group);
CREATE INDEX ON bb.transients (user_id);
CREATE INDEX ON bb.transients (seq_in_user);

--transient_journals
CREATE INDEX ON bb.transient_journals (transient_id);
CREATE INDEX ON bb.transient_journals (seq_in_transient);
CREATE INDEX ON bb.transient_journals (group_id);
CREATE INDEX ON bb.transient_journals (fixed_at);
CREATE INDEX ON bb.transient_journals (seq_in_db);
CREATE INDEX ON bb.transient_journals (created_at);

--transient_details
CREATE INDEX ON bb.transient_details (transient_journal_id);

--transient_nodes
CREATE INDEX ON bb.transient_nodes (transient_detail_id);
CREATE INDEX ON bb.transient_nodes (unit_id);

--tags
CREATE INDEX ON bb.groups_tags (tag_id);
CREATE INDEX ON bb.users_tags (tag_id);
CREATE INDEX ON bb.journals_tags (tag_id);
CREATE INDEX ON bb.transients_tags (tag_id);
CREATE INDEX ON bb.transient_journals_tags (tag_id);

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
	bb.transients,
	bb.transient_journals,
	bb.transient_details,
	bb.transient_nodes
TO blackbox;

GRANT INSERT, UPDATE ON TABLE
	bb.last_closings,
	bb.current_units,
	bb.closed_units,
	bb.snapshots,
	bb.jobs
TO blackbox;

--tag関連はINSERT, DELETEのみ
GRANT INSERT, DELETE ON TABLE
	bb.tags,
	bb.locking_groups,
	bb.groups_tags,
	bb.users_tags,
	bb.journals_tags,
	bb.transients_tags,
	bb.transient_journals_tags
TO blackbox;

--closing, journal関連はINSERTのみ
GRANT INSERT ON TABLE
	bb.closings,
	bb.units,
	bb.journal_batches,
	bb.journals,
	bb.details,
	bb.nodes,
	bb.journal_errors
TO blackbox;
