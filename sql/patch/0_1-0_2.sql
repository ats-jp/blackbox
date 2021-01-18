-- DDL patch
-- 0.1 -> 0.2

ALTER TABLE bb.nodes ADD CONSTRAINT seq CHECK (seq <= 999999);

COMMENT ON COLUMN bb.nodes.seq IS '�`�[���A��
�ő�l999999';

DROP TABLE bb.current_units;
DROP TABLE bb.snapshots;

--�ړ��m�[�h���
CREATE UNLOGGED TABLE bb.snapshots (
	id uuid PRIMARY KEY REFERENCES bb.nodes,
	unlimited boolean NOT NULL,
	in_search_scope boolean DEFAULT true NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	journal_group_id uuid REFERENCES bb.groups NOT NULL,
	unit_id uuid REFERENCES bb.units NOT NULL,
	fixed_at timestamptz NOT NULL,
	seq char(36) UNIQUE NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	updated_by uuid REFERENCES bb.users ON DELETE CASCADE NOT NULL);
--log�ΏۊO
--WAL�ΏۊO�̂��߁A�N���b�V����journal���畜������K�v����
--�p�ɂɎQ�ƁA�X�V����邱�Ƃ��\�z�����̂Œ��ߍς̃f�[�^�͍폜����

COMMENT ON TABLE bb.snapshots IS '�ړ��m�[�h���
fixed_at���_�ł�unit�̏��';
COMMENT ON COLUMN bb.snapshots.id IS 'ID
nodes.node_id�ɏ]��';
COMMENT ON COLUMN bb.snapshots.unlimited IS '���ʖ�����
true�̏ꍇ�Atotal���}�C�i�X�ł��G���[�ƂȂ�Ȃ�';
COMMENT ON COLUMN bb.snapshots.in_search_scope IS '���ʌ����Ώ�
snapshot�̌����Ώۂ����Ȃ����邱�ƂŒ��ߐ��ʂ̎擾����������������
���߂�ꂽ�ꍇ�A���ߎ����ȉ��̍ŐV��snapshot���N�_�ɒ��O�̍݌ɐ����擾����̂ŁA����ȑO��snapshot��false�ƂȂ�';
COMMENT ON COLUMN bb.snapshots.total IS '���̎��_�̑���';
COMMENT ON COLUMN bb.snapshots.journal_group_id IS '�`�[�̃O���[�vID
�����������̂���journals.group_id�������Ɏ���';
COMMENT ON COLUMN bb.snapshots.unit_id IS '�Ǘ��Ώ�ID
�����������̂���nodes.unit_id�������Ɏ���';
COMMENT ON COLUMN bb.snapshots.fixed_at IS '�m�莞��
�����������̂���journals.fixed_at�������Ɏ���';
COMMENT ON COLUMN bb.snapshots.seq IS '�ړ��m�[�h��Ԃ̓o�^��
�����������̂���fixedAt, created_at, nodes.seq��A���������Ɏ���
��ӂł��菇���Ƃ��Ďg�p�ł���';
COMMENT ON COLUMN bb.snapshots.updated_at IS '�X�V����';
COMMENT ON COLUMN bb.snapshots.updated_by IS '�X�V���[�U�[';

--NULL�̑�p(id=0)
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

--���ݍ݌�
CREATE UNLOGGED TABLE bb.current_units (
	id uuid PRIMARY KEY REFERENCES bb.units, --unit�͍폜����Ȃ��̂�CASCADE�Ȃ�
	unlimited boolean NOT NULL,
	total numeric CHECK (unlimited OR total >= 0) NOT NULL,
	snapshot_id uuid REFERENCES bb.snapshots ON DELETE CASCADE NOT NULL, --snapshot�č쐬�̂���snapshot���폜�����ۂɍ폜
	updated_at timestamptz DEFAULT now() NOT NULL);
--log�ΏۊO
--WAL�ΏۊO�̂��߁A�N���b�V����journals���畜������K�v����
--total�̍X�V�͏�Ƀ|�[�����O��������s���邽��updated_by�������Ȃ�

COMMENT ON TABLE bb.current_units IS '�����_�Ǘ��Ώ�
�Ǘ��Ώۂ̌��ݐ���ێ�';
COMMENT ON COLUMN bb.current_units.id IS 'ID
units.unit_id�ɏ]��';
COMMENT ON COLUMN bb.current_units.unlimited IS '���ʖ�����
true�̏ꍇ�Atotal���}�C�i�X�ł��G���[�ƂȂ�Ȃ�';
COMMENT ON COLUMN bb.current_units.total IS '�����_�̑���';
COMMENT ON COLUMN bb.current_units.snapshot_id IS '�X�i�b�v�V���b�gID
�����_�̐��ʂ�ύX�����`�[';
COMMENT ON COLUMN bb.current_units.updated_at IS '�X�V����';

--snapshots
CREATE INDEX ON bb.snapshots (in_search_scope);
CREATE INDEX ON bb.snapshots (total);
CREATE INDEX ON bb.snapshots (journal_group_id);
CREATE INDEX ON bb.snapshots (unit_id);
CREATE INDEX ON bb.snapshots (fixed_at);
CREATE INDEX ON bb.snapshots (seq);

--current_units
CREATE INDEX ON bb.current_units (snapshot_id);
