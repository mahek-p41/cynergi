WITH security_id AS (
	INSERT INTO security_group (value, description, company_id, deleted)
	SELECT 'admin', 'admin', c.id, FALSE
	FROM company AS c
	RETURNING *
),
access_point AS (
	SELECT * FROM security_access_point_type_domain
),
emp_to_sec AS (
	INSERT INTO employee_to_security_group (employee_id_sfk, security_group_id)
	SELECT e.id, s.id
	FROM employee e
	JOIN security_id s on e.company_id = s.company_id
	WHERE e.number = 998
)
INSERT INTO security_group_to_security_access_point (security_group_id, security_access_point_id)
SELECT security_id.id, access_point.id
FROM security_id, access_point;
