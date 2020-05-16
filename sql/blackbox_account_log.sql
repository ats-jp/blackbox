--===========================
--Blackbox log init DDL
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

--===========================
--log tables
--===========================

/*
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox_log';
*/

DROP TABLE IF EXISTS bb_log.accounts CASCADE;

CREATE TABLE bb_log.accounts (
	id uuid,
	org_id uuid,
	seq bigint,
	code text,
	name text,
	description text,
	type text,
	revision bigint,
	props jsonb,
	tags text[],
	active boolean,
	created_at timestamptz,
	created_by uuid,
	updated_at timestamptz,
	updated_by uuid,
	action "char",
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

DROP FUNCTION IF EXISTS bb_log.accounts_logfunction CASCADE;

CREATE FUNCTION bb_log.accounts_logfunction() RETURNS TRIGGER AS $accounts_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.accounts SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.accounts SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.accounts SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$accounts_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER accounts_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb_account.accounts
FOR EACH ROW EXECUTE PROCEDURE bb_log.accounts_logfunction();

----------

DROP TABLE IF EXISTS bb_log.subaccounts CASCADE;

CREATE TABLE bb_log.subaccounts (
	id uuid,
	account_id uuid,
	seq_in_account bigint,
	code text,
	name text,
	description text,
	revision bigint,
	props jsonb,
	tags text[],
	active boolean,
	created_at timestamptz,
	created_by uuid,
	updated_at timestamptz,
	updated_by uuid,
	action "char",
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

DROP FUNCTION IF EXISTS bb_log.subaccounts_logfunction CASCADE;

CREATE FUNCTION bb_log.subaccounts_logfunction() RETURNS TRIGGER AS $subaccounts_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.subaccounts SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.subaccounts SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.subaccounts SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$subaccounts_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER subaccounts_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb_account.subaccounts
FOR EACH ROW EXECUTE PROCEDURE bb_log.subaccounts_logfunction();

----------

--シーケンス使用権を再付与
GRANT USAGE ON ALL SEQUENCES IN SCHEMA bb_log TO blackbox;

--logはINSERTのみ
GRANT INSERT ON ALL TABLES IN SCHEMA bb_log TO blackbox;

--log系のテーブルはINSERTのみなので、autovacuumは行わない
--ただし、ANALYZEがかからなくなるので、定期的に実施する必要がある
ALTER TABLE bb_log.items SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.owners SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.locations SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.statuses SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
