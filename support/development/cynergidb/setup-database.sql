-- to execute this run as postgres user `psql -f /opt/cyn/v01/cynmid/data/setup-database.sql -v fastinfoUserName= -v fastinfoPassword= -v datasets=
-- Begin fastinfo setup

\c fastinfo_production
SET args.datasets TO :'datasets';

CREATE OR REPLACE FUNCTION isnumeric(text) RETURNS BOOLEAN AS $$
DECLARE x NUMERIC;
BEGIN
    x = $1::NUMERIC;
    RETURN TRUE;
EXCEPTION WHEN others THEN
    RETURN FALSE;
END;
$$
    STRICT
    LANGUAGE plpgsql IMMUTABLE;

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
            ''' || r.schema_name || '''::text AS dataset
         FROM ' || r.schema_name || '.level2_departments
         WHERE loc_dept_code IS NOT NULL
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
            id                                AS id,
            ''' || r.schema_name || '''::text AS dataset,
            loc_tran_loc                      AS number,
            loc_transfer_desc                 AS name
         FROM ' || r.schema_name || '.level2_stores
         WHERE loc_transfer_desc IS NOT NULL
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
            employee.id                                                                                    AS id,
            ''' || r.schema_name || '''::text                                                              AS dataset,
            employee.created_at AT TIME ZONE ''UTC''                                                       AS time_created,
            employee.updated_at AT TIME ZONE ''UTC''                                                       AS time_updated,
            emp_nbr                                                                                        AS number,
            emp_last_name                                                                                  AS last_name,
            NULLIF(TRIM(emp_first_name_mi), '''')                                                          AS first_name_mi,
            TRIM(BOTH FROM CONCAT(emp_pass_1, emp_pass_2, emp_pass_3, emp_pass_4, emp_pass_5, emp_pass_6)) AS pass_code,
            emp_store_nbr                                                                                  AS store_number,
            emp_termination_date IS NULL                                                                   AS active,
            dept.loc_dept_code                                                                             AS department,
            FALSE                                                                                          AS cynergi_system_admin,
            emp_alt_store_indr                                                                             AS alternative_store_indicator,
            emp_alt_area                                                                                   AS alternative_area
         FROM ' || r.schema_name || '.level2_employees employee
              JOIN ' || r.schema_name || '.level2_departments dept ON employee.department_id = dept.id
         WHERE emp_nbr IS NOT NULL
               AND TRIM(BOTH FROM
                  CONCAT(emp_pass_1, emp_pass_2, emp_pass_3, emp_pass_4, emp_pass_5, emp_pass_6)
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
            inventory.id                                                                                                             AS id,
            ''' || r.schema_name || '''::text                                                                                        AS dataset,
            inventory.inv_serial_nbr_key                                                                                             AS serial_number,
            CASE
               WHEN LEFT(location.loc_tran_strip_dir, 1) = ''B'' THEN inventory.inv_alt_id
               ELSE inventory.inv_serial_nbr_key
            END                                                                                                                      AS lookup_key,
            CASE
               WHEN LEFT(location.loc_tran_strip_dir, 1) = ''B'' THEN ''BARCODE''
               ELSE ''SERIAL''
            END                                                                                                                      AS lookup_key_type,
            inventory.inv_serial_nbr_key                                                                                             AS barcode,
            inventory.inv_alt_id                                                                                                     AS alt_id,
            manufacturer.manufile_manu_name                                                                                          AS brand,
            inventory.inv_model_nbr_manufacturer                                                                                     AS model_number,
            LEFT(inventory.inv_model_nbr_manufacturer, 1) || ''-'' || inventory.inv_desc                                             AS product_code,
            inventory.inv_desc                                                                                                       AS description,
            inventory.inv_date_received                                                                                              AS received_date,
            inventory.inv_original_cost                                                                                              AS original_cost,
            inventory.inv_actual_cost                                                                                                AS actual_cost,
            inventory.inv_model_nbr_category                                                                                         AS model_category,
            inventory.inv_total_times_rented                                                                                         AS times_rented,
            inventory.inv_total_revenue                                                                                              AS total_revenue,
            inventory.inv_remain_bk_value                                                                                            AS remaining_value,
            inventory.inv_sell_price                                                                                                 AS sell_price,
            inventory.inv_assigned_value                                                                                             AS assigned_value,
            inventory.inv_nbr_idle_days                                                                                              AS idle_days,
            inventory.inv_condition                                                                                                  AS condition,
            inventory.inventory_status_id                                                                                            AS status_id,
            inventory.model_id                                                                                                       AS model_id,
            CASE
               WHEN inventoryStatus.status_code = ''R'' AND inventory.inv_date_returned IS NOT NULL THEN inventory.inv_date_returned
            END                                                                                                                      AS returned_date,
            inventory.inv_location_rec_1                                                                                             AS location,
            inventoryStatus.status_code                                                                                              AS status,
            store.id                                                                                                                 AS store_id,
            store.loc_tran_loc                                                                                                       AS primary_location,
            locationType.location_type_code                                                                                          AS location_type
         FROM ' || r.schema_name || '.level2_inventories inventory
              JOIN ' || r.schema_name || '.level2_inventory_statuses inventoryStatus ON inventory.inventory_status_id = inventoryStatus.id
              JOIN ' || r.schema_name || '.level2_models model ON inventory.model_id = model.id
              JOIN ' || r.schema_name || '.level2_manufacturers manufacturer ON model.manufacturer_id = manufacturer.id
              JOIN ' || r.schema_name || '.level2_locations location ON inventory.location_id = location.id
              JOIN ' || r.schema_name || '.level2_location_types locationType ON location.location_type_id = locationType.id
              JOIN ' || r.schema_name || '.level2_stores store ON location.store_id = store.id
      ';

      unionAll := ' UNION ALL ';
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
   sqlToExec := 'CREATE OR REPLACE VIEW itemfile_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'itemfile_vw') THEN
      DROP VIEW itemfile_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            itemfile.id                              AS id,
            ''' || r.schema_name || '''::text        AS dataset,
            itemfile.created_at AT TIME ZONE ''UTC'' AS time_created,
            itemfile.updated_at AT TIME ZONE ''UTC'' AS time_updated,
            itemfile.itemfile_nbr                    AS number,
            itemfile.itemfile_desc_1                 AS description_1,
            itemfile.itemfile_desc_2                 AS description_2,
            itemfile.itemfile_discontinued_indr      AS discontinued_indicator,
            vendors.vend_number                      AS vendor_number
         FROM ' || r.schema_name || '.level2_models itemfile
              JOIN ' || r.schema_name || '.level2_vendors vendors ON itemfile.vendor_id = vendors.id
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
   sqlToExec := 'CREATE OR REPLACE VIEW customer_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'customer_vw') THEN
      DROP VIEW customer_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            customer.id                              AS id,
            ''' || r.schema_name || '''::text        AS dataset,
            customer.created_at AT TIME ZONE ''UTC'' AS time_created,
            customer.updated_at AT TIME ZONE ''UTC'' AS time_updated,
            customer.cust_acct_nbr                   AS number,
            customer.cust_first_name_mi              AS first_name_mi,
            customer.cust_last_name                  AS last_name
         FROM ' || r.schema_name || '.level2_customers customer
         WHERE cust_first_name_mi IS NOT NULL
               AND cust_last_name IS NOT NULL
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
   sqlToExec := 'CREATE OR REPLACE VIEW operator_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'operator_vw') THEN
      DROP VIEW operator_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT DISTINCT ON (operator_name)
            operator.id                              AS id,
            ''' || r.schema_name || '''::text        AS dataset,
            operator.created_at AT TIME ZONE ''UTC'' AS time_created,
            operator.updated_at AT TIME ZONE ''UTC'' AS time_updated,
            operator.operator_name                   AS name,
            operator.operator_security_2             AS account_payable_security,
            operator.operator_security_4             AS purchase_order_security,
            operator.operator_security_5             AS general_ledger_security,
            operator.operator_security_10            AS system_administration_security,
            operator.operator_security_11            AS file_maintenance_security,
            operator.operator_security_16            AS bank_reconciliation_security,
            CAST(operator_name AS INTEGER)           AS number
         FROM ' || r.schema_name || '.level1_operators operator
         INNER JOIN employee_vw employee ON CAST(operator.operator_name AS INTEGER) = employee.number AND ''' || r.schema_name || '''::text = employee.dataset
         WHERE operator_type = ''O'' AND isnumeric(operator.operator_name)
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY name, id DESC';

   EXECUTE sqlToExec;
END $$;
-- End fastinfo setup

-- Begin cynergidb setup
\c cynergidb
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
CREATE EXTENSION IF NOT EXISTS postgres_fdw;
CREATE SCHEMA IF NOT EXISTS fastinfo_prod_import;

DROP SERVER IF EXISTS fastinfo CASCADE;

CREATE SERVER fastinfo
    FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (host 'localhost', dbname 'fastinfo_production', updatable 'false');
CREATE USER MAPPING FOR cynergiuser
    SERVER fastinfo
    OPTIONS (USER :'fastinfoUserName', PASSWORD :'fastinfoPassword');

CREATE FOREIGN TABLE fastinfo_prod_import.store_vw (
    id BIGINT,
    dataset VARCHAR,
    number INTEGER,
    name VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'store_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.department_vw (
  id BIGINT,
  dataset VARCHAR,
  code VARCHAR,
  description VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'department_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.employee_vw (
   id BIGINT,
   dataset VARCHAR,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   number INTEGER,
   last_name VARCHAR,
   first_name_mi VARCHAR,
   pass_code VARCHAR,
   store_number INTEGER,
   active BOOLEAN,
   department VARCHAR,
   cynergi_system_admin BOOLEAN,
   alternative_store_indicator VARCHAR,
   alternative_area INTEGER
) SERVER fastinfo OPTIONS (TABLE_NAME 'employee_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.inventory_vw (
    id BIGINT,
    dataset VARCHAR,
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
    location_type INTEGER
) SERVER fastinfo OPTIONS (TABLE_NAME 'inventory_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.itemfile_vw (
   id BIGINT,
   dataset VARCHAR,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   number VARCHAR,
   description_1 VARCHAR,
   description_2 VARCHAR,
   discontinued_indr VARCHAR,
   vendor_number INTEGER
) SERVER fastinfo OPTIONS (TABLE_NAME 'itemfile_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.customer_vw (
   id BIGINT,
   dataset VARCHAR,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   number integer,
   first_name_mi VARCHAR,
   last_name VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'customer_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.operator_vw (
    id BIGINT,
    dataset VARCHAR,
    time_created TIMESTAMPTZ,
    time_updated TIMESTAMPTZ,
    name VARCHAR,
    account_payable_security INTEGER,
    purchase_order_security INTEGER,
    general_ledger_security INTEGER,
    system_administration_security INTEGER,
    file_maintenance_security INTEGER,
    bank_reconciliation_security INTEGER,
    number INTEGER
) SERVER fastinfo OPTIONS (TABLE_NAME 'operator_vw', SCHEMA_NAME 'public');

GRANT USAGE ON SCHEMA fastinfo_prod_import TO cynergiuser;
GRANT SELECT ON ALL TABLES IN SCHEMA fastinfo_prod_import TO cynergiuser;
-- End cynergidb setup
