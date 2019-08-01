-- BEGIN fastinfo_production SETUP
\c fastinfo_production
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS corrto;
CREATE SCHEMA IF NOT EXISTS corrnr;

--- BEGIN EMPLOYEES SETUP
CREATE TABLE IF NOT EXISTS  corrto.level1_loc_emps ( -- create stand-in table that should exist in fastinfo if a dump isn't used
   id                BIGSERIAL                           NOT NULL PRIMARY KEY,
   created_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
   updated_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
   emp_nbr           INTEGER,
   emp_last_name     VARCHAR(15),
   emp_first_name_mi VARCHAR(15),
   emp_pass_1        VARCHAR(1),
   emp_pass_2        VARCHAR(1),
   emp_pass_3        VARCHAR(1),
   emp_pass_4        VARCHAR(1),
   emp_pass_5        VARCHAR(1),
   emp_pass_6        VARCHAR(1),
   emp_store_nbr     INTEGER
);
CREATE TABLE IF NOT EXISTS  corrnr.level1_loc_emps ( -- create stand-in table that should exist in fastinfo if a dump isn't used
   id                BIGSERIAL                           NOT NULL PRIMARY KEY,
   created_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
   updated_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
   emp_nbr           INTEGER,
   emp_last_name     VARCHAR(15),
   emp_first_name_mi VARCHAR(15),
   emp_pass_1        VARCHAR(1),
   emp_pass_2        VARCHAR(1),
   emp_pass_3        VARCHAR(1),
   emp_pass_4        VARCHAR(1),
   emp_pass_5        VARCHAR(1),
   emp_pass_6        VARCHAR(1),
   emp_store_nbr     INTEGER
);

DO $$
   DECLARE r record;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW employee_vw AS';
   unionAll := '';

   FOR r IN SELECT schema_name FROM information_schema.schemata where schema_name not LIKE 'pg%' and schema_name NOT IN ('public', 'information_schema', 'alles')
   LOOP
      sqlToExec := sqlToExec || ' ' ||
      unionAll ||
      'SELECT ' ||
      '   id AS id, ' ||
      '   emp_nbr AS number, ' ||
      '   emp_store_nbr AS store_number, ' ||
      '   ''' || r.schema_name || ''' AS dataset, ' ||
      '   emp_last_name AS last_name, ' ||
      '   emp_first_name_mi AS first_name_mi, ' ||
      '   TRIM(BOTH FROM ' ||
      '        CAST(emp_pass_1 AS TEXT) || ' ||
      '        CAST(emp_pass_2 AS TEXT) || ' ||
      '        CAST(emp_pass_3 AS TEXT) || ' ||
      '        CAST(emp_pass_4 AS TEXT) || ' ||
      '        CAST(emp_pass_5 AS TEXT) || ' ||
      '        CAST(emp_pass_6 AS TEXT) ' ||
      '      ) AS pass_code, ' ||
      '   true AS active, ' ||
      '   created_at AT TIME ZONE ''UTC'' AS time_created, ' ||
      '   updated_at AT TIME ZONE ''UTC'' AS time_updated ' ||
      'FROM ' || r.schema_name || '.level1_loc_emps ' ||
      'WHERE emp_nbr IS NOT NULL ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY number';

   EXECUTE sqlToExec;
END $$;
--- END EMPLOYEES SETUP

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

CREATE TABLE IF NOT EXISTS corrnr.level2_stores(
   id                BIGSERIAL                           NOT NULL PRIMARY KEY,
   created_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
   updated_at        TIMESTAMP DEFAULT clock_timestamp() NOT NULL,
   loc_tran_loc      INTEGER,
   loc_transfer_desc VARCHAR(27)
);

CREATE OR REPLACE VIEW store_vw AS
   SELECT
      id AS id,
      created_at AT TIME ZONE 'UTC' AS time_created,
      updated_at AT TIME ZONE 'UTC' AS time_updated,
      loc_tran_loc AS number,
      loc_transfer_desc AS name,
      'corrto' AS dataset
   FROM corrto.level2_stores
   WHERE loc_transfer_desc IS NOT NULL
   UNION ALL
   SELECT
      id AS id,
      created_at AT TIME ZONE 'UTC' AS time_created,
      updated_at AT TIME ZONE 'UTC' AS time_updated,
      loc_tran_loc AS number,
      loc_transfer_desc AS name,
      'corrnr' AS dataset
   FROM corrnr.level2_stores
   WHERE loc_transfer_desc IS NOT NULL;
--- END STORES SETUP

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
CREATE TABLE IF NOT EXISTS corrnr.level1_ninvrecs (
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

CREATE OR REPLACE VIEW inventory_vw AS
   SELECT
      id,
      created_at AT TIME ZONE 'UTC' AS time_created,
      updated_at AT TIME ZONE 'UTC' AS time_updated,
      inv_serial_nbr_key AS serial_number,
      inv_alt_id AS barcode_number,
      inv_location_rec_1 AS location,
      inv_status AS status,
      inv_mk_model_nbr AS make_model_number,
      inv_model_nbr_category AS model_category,
      Left(inv_mk_model_nbr, 2) AS product_code,
      inv_desc AS description
   FROM corrto.level1_ninvrecs
   WHERE inv_status IN ('N', 'O', 'R', 'D')
   UNION
   SELECT
      id,
      created_at AT TIME ZONE 'UTC' AS time_created,
      updated_at AT TIME ZONE 'UTC' AS time_updated,
      inv_serial_nbr_key AS serial_number,
      inv_alt_id AS barcode_number,
      inv_location_rec_1 AS location,
      inv_status AS status,
      inv_mk_model_nbr AS make_model_number,
      inv_model_nbr_category AS model_category,
      Left(inv_mk_model_nbr, 2) AS product_code,
      inv_desc AS description
   FROM corrnr.level1_ninvrecs
   WHERE inv_status IN ('N', 'O', 'R', 'D');
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
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   number INTEGER,
   last_name VARCHAR,
   first_name_mi VARCHAR,
   pass_code VARCHAR,
   store_number INTEGER,
   dataset VARCHAR,
   active BOOLEAN
) SERVER fastinfo OPTIONS (TABLE_NAME 'employee_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.store_vw (
   id BIGINT,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   number INTEGER,
   name VARCHAR,
   dataset VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'store_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.inventory_vw (
   id BIGINT,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   serial_number VARCHAR,
   barcode_number VARCHAR,
   location INTEGER,
   status VARCHAR,
   make_model_number VARCHAR,
   model_category VARCHAR,
   product_code VARCHAR,
   description VARCHAR
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
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   number INTEGER,
   last_name VARCHAR,
   first_name_mi VARCHAR,
   pass_code VARCHAR,
   store_number INTEGER,
   dataset VARCHAR,
   active BOOLEAN
) SERVER fastinfo OPTIONS (TABLE_NAME 'employee_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.store_vw (
   id BIGINT,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   number INTEGER,
   name VARCHAR,
   dataset VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'store_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.inventory_vw (
   id BIGINT,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   serial_number VARCHAR,
   barcode_number VARCHAR,
   location INTEGER,
   status VARCHAR,
   make_model_number VARCHAR,
   model_category VARCHAR,
   product_code VARCHAR,
   description VARCHAR
   ) SERVER fastinfo OPTIONS (TABLE_NAME 'inventory_vw', SCHEMA_NAME 'public');
-- BEGIN cynergidemodb SETUP
