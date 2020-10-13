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

--全削除
DROP SCHEMA IF EXISTS bb_log CASCADE;

--===========================
--log tables
--===========================

create SCHEMA bb_log;

COMMENT ON SCHEMA bb_log IS 'Blackbox Log Schema';

/*
--開発環境、クラウド環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox_log';
*/

CREATE TABLE bb_log.orgs (
	id uuid,
	instance_id uuid,
	seq bigint,
	name text,
	description text,
	revision bigint,
	props jsonb,
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

CREATE FUNCTION bb_log.orgs_logfunction() RETURNS TRIGGER AS $orgs_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.orgs SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.orgs SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.orgs SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$orgs_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER orgs_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.orgs
FOR EACH ROW EXECUTE PROCEDURE bb_log.orgs_logfunction();

----------

CREATE TABLE bb_log.groups (
	id uuid,
	org_id uuid,
	seq bigint,
	name text,
	description text,
	parent_id uuid,
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

CREATE FUNCTION bb_log.groups_logfunction() RETURNS TRIGGER AS $groups_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.groups SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.groups SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.groups SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$groups_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER groups_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.groups
FOR EACH ROW EXECUTE PROCEDURE bb_log.groups_logfunction();

----------

CREATE TABLE bb_log.users (
	id uuid,
	group_id uuid,
	seq bigint,
	name text,
	description text,
	role smallint,
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

CREATE FUNCTION bb_log.users_logfunction() RETURNS TRIGGER AS $users_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.users SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.users SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.users SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$users_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER users_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.users
FOR EACH ROW EXECUTE PROCEDURE bb_log.users_logfunction();

----------

CREATE TABLE bb_log.closings (
	id uuid,
	group_id uuid,
	description text,
	seq bigint,
	closed_at timestamptz,
	props jsonb,
	created_at timestamptz,
	created_by uuid,
	action "char",
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

CREATE FUNCTION bb_log.closings_logfunction() RETURNS TRIGGER AS $closings_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.closings SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.closings SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.closings SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$closings_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER closings_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.closings
FOR EACH ROW EXECUTE PROCEDURE bb_log.closings_logfunction();

----------

CREATE TABLE bb_log.transients (
	id uuid,
	group_id uuid,
	seq_in_group bigint,
	user_id uuid,
	seq_in_user bigint,
	description text,
	revision bigint,
	created_at timestamptz,
	created_by uuid,
	updated_at timestamptz,
	updated_by uuid,
	action "char",
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

CREATE FUNCTION bb_log.transients_logfunction() RETURNS TRIGGER AS $transients_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.transients SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.transients SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.transients SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$transients_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER transients_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.transients
FOR EACH ROW EXECUTE PROCEDURE bb_log.transients_logfunction();

----------

CREATE TABLE bb_log.transient_journals (
	id uuid,
	transient_id uuid,
	seq_in_transient bigint,
	group_id uuid,
	fixed_at timestamptz,
	description text,
	seq_in_db bigint,
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

CREATE FUNCTION bb_log.transient_journals_logfunction() RETURNS TRIGGER AS $transient_journals_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.transient_journals SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.transient_journals SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.transient_journals SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$transient_journals_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER transient_journals_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.transient_journals
FOR EACH ROW EXECUTE PROCEDURE bb_log.transient_journals_logfunction();

----------

CREATE TABLE bb_log.transient_details (
	id uuid,
	transient_journal_id uuid,
	seq_in_journal integer,
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

CREATE FUNCTION bb_log.transient_details_logfunction() RETURNS TRIGGER AS $transient_details_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.transient_details SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.transient_details SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.transient_details SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$transient_details_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER transient_details_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.transient_details
FOR EACH ROW EXECUTE PROCEDURE bb_log.transient_details_logfunction();

----------

CREATE TABLE bb_log.transient_nodes (
	id uuid,
	transient_detail_id uuid,
	unit_id uuid,
	in_out smallint,
	seq_in_detail integer,
	quantity numeric,
	grants_unlimited boolean,
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

CREATE FUNCTION bb_log.transient_nodes_logfunction() RETURNS TRIGGER AS $transient_nodes_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.transient_nodes SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.transient_nodes SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.transient_nodes SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$transient_nodes_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER transient_nodes_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.transient_nodes
FOR EACH ROW EXECUTE PROCEDURE bb_log.transient_nodes_logfunction();

----------

--stocks, snapshotsはlog不要

--log系のテーブルはINSERTのみなので、autovacuumは行わない
--ただし、ANALYZEがかからなくなるので、定期的に実施する必要がある
ALTER TABLE bb_log.orgs SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.groups SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.users SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.closings SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.transients SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.transient_journals SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.transient_details SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.transient_nodes SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);

--===========================
--privileges
--===========================

--スキーマ使用権を付与
GRANT USAGE ON SCHEMA bb_log TO blackbox;

--シーケンス使用権を付与
GRANT USAGE ON ALL SEQUENCES IN SCHEMA bb_log TO blackbox;

--logはINSERTのみ
GRANT INSERT ON ALL TABLES IN SCHEMA bb_log TO blackbox;
