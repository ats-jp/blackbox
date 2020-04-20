-- DDL patch
-- 0.1 -> 0.2

ALTER TABLE bb.nodes ADD CONSTRAINT seq CHECK (seq <= 999999);

COMMENT ON COLUMN bb.nodes.seq IS '伝票内連番
最大値999999';

DROP TABLE bb.current_units;
DROP TABLE bb.snapshots;

--移動ノード状態
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
一意であり順序として使用できる';
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

--snapshots
CREATE INDEX ON bb.snapshots (in_search_scope);
CREATE INDEX ON bb.snapshots (total);
CREATE INDEX ON bb.snapshots (journal_group_id);
CREATE INDEX ON bb.snapshots (unit_id);
CREATE INDEX ON bb.snapshots (fixed_at);
CREATE INDEX ON bb.snapshots (seq);

--current_units
CREATE INDEX ON bb.current_units (snapshot_id);
