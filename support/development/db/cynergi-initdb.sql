-- BEGIN fastinfo_production SETUP
\c fastinfo_production
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS corrto;

--- BEGIN STORES SETUP
CREATE TABLE IF NOT EXISTS corrto.level2_stores(
    id                BIGSERIAL NOT NULL PRIMARY KEY,
    created_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
    updated_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
    loc_tran_loc      INTEGER,
    loc_transfer_desc VARCHAR(27),
    UNIQUE (loc_tran_loc, loc_transfer_desc)
);

INSERT INTO corrto.level2_stores (loc_tran_loc, loc_transfer_desc) SELECT 1, 'KANSAS CITY' WHERE NOT EXISTS(SELECT * FROM corrto.level2_stores WHERE loc_tran_loc = 1);
INSERT INTO corrto.level2_stores (loc_tran_loc, loc_transfer_desc) SELECT 3, 'INDEPENDENCE' WHERE NOT EXISTS(SELECT * FROM corrto.level2_stores WHERE loc_tran_loc = 3);
INSERT INTO corrto.level2_stores (loc_tran_loc, loc_transfer_desc) SELECT 9000, 'HOME OFFICE' WHERE NOT EXISTS(SELECT * FROM corrto.level2_stores WHERE loc_tran_loc = 9000);

DO $$
   DECLARE r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW store_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'store_vw') THEN
      DROP VIEW store_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata where schema_name not LIKE 'pg%' and schema_name NOT IN ('public', 'information_schema', 'alles')
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll
      || 'SELECT '
      || '   id AS id, '
      || '   loc_tran_loc AS number, '
      || '   loc_transfer_desc AS name, '
      || '   ''' || r.schema_name || ''' AS dataset, '
      || '   created_at AT TIME ZONE ''UTC'' AS time_created, '
      || '   updated_at AT TIME ZONE ''UTC'' AS time_updated '
      || 'FROM ' || r.schema_name || '.level2_stores '
      || 'WHERE loc_transfer_desc IS NOT NULL ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY number ASC';

   EXECUTE sqlToExec;
END $$;
--- END STORES SETUP

--- BEGIN EMPLOYEES SETUP
CREATE TABLE IF NOT EXISTS  corrto.level1_loc_emps ( -- create stand-in table that should exist in fastinfo if a dump isn't used
   id                BIGSERIAL                           NOT NULL PRIMARY KEY,
   emp_nbr           INTEGER,
   emp_store_nbr     INTEGER,
   emp_last_name     VARCHAR(15),
   emp_first_name_mi VARCHAR(15),
   emp_pass_1        VARCHAR(1),
   emp_pass_2        VARCHAR(1),
   emp_pass_3        VARCHAR(1),
   emp_pass_4        VARCHAR(1),
   emp_pass_5        VARCHAR(1),
   emp_pass_6        VARCHAR(1),
   created_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
   updated_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL
);

DO $$
   DECLARE r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW employee_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'employee_vw') THEN
      DROP VIEW employee_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata where schema_name not LIKE 'pg%' and schema_name NOT IN ('public', 'information_schema', 'alles')
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll
      || 'SELECT '
      || '   id AS id, '
      || '   emp_nbr AS number, '
      || '   emp_store_nbr AS store_number, '
      || '   ''' || r.schema_name || ''' AS dataset, '
      || '   emp_last_name AS last_name, '
      || '   emp_first_name_mi AS first_name_mi, '
      || '   emp_dept AS department, '
      || '   TRIM(BOTH FROM '
      || '        CAST(emp_pass_1 AS TEXT) || '
      || '        CAST(emp_pass_2 AS TEXT) || '
      || '        CAST(emp_pass_3 AS TEXT) || '
      || '        CAST(emp_pass_4 AS TEXT) || '
      || '        CAST(emp_pass_5 AS TEXT) || '
      || '        CAST(emp_pass_6 AS TEXT) '
      || '      ) AS pass_code, '
      || '   true AS active, '
      || '   created_at AT TIME ZONE ''UTC'' AS time_created, '
      || '   updated_at AT TIME ZONE ''UTC'' AS time_updated '
      || 'FROM ' || r.schema_name || '.level1_loc_emps '
      || 'WHERE emp_nbr IS NOT NULL '
      || '      AND TRIM(BOTH FROM '
      || '            CAST(emp_pass_1 AS TEXT) || '
      || '            CAST(emp_pass_2 AS TEXT) || '
      || '            CAST(emp_pass_3 AS TEXT) || '
      || '            CAST(emp_pass_4 AS TEXT) || '
      || '            CAST(emp_pass_5 AS TEXT) || '
      || '            CAST(emp_pass_6 AS TEXT) '
      || '          ) <> ''''';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY number';

   EXECUTE sqlToExec;
END $$;
--- END EMPLOYEES SETUP

--- BEGIN INVENTORY SETUP
CREATE TABLE IF NOT EXISTS corrto.level1_ninvrecs (
   id                     BIGSERIAL                           NOT NULL PRIMARY KEY,
   created_at             TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
   updated_at             TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
   inv_serial_nbr_key     VARCHAR(10)                         NOT NULL,
   inv_alt_id             VARCHAR(30)                         NOT NULL,
   inv_location_rec_1     INTEGER                             NOT NULL,
   inv_status             VARCHAR(1)                          NOT NULL,
   inv_mk_model_nbr       VARCHAR(18)                         NOT NULL,
   inv_model_nbr_category VARCHAR(1)                          NOT NULL,
   inv_desc               VARCHAR(28)                         NOT NULL
);

DO $$
   DECLARE r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW inventory_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'inventory_vw') THEN
      DROP VIEW inventory_vw;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata where schema_name not LIKE 'pg%' and schema_name NOT IN ('public', 'information_schema', 'alles')
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll
      || 'SELECT '
      || '   inv_recs.id AS id, '
      || '   inv_recs.inv_serial_nbr_key AS serial_number, '
      || '   CASE '
      || '      WHEN LEFT(loc_trans.loc_tran_strip_dir, 1) = ''B'' THEN inv_recs.inv_alt_id '
      || '      ELSE inv_recs.inv_serial_nbr_key '
      || '   END AS lookup_key, '
      || '   CASE '
      || '      WHEN LEFT(loc_tran_strip_dir, 1) = ''B'' THEN ''BARCODE'' '
      || '      ELSE ''SERIAL'''
      || '   END AS lookup_key_type, '
      || '   inv_recs.inv_serial_nbr_key AS barcode, '
      || '   inv_recs.inv_alt_id AS alt_id, '
      || '   manufacturer.manufile_manu_name AS brand, '
      || '   inv_recs.inv_mk_model_nbr AS model_number, '
      || '   LEFT(inv_recs.inv_mk_model_nbr, 1) || ''-'' || inv_recs.inv_desc AS product_code, '
      || '   inv_recs.inv_desc AS description, '
      || '   inv_recs.inv_date_received AS received_date, '
      || '   inv_recs.inv_original_cost AS original_cost, '
      || '   inv_recs.inv_actual_cost AS actual_cost, '
      || '   inv_recs.inv_model_nbr_category AS model_category, '
      || '   inv_recs.inv_total_times_rented AS times_rented, '
      || '   inv_recs.inv_total_revenue AS total_revenue, '
      || '   inv_recs.inv_remain_bk_value AS remaining_value, '
      || '   inv_recs.inv_sell_price AS sell_price, '
      || '   inv_recs.inv_assigned_value AS assigned_value, '
      || '   inv_recs.inv_nbr_idle_days AS idle_days, '
      || '   inv_recs.inv_condition AS condition, '
      || '   CASE '
      || '      WHEN inv_recs.inv_status = ''R'' AND inv_recs.inv_date_returned IS NOT NULL THEN inv_recs.inv_date_returned '
      || '      ELSE NULL '
      || '   END AS returned_date, '
      || '   inv_recs.inv_location_rec_1 AS location, '
      || '   inv_recs.inv_status AS status, '
      || '   loc_trans.loc_tran_primary_loc AS primary_location, '
      || '   loc_transfer_loc_type AS location_type, '
      || '   inv_recs.created_at AT TIME ZONE ''UTC'' AS time_created, '
      || '   inv_recs.updated_at AT TIME ZONE ''UTC'' AS time_updated '
      || 'FROM ' || r.schema_name || '.level1_ninvrecs inv_recs '
      || '     JOIN ' || r.schema_name || '.level1_loc_trans loc_trans '
      || '       ON inv_location_rec_1 = loc_tran_loc '
      || '     LEFT JOIN ' || r.schema_name || '.level1_manufiles manufacturer '
      || '       ON SUBSTRING(inv_mk_model_nbr, 3, 3) = manufile_manu_code_an3 ';

      unionAll := ' UNION ALL ';
   END LOOP;

   EXECUTE sqlToExec;
END $$;
--- END INVENTORY SETUP
-- END fastinfo_production SETUP

-- BEGIN cynergidb SETUP
\c cynergidb
CREATE EXTENSION IF NOT EXISTS postgres_fdw;
CREATE SCHEMA IF NOT EXISTS fastinfo_prod_import;

DROP SERVER IF EXISTS fastinfo CASCADE;

CREATE SERVER fastinfo
    FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (host 'localhost', dbname 'fastinfo_production', updatable 'false');
CREATE USER MAPPING FOR CURRENT_USER
    SERVER fastinfo
    OPTIONS (USER 'postgres', PASSWORD 'password');
GRANT USAGE ON SCHEMA fastinfo_prod_import TO postgres;
GRANT SELECT ON ALL TABLES IN SCHEMA fastinfo_prod_import TO postgres;

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
-- END cynergidb SETUP

-- BEGIN cynergidemodb SETUP
\c cynergidemodb
CREATE EXTENSION IF NOT EXISTS postgres_fdw;
CREATE SCHEMA IF NOT EXISTS fastinfo_prod_import;

DROP SERVER IF EXISTS fastinfo CASCADE;

CREATE SERVER fastinfo
   FOREIGN DATA WRAPPER postgres_fdw
   OPTIONS (host 'localhost', dbname 'fastinfo_production', updatable 'false');
CREATE USER MAPPING FOR CURRENT_USER
    SERVER fastinfo
    OPTIONS (USER 'postgres', PASSWORD 'password');
GRANT USAGE ON SCHEMA fastinfo_prod_import TO postgres;
GRANT SELECT ON ALL TABLES IN SCHEMA fastinfo_prod_import TO postgres;

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
-- BEGIN cynergidemodb SETUP
