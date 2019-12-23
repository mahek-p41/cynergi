-- to execute this run as postgres user `psql -f /opt/cyn/v01/cynmid/data/setup-database.sql -v fastinfoUserName= -v fastinfoPassword= -v datasets=
-- Begin fastinfo setup

\c fastinfo_production
SET args.datasets TO :'datasets';

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW company_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'company_vw') THEN
      DROP VIEW company_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            id AS id,
            loc_trans.loc_tran_loc AS number,
            loc_trans.loc_transfer_desc AS name,
            ''' || r.schema_name || ''' AS dataset,
            created_at AT TIME ZONE ''UTC'' AS time_created,
            updated_at AT TIME ZONE ''UTC'' AS time_updated
         FROM ' || r.schema_name || '.level1_loc_trans loc_trans
         WHERE (loc_trans.loc_tran_rec_type = ''4'') AND
         (loc_trans.loc_tran_loc = 0) AND
         loc_trans.loc_transfer_desc IS NOT NULL
      ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionStr VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW department_vw AS';
   unionStr := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'department_vw') THEN
      DROP VIEW department_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionStr || '
         SELECT
            id AS id,
            TRIM(loc_dept_code) AS code,
            loc_dept_desc AS description,
            loc_dept_security_profile AS security_profile,
            loc_dept_default_menu AS default_menu,
            created_at AT TIME ZONE ''UTC'' AS time_created,
            updated_at AT TIME ZONE ''UTC'' AS time_updated
         FROM ' || r.schema_name || '.level2_departments
         WHERE loc_dept_code IS NOT NULL
               AND loc_dept_desc IS NOT NULL
               AND loc_dept_security_profile IS NOT NULL
               AND loc_dept_default_menu IS NOT NULL
      ';

      unionStr := ' UNION ';
   END LOOP;

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW store_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'store_vw') THEN
      DROP VIEW store_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            loc_trans.id AS id,
            loc_trans2.id AS company_id,
            loc_trans.loc_tran_loc AS number,
            loc_trans.loc_transfer_desc AS name,
            ''' || r.schema_name || ''' AS dataset,
            loc_trans.created_at AT TIME ZONE ''UTC'' AS time_created,
            loc_trans.updated_at AT TIME ZONE ''UTC'' AS time_updated
         FROM ' || r.schema_name || '.level1_loc_trans loc_trans
              JOIN ' || r.schema_name || '.level1_loc_trans loc_trans2
                ON loc_trans.loc_tran_company_nbr = loc_trans2.loc_tran_company_nbr
         WHERE loc_trans.loc_tran_rec_type = ''4''
            AND loc_trans.loc_tran_loc = loc_trans.loc_tran_primary_loc
            AND loc_trans2.loc_tran_rec_type = ''4''
            AND loc_trans2.loc_tran_loc = 0
            AND loc_trans.loc_transfer_desc IS NOT NULL
      ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW employee_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'employee_vw') THEN
      DROP VIEW employee_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            id AS id,
            emp_nbr AS number,
            emp_store_nbr AS store_number,
            ''' || r.schema_name || ''' AS dataset,
            emp_last_name AS last_name,
            NULLIF(TRIM(emp_first_name_mi), '''') AS first_name_mi,
            emp_dept AS department,
            TRIM(BOTH FROM
                 CAST(emp_pass_1 AS TEXT) ||
                 CAST(emp_pass_2 AS TEXT) ||
                 CAST(emp_pass_3 AS TEXT) ||
                 CAST(emp_pass_4 AS TEXT) ||
                 CAST(emp_pass_5 AS TEXT) ||
                 CAST(emp_pass_6 AS TEXT)
               ) AS pass_code,
            true AS active,
            created_at AT TIME ZONE ''UTC'' AS time_created,
            updated_at AT TIME ZONE ''UTC'' AS time_updated
         FROM ' || r.schema_name || '.level1_loc_emps
         WHERE emp_nbr IS NOT NULL
               AND TRIM(BOTH FROM
                     CAST(emp_pass_1 AS TEXT) ||
                     CAST(emp_pass_2 AS TEXT) ||
                     CAST(emp_pass_3 AS TEXT) ||
                     CAST(emp_pass_4 AS TEXT) ||
                     CAST(emp_pass_5 AS TEXT) ||
                     CAST(emp_pass_6 AS TEXT)
                   ) <> ''''
      ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY number';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW inventory_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'inventory_vw') THEN
      DROP VIEW inventory_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            inv_recs.id AS id,
            inv_recs.inv_serial_nbr_key AS serial_number,
            CASE
               WHEN LEFT(loc_trans2.loc_tran_strip_dir, 1) = ''B'' THEN inv_recs.inv_alt_id
               ELSE inv_recs.inv_serial_nbr_key
            END AS lookup_key,
            CASE
               WHEN LEFT(loc_trans2.loc_tran_strip_dir, 1) = ''B'' THEN ''BARCODE''
               ELSE ''SERIAL''
            END AS lookup_key_type,
            inv_recs.inv_serial_nbr_key AS barcode,
            inv_recs.inv_alt_id AS alt_id,
            manufacturer.manufile_manu_name AS brand,
            inv_recs.inv_mk_model_nbr AS model_number,
            LEFT(inv_recs.inv_mk_model_nbr, 1) || ''-'' || inv_recs.inv_desc AS product_code,
            inv_recs.inv_desc AS description,
            inv_recs.inv_date_received AS received_date,
            inv_recs.inv_original_cost AS original_cost,
            inv_recs.inv_actual_cost AS actual_cost,
            inv_recs.inv_model_nbr_category AS model_category,
            inv_recs.inv_total_times_rented AS times_rented,
            inv_recs.inv_total_revenue AS total_revenue,
            inv_recs.inv_remain_bk_value AS remaining_value,
            inv_recs.inv_sell_price AS sell_price,
            inv_recs.inv_assigned_value AS assigned_value,
            inv_recs.inv_nbr_idle_days AS idle_days,
            inv_recs.inv_condition AS condition,
            CASE
               WHEN inv_recs.inv_status = ''R'' AND inv_recs.inv_date_returned IS NOT NULL THEN inv_recs.inv_date_returned
               ELSE NULL
            END AS returned_date,
            inv_recs.inv_location_rec_1 AS location,
            inv_recs.inv_status AS status,
            loc_trans.loc_tran_primary_loc AS primary_location,
            loc_trans2.loc_transfer_loc_type AS location_type,
            loc_trans2.created_at AT TIME ZONE ''UTC'' AS time_created,
            inv_recs.updated_at AT TIME ZONE ''UTC'' AS time_updated,
            ''' || r.schema_name || ''' AS dataset
         FROM ' || r.schema_name || '.level1_ninvrecs inv_recs '
      || '     JOIN ' || r.schema_name || '.level1_loc_trans loc_trans ON inv_location_rec_1 = loc_trans.loc_tran_loc '
      || '     JOIN ' || r.schema_name || '.level1_loc_trans loc_trans2 ON loc_trans.loc_tran_primary_loc = loc_trans2.loc_tran_loc '
      || '     LEFT JOIN ' || r.schema_name || '.level1_manufiles manufacturer ON SUBSTRING(inv_mk_model_nbr, 3, 3) = manufile_manu_code_an3
      ';

      unionAll := ' UNION ALL ';
   END LOOP;

   EXECUTE sqlToExec;
END $$;
-- End fastinfo setup

-- Begin cynergidb setup
\c cynergidb
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgres_fdw;
CREATE SCHEMA IF NOT EXISTS fastinfo_prod_import;

DROP SERVER IF EXISTS fastinfo CASCADE;

CREATE SERVER fastinfo
    FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (host 'localhost', dbname 'fastinfo_production', updatable 'false');
CREATE USER MAPPING FOR cynergiuser
    SERVER fastinfo
    OPTIONS (USER :'fastinfoUserName', PASSWORD :'fastinfoPassword');

CREATE FOREIGN TABLE fastinfo_prod_import.department_vw (
  id BIGINT,
  code VARCHAR,
  description VARCHAR,
  security_profile INTEGER,
  default_menu VARCHAR,
  time_created TIMESTAMPTZ,
  time_updated TIMESTAMPTZ
) SERVER fastinfo OPTIONS (TABLE_NAME 'department_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.employee_vw (
   id BIGINT,
   number INTEGER,
   store_number INTEGER,
   dataset VARCHAR,
   last_name VARCHAR,
   first_name_mi VARCHAR,
   department VARCHAR,
   pass_code VARCHAR,
   active BOOLEAN,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ
) SERVER fastinfo OPTIONS (TABLE_NAME 'employee_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.store_vw (
   id BIGINT,
   number INTEGER,
   name VARCHAR,
   dataset VARCHAR,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ
) SERVER fastinfo OPTIONS (TABLE_NAME 'store_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.company_vw (
   id BIGINT,
   number INTEGER,
   company_name VARCHAR,
   dataset VARCHAR,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ
) SERVER fastinfo OPTIONS (TABLE_NAME 'company_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.inventory_vw (
    id BIGINT,
    serial_number VARCHAR,
    lookup_key VARCHAR,
    lookup_key_type TEXT,
    barcode VARCHAR,
    alt_id VARCHAR,
    brand VARCHAR,
    model_number VARCHAR,
    product_code VARCHAR,
    description VARCHAR,
    received_date DATE,
    original_cost NUMERIC,
    actual_cost NUMERIC,
    model_category VARCHAR,
    times_rented INTEGER,
    total_revenue NUMERIC,
    remaining_value NUMERIC,
    sell_price NUMERIC,
    assigned_value NUMERIC,
    idle_days INTEGER,
    condition VARCHAR,
    returned_date DATE,
    location INTEGER,
    status VARCHAR,
    primary_location INTEGER,
    location_type INTEGER,
    time_created TIMESTAMPTZ,
    time_updated TIMESTAMPTZ
) SERVER fastinfo OPTIONS (TABLE_NAME 'inventory_vw', SCHEMA_NAME 'public');

GRANT USAGE ON SCHEMA fastinfo_prod_import TO cynergiuser;
GRANT SELECT ON ALL TABLES IN SCHEMA fastinfo_prod_import TO cynergiuser;
-- End cynergidb setup
