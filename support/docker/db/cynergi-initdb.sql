\c fastinfo_production
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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
      ) AS passcode,
      true AS active
   FROM corrto.level1_loc_emps
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
      ) AS passcode,
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

CREATE FOREIGN TABLE fastinfo_prod_import.employee (
   number INTEGER,
   passcode VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'employee_vw', SCHEMA_NAME 'public');

CREATE TABLE cyn_temp.employee (
   number INTEGER NOT NULL,
   passcode VARCHAR(6) NOT NULL
)
