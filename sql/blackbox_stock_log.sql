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

DROP TABLE IF EXISTS bb_log.owners CASCADE;

CREATE TABLE bb_log.owners (
	id uuid,
	group_id uuid,
	seq bigint,
	name text,
	code text,
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

DROP FUNCTION IF EXISTS bb_log.owners_logfunction CASCADE;

CREATE FUNCTION bb_log.owners_logfunction() RETURNS TRIGGER AS $owners_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.owners SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.owners SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.owners SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$owners_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER owners_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb_stock.owners
FOR EACH ROW EXECUTE PROCEDURE bb_log.owners_logfunction();

----------

DROP TABLE IF EXISTS bb_log.items CASCADE;

CREATE TABLE bb_log.items (
	id uuid,
	group_id uuid,
	seq bigint,
	name text,
	code text,
	description text,
	owner_id uuid,
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

DROP FUNCTION IF EXISTS bb_log.items_logfunction CASCADE;

CREATE FUNCTION bb_log.items_logfunction() RETURNS TRIGGER AS $items_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.items SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.items SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.items SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$items_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER items_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb_stock.items
FOR EACH ROW EXECUTE PROCEDURE bb_log.items_logfunction();

----------

DROP TABLE IF EXISTS bb_log.locations CASCADE;

CREATE TABLE bb_log.locations (
	id uuid,
	group_id uuid,
	seq bigint,
	name text,
	code text,
	description text,
	owner_id uuid,
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

DROP FUNCTION IF EXISTS bb_log.locations_logfunction CASCADE;

CREATE FUNCTION bb_log.locations_logfunction() RETURNS TRIGGER AS $locations_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.locations SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.locations SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.locations SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$locations_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER locations_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb_stock.locations
FOR EACH ROW EXECUTE PROCEDURE bb_log.locations_logfunction();

----------

DROP TABLE IF EXISTS bb_log.statuses CASCADE;

CREATE TABLE bb_log.statuses (
	id uuid,
	group_id uuid,
	seq bigint,
	name text,
	code text,
	description text,
	owner_id uuid,
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

DROP FUNCTION IF EXISTS bb_log.statuses_logfunction CASCADE;

CREATE FUNCTION bb_log.statuses_logfunction() RETURNS TRIGGER AS $statuses_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.statuses SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.statuses SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.statuses SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$statuses_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER statuses_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb_stock.statuses
FOR EACH ROW EXECUTE PROCEDURE bb_log.statuses_logfunction();

----------

DROP TABLE IF EXISTS bb_log.formulas CASCADE;

CREATE TABLE bb_log.formulas (
	id uuid,
	group_id uuid,
	seq bigint,
	name text,
	code text,
	description text,
	owner_id uuid,
	revision bigint,
	props jsonb,
	tags text[],
	created_at timestamptz,
	created_by uuid,
	updated_at timestamptz,
	updated_by uuid,
	action "char",
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

DROP FUNCTION IF EXISTS bb_log.formulas_logfunction CASCADE;

CREATE FUNCTION bb_log.formulas_logfunction() RETURNS TRIGGER AS $formulas_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.formulas SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.formulas SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.formulas SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$formulas_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER formulas_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb_stock.formulas
FOR EACH ROW EXECUTE PROCEDURE bb_log.formulas_logfunction();

----------

DROP TABLE IF EXISTS bb_log.formula_details CASCADE;

CREATE TABLE bb_log.formula_details (
	id uuid,
	formula_id uuid,
	stock_id uuid,
	in_out smallint,
	seq integer,
	quantity numeric,
	props jsonb,
	created_at timestamptz,
	created_by uuid,
	updated_at timestamptz,
	updated_by uuid,
	action "char",
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

DROP FUNCTION IF EXISTS bb_log.formula_details_logfunction CASCADE;

CREATE FUNCTION bb_log.formula_details_logfunction() RETURNS TRIGGER AS $formula_details_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.formula_details SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.formula_details SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.formula_details SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$formula_details_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER formula_details_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb_stock.formula_details
FOR EACH ROW EXECUTE PROCEDURE bb_log.formula_details_logfunction();

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
