-- 全削除
DROP DATABASE IF EXISTS blackbox;
DROP TABLESPACE IF EXISTS blackbox_index;
DROP TABLESPACE IF EXISTS blackbox_log;
DROP TABLESPACE IF EXISTS blackbox;
DROP USER IF EXISTS blackbox;
DROP USER IF EXISTS blackbox_admin;

CREATE USER blackbox_admin WITH
	LOGIN
	SUPERUSER
	NOCREATEDB
	NOCREATEROLE
	NOINHERIT
	NOREPLICATION
	CONNECTION LIMIT -1;
-- passwordはpgAdminで設定すること

CREATE USER blackbox WITH
	LOGIN
	NOSUPERUSER
	NOCREATEDB
	NOCREATEROLE
	NOINHERIT
	NOREPLICATION
	CONNECTION LIMIT -1;
-- passwordはpgAdminで設定すること

-- CREATE TABLESPACE

-- アプリケーション用
CREATE TABLESPACE blackbox
	OWNER blackbox_admin
	LOCATION 'C:\ats_chiba\blackbox_tablespaces\blackbox'; --絶対パスで指定すること

-- logスキーマ用
CREATE TABLESPACE blackbox_log
	OWNER blackbox_admin
	LOCATION 'C:\ats_chiba\blackbox_tablespaces\blackbox_log'; --絶対パスで指定すること

-- index用
CREATE TABLESPACE blackbox_index
	OWNER blackbox_admin
	LOCATION 'C:\ats_chiba\blackbox_tablespaces\blackbox_index'; --絶対パスで指定すること

-- CREATE DATABASE

CREATE DATABASE blackbox WITH
	OWNER = blackbox_admin
	ENCODING = 'UTF8'
	TABLESPACE = blackbox
	CONNECTION LIMIT = -1;
