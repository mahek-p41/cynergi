CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- create wrapper function for hashing passwords
CREATE OR REPLACE FUNCTION hash_passcode(TEXT)
    RETURNS TEXT AS
$$
BEGIN
    IF $1 IS NOT NULL AND length($1) > 2 THEN
        RETURN crypt($1, gen_salt('bf', 10));
    ELSE
        RAISE EXCEPTION 'Pass code provided does not meet length requirement of 3';
    END IF;
END;
$$ LANGUAGE plpgsql;

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
            address.id                      AS address_id,
            address.name                    AS address_name,
            address.address1                AS address_address1,
            address.address2                AS address_address2,
            address.city                    AS address_city,
            address.state                   AS address_state,
            address.postal_code             AS address_postal_code,
            address.latitude                AS address_latitude,
            address.longitude               AS address_longitude,
            address.country                 AS address_country,
            address.county                  AS address_county,
            address.phone                   AS address_phone,
            address.fax                     AS address_fax,
            dept.id                         AS dept_id,
            dept.code                       AS dept_code,
            dept.description                AS dept_description,
            store.id                        AS store_id,
            store.number                    AS store_number,
            store.name                      AS store_name
         FROM fastinfo_prod_import.employee_vw emp
            JOIN company comp ON emp.dataset = comp.dataset_code
            LEFT JOIN address ON comp.address_id = address.id
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
         address.id                      AS address_id,
         address.name                    AS address_name,
         address.address1                AS address_address1,
         address.address2                AS address_address2,
         address.city                    AS address_city,
         address.state                   AS address_state,
         address.postal_code             AS address_postal_code,
         address.latitude                AS address_latitude,
         address.longitude               AS address_longitude,
         address.country                 AS address_country,
         address.county                  AS address_county,
         address.phone                   AS address_phone,
         address.fax                     AS address_fax,
         dept.id                         AS dept_id,
         dept.code                       AS dept_code,
         dept.description                AS dept_description,
         store.id                        AS store_id,
         store.number                    AS store_number,
         store.name                      AS store_name
      FROM employee emp
           JOIN company comp ON emp.company_id = comp.id
           LEFT JOIN address ON comp.address_id = address.id
           LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
           LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
      ) AS inner_employees
      ORDER BY from_priority
  ) AS system_employees';

   EXECUTE sqlToExec;
END $$;
