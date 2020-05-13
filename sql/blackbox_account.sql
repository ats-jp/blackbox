--===========================
--Blackbox Account init DDL
--===========================

/*
����DDL�����s���邽�߂ɕK�v�Ȃ���
role
	blackbox_admin Blackbox�Ǘ��� SUPERUSER����
	blackbox Blackbox���p�� ��ʌ���
tablespace
	blackbox
		Blackbox�A�v���P�[�V�����p
		owner: blackbox
	blackbox_index
		Blackbox index�p
		owner: blackbox
	log
		Blackbox log�p
		owner: blackbox
database
	blackbox
		Blackbox�A�v���P�[�V�����p�Atablespace��blackbox�ɂ���
		owner: blackbox
*/

--�S�폜
DROP SCHEMA IF EXISTS bb_account CASCADE;

/*
postgresql role
blackbox_admin Blackbox�Ǘ��� SUPERUSER����
blackbox Blackbox���p�� ��ʌ���
�ȉ���DDL�͑S��blackbox_admin�Ŏ��s���邱��
�A�v���P�[�V������blackbox���[���Ŏ��s���邱�Ƃ�z�肵�Ă���
�O���A�v���P�[�V�����Ŋ��Ƀ��[�U�[�����݂���ꍇ�́Ablackbox�Ɠ���GRANT�������s���邱��
*/

CREATE SCHEMA bb_account;

COMMENT ON SCHEMA bb_account IS 'Blackbox Account Schema Ver. 0.3';

/*
--postgresql tablespace
--�J�����A�N���E�h�����f�t�H���gtablespace�̂܂�CREATE����ꍇ�A���̍s���R�����g�A�E�g
SET default_tablespace = 'blackbox';
*/

--===========================
--account tables
--===========================

--����Ȗ�
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
--log�Ώ�

COMMENT ON TABLE bb_account.accounts IS '����Ȗ�';
COMMENT ON COLUMN bb_account.accounts.id IS 'ID';
COMMENT ON COLUMN bb_account.accounts.org_id IS '�g�DID';
COMMENT ON COLUMN bb_account.accounts.seq IS '�g�D���A��';
COMMENT ON COLUMN bb_account.accounts.code IS '�O���A�v���P�[�V�����w��R�[�h';
COMMENT ON COLUMN bb_account.accounts.name IS '����';
COMMENT ON COLUMN bb_account.accounts.type IS '����Ȗڕ���
AS=���Y (Assets)
LI=���� (Liabilities)
EQ=�����Y (Equity)
RE=���v (Revenue)
EX=��p (Expenses)';
COMMENT ON COLUMN bb_account.accounts.revision IS '���r�W�����ԍ�';
COMMENT ON COLUMN bb_account.accounts.props IS '�O���A�v���P�[�V�������JSON';
COMMENT ON COLUMN bb_account.accounts.tags IS 'log�ۑ��p�^�O';
COMMENT ON COLUMN bb_account.accounts.active IS '�A�N�e�B�u�t���O';
COMMENT ON COLUMN bb_account.accounts.created_at IS '�쐬����';
COMMENT ON COLUMN bb_account.accounts.created_by IS '�쐬���[�U�[';
COMMENT ON COLUMN bb_account.accounts.updated_at IS '�X�V����';
COMMENT ON COLUMN bb_account.accounts.updated_by IS '�X�V���[�U�[';

--�ȗ��s�Ȃ̂�NULL�s�s�v

CREATE TABLE bb_account.accounts_tags (
	id uuid REFERENCES bb_account.accounts ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--������Ȗ�
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
--log�Ώ�

COMMENT ON TABLE bb_account.subaccounts IS '������Ȗ�';
COMMENT ON COLUMN bb_account.subaccounts.id IS 'ID';
COMMENT ON COLUMN bb_account.subaccounts.account_id IS '����Ȗ�ID';
COMMENT ON COLUMN bb_account.subaccounts.seq_in_account IS '����Ȗړ��A��';
COMMENT ON COLUMN bb_account.subaccounts.code IS '�O���A�v���P�[�V�����w��T�u�R�[�h';
COMMENT ON COLUMN bb_account.subaccounts.name IS '����';
COMMENT ON COLUMN bb_account.subaccounts.revision IS '���r�W�����ԍ�';
COMMENT ON COLUMN bb_account.subaccounts.props IS '�O���A�v���P�[�V�������JSON';
COMMENT ON COLUMN bb_account.subaccounts.tags IS 'log�ۑ��p�^�O';
COMMENT ON COLUMN bb_account.subaccounts.active IS '�A�N�e�B�u�t���O';
COMMENT ON COLUMN bb_account.subaccounts.created_at IS '�쐬����';
COMMENT ON COLUMN bb_account.subaccounts.created_by IS '�쐬���[�U�[';
COMMENT ON COLUMN bb_account.subaccounts.updated_at IS '�X�V����';
COMMENT ON COLUMN bb_account.subaccounts.updated_by IS '�X�V���[�U�[';

--�ȗ��s�Ȃ̂�NULL�s�s�v

CREATE TABLE bb_account.subaccounts_tags (
	id uuid REFERENCES bb_account.subaccounts ON DELETE CASCADE NOT NULL,
	tag_id uuid REFERENCES bb.tags ON DELETE CASCADE NOT NULL,
	UNIQUE (id, tag_id));

----------

--�O���[�v�ʊ���Ȗ�
CREATE TABLE bb_account.group_accounts (
	id uuid REFERENCES bb.units PRIMARY KEY,
	group_id uuid REFERENCES bb.groups NOT NULL,
	subaccount_id uuid REFERENCES bb_account.subaccounts NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	created_by uuid REFERENCES bb.users NOT NULL,
	UNIQUE (group_id, subaccount_id));
--log�ΏۊO
--��x�o�^���ꂽ��ύX����Ȃ�

COMMENT ON TABLE bb_account.group_accounts IS '�O���[�v�ʊ���Ȗ�';
COMMENT ON COLUMN bb_account.group_accounts.id IS 'ID
�Ǘ��Ώ�ID�ɏ]��';
COMMENT ON COLUMN bb_account.group_accounts.group_id IS '�O���[�vID';
COMMENT ON COLUMN bb_account.group_accounts.subaccount_id IS '������Ȗ�ID';
COMMENT ON COLUMN bb_account.group_accounts.created_at IS '�쐬����';
COMMENT ON COLUMN bb_account.group_accounts.created_by IS '�쐬���[�U�[';

--�ȗ��s�Ȃ̂�NULL�s�s�v

--===========================
--indexes
--===========================

/*
--�J�����A�N���E�h�����f�t�H���gtablespace�̂܂�CREATE����ꍇ�A���̍s���R�����g�A�E�g
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

--�X�L�[�}�g�p����t�^
GRANT USAGE ON SCHEMA bb_account TO blackbox;

--�V�[�P���X�g�p����t�^
GRANT USAGE ON ALL SEQUENCES IN SCHEMA bb_account TO blackbox;

--�S�e�[�u��SELECT�\
GRANT SELECT ON ALL TABLES IN SCHEMA bb_account TO blackbox;

GRANT INSERT, UPDATE, DELETE ON TABLE
	bb_account.accounts,
	bb_account.subaccounts
TO blackbox;

--tag�֘A��INSERT, DELETE�̂�
GRANT INSERT, DELETE ON TABLE
	bb_account.accounts_tags,
	bb_account.subaccounts_tags
TO blackbox;

--group_accounts��INSERT�̂�
GRANT INSERT ON TABLE
	bb_account.group_accounts
TO blackbox;
