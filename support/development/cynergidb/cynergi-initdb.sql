-- BEGIN cynergidevelopdb SETUP
\c cynergidevelopdb
CREATE EXTENSION IF NOT EXISTS postgres_fdw;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
CREATE SCHEMA IF NOT EXISTS fastinfo_prod_import;

DROP SERVER IF EXISTS fastinfo CASCADE;

CREATE SERVER fastinfo
    FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (host 'localhost', dbname 'fastinfo_production', updatable 'false');
CREATE USER MAPPING FOR cynergiuser
    SERVER fastinfo
    OPTIONS (USER 'postgres', PASSWORD 'password');

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

CREATE FOREIGN TABLE fastinfo_prod_import.furncol_vw (
    id BIGINT,
    dataset VARCHAR,
    time_created TIMESTAMPTZ,
    time_updated TIMESTAMPTZ,
    number INTEGER,
    description VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'furncol_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.furnfab_vw (
    id BIGINT,
    dataset VARCHAR,
    time_created TIMESTAMPTZ,
    time_updated TIMESTAMPTZ,
    number INTEGER,
    description VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'furnfab_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.location_vw (
    id BIGINT,
    dataset VARCHAR,
    number INTEGER,
    name VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'location_vw', SCHEMA_NAME 'public');

GRANT USAGE ON SCHEMA fastinfo_prod_import TO cynergiuser;
GRANT SELECT ON ALL TABLES IN SCHEMA fastinfo_prod_import TO cynergiuser;
-- END cynergidevelopdb SETUP
