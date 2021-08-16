SELECT
	g.id AS group_id,
	o.id AS org_id
FROM
	bb.groups g
JOIN
	bb.orgs o
ON
	g.org_id = o.id
WHERE
	g.id = ${id}
