\c fastinfo_production
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS corrto;
CREATE SCHEMA IF NOT EXISTS corrnr;

CREATE TABLE IF NOT EXISTS  corrto.level1_loc_emps ( -- create stand-in table that should exist in fastinfo if a dump isn't used
   id              BIGSERIAL                                 NOT NULL PRIMARY KEY,
   ht_etl_checksum VARCHAR(255),
   created_at      TIMESTAMP DEFAULT clock_timestamp()       NOT NULL,
   updated_at      TIMESTAMP DEFAULT clock_timestamp()       NOT NULL,
   emp_nbr         INTEGER,
   emp_pass_1      VARCHAR(1),
   emp_pass_2      VARCHAR(1),
   emp_pass_3      VARCHAR(1),
   emp_pass_4      VARCHAR(1),
   emp_pass_5      VARCHAR(1),
   emp_pass_6      VARCHAR(1)
);
CREATE TABLE IF NOT EXISTS  corrnr.level1_loc_emps ( -- create stand-in table that should exist in fastinfo if a dump isn't used
   id              BIGSERIAL                                 NOT NULL PRIMARY KEY,
   ht_etl_checksum VARCHAR(255),
   created_at      TIMESTAMP DEFAULT clock_timestamp()       NOT NULL,
   updated_at      TIMESTAMP DEFAULT clock_timestamp()       NOT NULL,
   emp_nbr         INTEGER,
   emp_pass_1      VARCHAR(1),
   emp_pass_2      VARCHAR(1),
   emp_pass_3      VARCHAR(1),
   emp_pass_4      VARCHAR(1),
   emp_pass_5      VARCHAR(1),
   emp_pass_6      VARCHAR(1)
);

CREATE OR REPLACE VIEW employee_vw AS
   SELECT
      id AS id,
      ht_etl_checksum::uuid AS uu_row_id,
      created_at AT TIME ZONE 'UTC' AS time_created,
      updated_at AT TIME ZONE 'UTC' AS time_updated,
      emp_nbr AS number,
      TRIM(BOTH FROM
         CAST(emp_pass_1 AS TEXT) ||
         CAST(emp_pass_2 AS TEXT) ||
         CAST(emp_pass_3 AS TEXT) ||
         CAST(emp_pass_4 AS TEXT) ||
         CAST(emp_pass_5 AS TEXT) ||
         CAST(emp_pass_6 AS TEXT)
      ) AS pass_code,
      true AS active
   FROM corrto.level1_loc_emps
   WHERE emp_nbr IS NOT NULL
   UNION ALL
   SELECT
      id AS id,
      ht_etl_checksum::uuid AS uu_row_id,
      created_at AT TIME ZONE 'UTC' AS time_created,
      updated_at AT TIME ZONE 'UTC' AS time_updated,
      emp_nbr AS number,
      TRIM(BOTH FROM
         CAST(emp_pass_1 AS TEXT) ||
         CAST(emp_pass_2 AS TEXT) ||
         CAST(emp_pass_3 AS TEXT) ||
         CAST(emp_pass_4 AS TEXT) ||
         CAST(emp_pass_5 AS TEXT) ||
         CAST(emp_pass_6 AS TEXT)
      ) AS pass_code,
      true AS active
   FROM corrnr.level1_loc_emps
   ORDER BY number;

\c cynergidb
CREATE EXTENSION IF NOT EXISTS postgres_fdw;
CREATE SCHEMA fastinfo_prod_import;

CREATE SERVER fastinfo
   FOREIGN DATA WRAPPER postgres_fdw
   OPTIONS (host 'localhost', dbname 'fastinfo_production', updatable 'false');
CREATE USER MAPPING FOR postgres
   SERVER fastinfo
   OPTIONS (USER 'postgres', PASSWORD 'password');

CREATE FOREIGN TABLE fastinfo_prod_import.employee_vw (
   id BIGINT,
   uu_row_id UUID,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   number INTEGER,
   pass_code VARCHAR,
   active BOOLEAN
) SERVER fastinfo OPTIONS (TABLE_NAME 'employee_vw', SCHEMA_NAME 'public');

\c cynergidemodb
CREATE EXTENSION IF NOT EXISTS postgres_fdw;
CREATE SCHEMA fastinfo_prod_import;

CREATE SERVER fastinfo
   FOREIGN DATA WRAPPER postgres_fdw
   OPTIONS (host 'localhost', dbname 'fastinfo_production', updatable 'false');
CREATE USER MAPPING FOR postgres
   SERVER fastinfo
   OPTIONS (USER 'postgres', PASSWORD 'password');

CREATE FOREIGN TABLE fastinfo_prod_import.employee_vw (
   id BIGINT,
   uu_row_id UUID,
   time_created TIMESTAMPTZ,
   time_updated TIMESTAMPTZ,
   number INTEGER,
   pass_code VARCHAR,
   active BOOLEAN
) SERVER fastinfo OPTIONS (TABLE_NAME 'employee_vw', SCHEMA_NAME 'public');
