-- setup a simple view for getting employees that can be authenticated against
DO $$
DECLARE
    sqlToExec VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW authenticated_user_vw AS SELECT * FROM (';

   IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name = 'employee_vw' AND table_schema = 'fastinfo_prod_import') THEN
      sqlToExec := sqlToExec || '
         SELECT
            1                               AS from_priority,
            emp.id                          AS id,
            ''sysz''                        AS type,
            emp.number                      AS number,
            emp.active                      AS active,
            false                           AS cynergi_system_admin,
            emp.department                  AS department,
            emp.pass_code                   AS pass_code,
            emp.alternative_store_indicator AS alternative_store_indicator,
            emp.alternative_area            AS alternative_area,
            emp.store_number                AS store_number,
            comp.id                         AS company_id
         FROM company comp
           JOIN fastinfo_prod_import.employee_vw emp ON comp.dataset_code = emp.dataset
         UNION
   ';
   END IF;

   sqlToExec := sqlToExec || '
      SELECT
         2                               AS from_priority,
         emp.id                          AS id,
         ''eli''                         AS type,
         emp.number                      AS number,
         emp.active                      AS active,
         emp.cynergi_system_admin        AS cynergi_system_admin,
         emp.department                  AS department,
         emp.pass_code                   AS pass_code,
         emp.alternative_store_indicator AS alternative_store_indicator,
         emp.alternative_area            AS alternative_area,
         emp.store_number                AS store_number,
         emp.company_id                  AS company_id
      FROM company comp
         JOIN employee emp ON comp.id = emp.company_id
      WHERE active = true
      ORDER BY from_priority, id
   ) AS users ';

   EXECUTE sqlToExec;
END $$;

-- setup view for querying employees
DO $$
DECLARE
    sqlToExec VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW system_employees_vw AS SELECT * FROM ( SELECT * FROM (';

   IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name = 'employee_vw' AND table_schema = 'fastinfo_prod_import') THEN
      sqlToExec := sqlToExec || '
         SELECT
            1                               AS from_priority,
            emp.id                          AS emp_id,
            ''sysz''                        AS emp_type,
            emp.number                      AS emp_number,
            emp.last_name                   AS emp_last_name,
            emp.first_name_mi               AS emp_first_name_mi,
            emp.pass_code                   AS emp_pass_code,
            emp.active                      AS emp_active,
            emp.department                  AS emp_department,
            false                           AS emp_cynergi_system_admin,
            emp.alternative_store_indicator AS emp_alternative_store_indicator,
            emp.alternative_area            AS emp_alternative_area,
            comp.id                         AS comp_id,
            comp.time_created               AS comp_time_created,
            comp.time_updated               AS comp_time_updated,
            comp.name                       AS comp_name,
            comp.doing_business_as          AS comp_doing_business_as,
            comp.client_code                AS comp_client_code,
            comp.client_id                  AS comp_client_id,
            comp.dataset_code               AS comp_dataset_code,
            comp.federal_id_number          AS comp_federal_id_number,
            dept.id                         AS dept_id,
            dept.code                       AS dept_code,
            dept.description                AS dept_description,
            store.id                        AS store_id,
            store.number                    AS store_number,
            store.name                      AS store_name
         FROM fastinfo_prod_import.employee_vw emp
            JOIN company comp ON emp.dataset = comp.dataset_code
            LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
            LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
        UNION';
   END IF;

   sqlToExec := sqlToExec || '
      SELECT
         2                               AS from_priority,
         emp.id                          AS emp_id,
         ''eli''                         AS emp_type,
         emp.number                      AS emp_number,
         emp.last_name                   AS emp_last_name,
         emp.first_name_mi               AS emp_first_name_mi,
         emp.pass_code                   AS emp_pass_code,
         emp.active                      AS emp_active,
         emp.department                  AS emp_department,
         emp.cynergi_system_admin        AS emp_cynergi_system_admin,
         emp.alternative_store_indicator AS emp_alternative_store_indicator,
         emp.alternative_area            AS emp_alternative_area,
         comp.id                         AS comp_id,
         comp.time_created               AS comp_time_created,
         comp.time_updated               AS comp_time_updated,
         comp.name                       AS comp_name,
         comp.doing_business_as          AS comp_doing_business_as,
         comp.client_code                AS comp_client_code,
         comp.client_id                  AS comp_client_id,
         comp.dataset_code               AS comp_dataset_code,
         comp.federal_id_number          AS comp_federal_id_number,
         dept.id                         AS dept_id,
         dept.code                       AS dept_code,
         dept.description                AS dept_description,
         store.id                        AS store_id,
         store.number                    AS store_number,
         store.name                      AS store_name
      FROM employee emp
           JOIN company comp ON emp.company_id = comp.id
           LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
           LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
      ) AS inner_employees
      ORDER BY from_priority
  ) AS system_employees';

   EXECUTE sqlToExec;
END $$;

CREATE MATERIALIZED VIEW system_employees_fimvw AS SELECT * FROM system_employees_vw;
DROP INDEX IF EXISTS sys_emp_mat_vw_number_idx;
DROP INDEX IF EXISTS sys_emp_mat_vw_comp_id;
CREATE INDEX sys_emp_mat_vw_number_idx ON system_employees_fimvw (emp_number);
CREATE INDEX sys_emp_mat_vw_comp_id ON system_employees_fimvw (comp_id);

CREATE MATERIALIZED VIEW system_stores_fimvw AS SELECT * FROM fastinfo_prod_import.store_vw;
CREATE INDEX sys_stores_dataset_idx ON system_stores_fimvw (dataset);
CREATE INDEX sys_stores_number_idx ON system_stores_fimvw (number);

CREATE OR REPLACE FUNCTION refresh_fastinfo_materialized_views_fn() RETURNS VOID AS
$$
DECLARE
   r RECORD;
BEGIN
   FOR r IN SELECT matviewname FROM pg_matviews WHERE matviewname LIKE '%_fimvw'
   LOOP
      EXECUTE 'REFRESH MATERIALIZED VIEW ' || r.matviewname || ' WITH DATA';
   END LOOP;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_employees_fimvw_fn()
   RETURNS TRIGGER AS
$$
BEGIN
   PERFORM refresh_fastinfo_materialized_views_fn();

   RETURN new;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_system_employees_fimvw_trg On employee;

CREATE TRIGGER update_system_employees_fimvw_trg
   AFTER INSERT OR UPDATE
   ON employee
   FOR EACH STATEMENT
   EXECUTE PROCEDURE refresh_employees_fimvw_fn();


-- This function will take the provided pass_code and determine what TEXT string to return based on the type.
-- 'eli' get's you the bcypt hashed version
-- 'sysz' get's you the plain text result trimmed to 6 characters
CREATE OR REPLACE FUNCTION convert_passcode(TEXT, TEXT, TEXT) -- $1: type, $2: pass_code, $3: pass_code-column
   RETURNS TEXT AS
$$
BEGIN
   IF $1 IS NOT NULL AND upper($1) = 'ELI' THEN
      RETURN crypt($2, $3);
   ELSE
      RETURN substring($2, 1, 6); -- this is how cynergi currently does it, and we must continue to support that, because some users type in pass_codes that are longer than allowed, and we just drop the extra characters
   END IF;
END;
$$ LANGUAGE plpgsql;
