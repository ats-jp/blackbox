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

--開発環境等デフォルトtablespaceのままCREATEする場合、この行をコメントアウト
SET default_tablespace = 'blackbox_log';

CREATE TABLE bb_log.orgs (
	id bigint,
	name text,
	revision bigint,
	extension jsonb,
	active boolean,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
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
	id bigint,
	org_id bigint,
	name text,
	parent_id bigint,
	revision bigint,
	extension jsonb,
	active boolean,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
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
	id bigint,
	group_id bigint,
	name text,
	role smallint,
	revision bigint,
	extension jsonb,
	active boolean,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
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

CREATE TABLE bb_log.items (
	id bigint,
	group_id bigint,
	name text,
	revision bigint,
	extension jsonb,
	active boolean,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

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

CREATE TRIGGER items_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.items
FOR EACH ROW EXECUTE PROCEDURE bb_log.items_logfunction();

----------

CREATE TABLE bb_log.owners (
	id bigint,
	group_id bigint,
	name text,
	revision bigint,
	extension jsonb,
	active boolean,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

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

CREATE TRIGGER owners_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.owners
FOR EACH ROW EXECUTE PROCEDURE bb_log.owners_logfunction();

----------

CREATE TABLE bb_log.locations (
	id bigint,
	group_id bigint,
	name text,
	revision bigint,
	extension jsonb,
	active boolean,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

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

CREATE TRIGGER locations_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.locations
FOR EACH ROW EXECUTE PROCEDURE bb_log.locations_logfunction();

----------

CREATE TABLE bb_log.statuses (
	id bigint,
	group_id bigint,
	name text,
	revision bigint,
	extension jsonb,
	active boolean,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

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

CREATE TRIGGER statuses_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.statuses
FOR EACH ROW EXECUTE PROCEDURE bb_log.statuses_logfunction();

----------

CREATE TABLE bb_log.jobs (
	id bigint,
	starts_at timestamptz,
	completed boolean,
	trigger_id bigint,
	parameter jsonb,
	revision bigint,
	created_at timestamptz DEFAULT now(),
	created_by bigint REFERENCES bb.users,
	updated_at timestamptz DEFAULT now(),
	updated_by bigint REFERENCES bb.users,
	action "char" CHECK (action IN ('I', 'U', 'D')),
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

CREATE FUNCTION bb_log.jobs_logfunction() RETURNS TRIGGER AS $jobs_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.jobs SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.jobs SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.jobs SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$jobs_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER jobs_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.jobs
FOR EACH ROW EXECUTE PROCEDURE bb_log.jobs_logfunction();

----------

CREATE TABLE bb_log.transients (
	id bigint,
	group_id bigint,
	user_id bigint,
	revision bigint,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
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

CREATE TABLE bb_log.transient_transfers (
	id bigint,
	transient_id bigint,
	group_id bigint,
	transferred_at timestamptz,
	extension jsonb,
	completed boolean,
	trigger_id bigint,
	parameter jsonb,
	revision bigint,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

CREATE FUNCTION bb_log.transient_transfers_logfunction() RETURNS TRIGGER AS $transient_transfers_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.transient_transfers SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.transient_transfers SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.transient_transfers SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$transient_transfers_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER transient_transfers_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.transient_transfers
FOR EACH ROW EXECUTE PROCEDURE bb_log.transient_transfers_logfunction();

----------

CREATE TABLE bb_log.transient_bundles (
	id bigint,
	transfer_branch_id bigint,
	extension jsonb,
	created_at timestamptz,
	created_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
	log_id bigserial PRIMARY KEY,
	txid bigint DEFAULT txid_current(),
	logged_by name DEFAULT current_user,
	logged_at timestamptz DEFAULT now());

CREATE FUNCTION bb_log.transient_bundles_logfunction() RETURNS TRIGGER AS $transient_bundles_logtrigger$
	BEGIN
		IF (TG_OP = 'DELETE') THEN
			INSERT INTO bb_log.transient_bundles SELECT OLD.*, 'D';
			RETURN OLD;
		ELSIF (TG_OP = 'UPDATE') THEN
			INSERT INTO bb_log.transient_bundles SELECT NEW.*, 'U';
			RETURN NEW;
		ELSIF (TG_OP = 'INSERT') THEN
			INSERT INTO bb_log.transient_bundles SELECT NEW.*, 'I';
			RETURN NEW;
		END IF;
		RETURN NULL;
	END;
$transient_bundles_logtrigger$ LANGUAGE plpgsql;

CREATE TRIGGER transient_bundles_logtrigger AFTER INSERT OR UPDATE OR DELETE ON bb.transient_bundles
FOR EACH ROW EXECUTE PROCEDURE bb_log.transient_bundles_logfunction();

----------

CREATE TABLE bb_log.transient_nodes (
	id bigint,
	transient_bundle_id bigint,
	transient_stock_id bigint,
	in_out "char",
	quantity numeric,
	extension jsonb,
	revision bigint,
	created_at timestamptz,
	created_by bigint,
	updated_at timestamptz,
	updated_by bigint,
	action "char" CHECK (action IN ('I', 'U', 'D')),
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
ALTER TABLE bb_log.items SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.owners SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.locations SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.statuses SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.jobs SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.transients SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.transient_transfers SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.transient_bundles SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE bb_log.transient_nodes SET (autovacuum_enabled = false, toast.autovacuum_enabled = false);

--===========================
--privileges
--===========================

--logはSELECT, INSERTのみ
GRANT SELECT, INSERT ON ALL TABLES IN SCHEMA bb_log TO blackbox;
