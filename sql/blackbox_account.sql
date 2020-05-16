--===========================
--Blackbox Account init DDL
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
DROP SCHEMA IF EXISTS bb_account CASCADE;

/*
postgresql role
blackbox_admin Blackbox管理者 SUPERUSER権限
blackbox Blackbox利用者 一般権限
以下のDDLは全てblackbox_adminで実行すること
アプリケーションはblackboxロールで実行することを想定している
外部アプリケーションで既にユーザーが存在する場合は、blackboxと同じGRANT文を実行すること
*/

CREATE SCHEMA bb_account;

COMMENT ON SCHEMA bb_account IS 'Blackbox Account Schema Ver. 0.3';

/*
--postgresql tablespace
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox';
*/

--===========================
--account tables
--===========================

--勘定科目
CREATE TABLE bb_account.accounts (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	org_id uuid REFERENCES bb.orgs NOT NULL,
	seq bigint NOT NULL,
	code text NOT NULL,
	name text NOT NULL,
	type text CHECK (type IN (
		'AS', --Assets
		'LI', --Liabilities
		'EQ', --Equity
		'RE', --Revenue
		'EX') --Expenses
	) NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL,
	UNIQUE (org_id, seq),
	UNIQUE (org_id, code));
--log対象

COMMENT ON TABLE bb_account.accounts IS '勘定科目';
COMMENT ON COLUMN bb_account.accounts.id IS 'ID';
COMMENT ON COLUMN bb_account.accounts.org_id IS '組織ID';
COMMENT ON COLUMN bb_account.accounts.seq IS '組織内連番';
COMMENT ON COLUMN bb_account.accounts.code IS '外部アプリケーション指定コード';
COMMENT ON COLUMN bb_account.accounts.name IS '名称';
COMMENT ON COLUMN bb_account.accounts.type IS '勘定科目分類
AS=資産 (Assets)
LI=負債 (Liabilities)
EQ=純資産 (Equity)
RE=収益 (Revenue)
EX=費用 (Expenses)';
COMMENT ON COLUMN bb_account.accounts.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb_account.accounts.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb_account.accounts.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb_account.accounts.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb_account.accounts.created_at IS '作成時刻';
COMMENT ON COLUMN bb_account.accounts.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb_account.accounts.updated_at IS '更新時刻';
COMMENT ON COLUMN bb_account.accounts.updated_by IS '更新ユーザー';

--省略不可なのでNULL行不要

CREATE TABLE bb_account.accounts_tags (
	id uuid REFERENCES bb_account.accounts ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--小勘定科目
CREATE TABLE bb_account.subaccounts (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	account_id uuid REFERENCES bb_account.accounts NOT NULL,
	seq_in_account bigint NOT NULL,
	code text NOT NULL,
	name text NOT NULL,
	revision bigint DEFAULT 0 NOT NULL,
	props jsonb DEFAULT '{}' NOT NULL,
	tags text[] DEFAULT '{}' NOT NULL,
	active boolean DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users NOT NULL,
	UNIQUE (account_id, seq_in_account),
	UNIQUE (account_id, code));
--log対象

COMMENT ON TABLE bb_account.subaccounts IS '小勘定科目';
COMMENT ON COLUMN bb_account.subaccounts.id IS 'ID';
COMMENT ON COLUMN bb_account.subaccounts.account_id IS '勘定科目ID';
COMMENT ON COLUMN bb_account.subaccounts.seq_in_account IS '勘定科目内連番';
COMMENT ON COLUMN bb_account.subaccounts.code IS '外部アプリケーション指定サブコード';
COMMENT ON COLUMN bb_account.subaccounts.name IS '名称';
COMMENT ON COLUMN bb_account.subaccounts.revision IS 'リビジョン番号';
COMMENT ON COLUMN bb_account.subaccounts.props IS '外部アプリケーション情報JSON';
COMMENT ON COLUMN bb_account.subaccounts.tags IS 'log保存用タグ';
COMMENT ON COLUMN bb_account.subaccounts.active IS 'アクティブフラグ';
COMMENT ON COLUMN bb_account.subaccounts.created_at IS '作成時刻';
COMMENT ON COLUMN bb_account.subaccounts.created_by IS '作成ユーザー';
COMMENT ON COLUMN bb_account.subaccounts.updated_at IS '更新時刻';
COMMENT ON COLUMN bb_account.subaccounts.updated_by IS '更新ユーザー';

--省略不可なのでNULL行不要

CREATE TABLE bb_account.subaccounts_tags (
	id uuid REFERENCES bb_account.subaccounts ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--グループ別勘定科目
CREATE TABLE bb_account.group_accounts (
	id uuid REFERENCES bb.units PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	subaccount_id uuid REFERENCES bb_account.subaccounts NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	UNIQUE (group_id, subaccount_id));
--log対象外
--一度登録されたら変更されない

COMMENT ON TABLE bb_account.group_accounts IS 'グループ別勘定科目';
COMMENT ON COLUMN bb_account.group_accounts.id IS 'ID
管理対象IDに従属';
COMMENT ON COLUMN bb_account.group_accounts.group_id IS 'グループID';
COMMENT ON COLUMN bb_account.group_accounts.subaccount_id IS '小勘定科目ID';
COMMENT ON COLUMN bb_account.group_accounts.created_at IS '作成時刻';
COMMENT ON COLUMN bb_account.group_accounts.created_by IS '作成ユーザー';

--省略不可なのでNULL行不要

--===========================
--indexes
--===========================

/*
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox_index';
*/

--accounts
CREATE INDEX ON bb_account.accounts (org_id);
CREATE INDEX ON bb_account.accounts (seq);
CREATE INDEX ON bb_account.accounts (code);
CREATE INDEX ON bb_account.accounts (active);

--subaccounts
CREATE INDEX ON bb_account.subaccounts (account_id);
CREATE INDEX ON bb_account.subaccounts (active);

--group_accounts
CREATE INDEX ON bb_account.group_accounts (group_id);
CREATE INDEX ON bb_account.group_accounts (subaccount_id);

--tags
CREATE INDEX ON bb_account.accounts_tags (tag_id);
CREATE INDEX ON bb_account.subaccounts_tags (tag_id);

--===========================
--privileges
--===========================

--スキーマ使用権を付与
GRANT USAGE ON SCHEMA bb_account TO blackbox;

--シーケンス使用権を付与
GRANT USAGE ON ALL SEQUENCES IN SCHEMA bb_account TO blackbox;

--全テーブルSELECT可能
GRANT SELECT ON ALL TABLES IN SCHEMA bb_account TO blackbox;

GRANT INSERT, UPDATE, DELETE ON TABLE
	bb_account.accounts,
	bb_account.subaccounts
TO blackbox;

--tag関連はINSERT, DELETEのみ
GRANT INSERT, DELETE ON TABLE
	bb_account.accounts_tags,
	bb_account.subaccounts_tags
TO blackbox;

--group_accountsはINSERTのみ
GRANT INSERT ON TABLE
	bb_account.group_accounts
TO blackbox;
