-- to execute this run as postgres user `psql -v ON_ERROR_STOP=1 -f /opt/cyn/v01/cynmid/data/setup-database.sql -v fastinfoUserName= -v fastinfoPassword= -v datasets=
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
   unionStr VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW department_vw AS';
   unionStr := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'department_vw') THEN
      DROP VIEW IF EXISTS department_vw CASCADE;
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
      DROP VIEW IF EXISTS store_vw CASCADE;
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
      DROP VIEW IF EXISTS employee_vw CASCADE;
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
            CASE
               WHEN emp_termination_date > NOW() THEN true
               WHEN emp_termination_date is null THEN true
               ELSE false
            END                                                                                            AS active,
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
      DROP VIEW IF EXISTS inventory_vw CASCADE;
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
               WHEN LEFT(location.loc_tran_strip_dir, 1) = ''B'' AND inventory.inv_alt_id <> '''' THEN inventory.inv_alt_id
               ELSE inventory.inv_serial_nbr_key
            END                                                                                                                      AS lookup_key,
            CASE
               WHEN LEFT(location.loc_tran_strip_dir, 1) = ''B'' AND inventory.inv_alt_id <> '''' THEN ''ALT_ID''
               ELSE ''SERIAL''
            END                                                                                                                      AS lookup_key_type,
            inventory.inv_serial_nbr_key                                                                                             AS barcode,
            inventory.inv_alt_id                                                                                                     AS alt_id,
            manufacturer.manufile_manu_name                                                                                          AS brand,
            model.itemfile_nbr                                                                                                             AS model_number,
            Left(model.itemfile_nbr,2)                                                                                                     AS product_code,
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
            inventory.inv_invoice_nbr                                                                                                AS invoice_number,
            inventory.inv_invoice_expensed_date                                                                                      AS inv_invoice_expensed_date,
            inventory.inv_po_nbr                                                                                                     AS inv_purchase_order_number,
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
      DROP VIEW itemfile_vw CASCADE;
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
      DROP VIEW customer_vw CASCADE;
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
   sqlToExec := 'CREATE OR REPLACE VIEW operator_vw AS
                 SELECT *
                 FROM (';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'operator_vw') THEN
      DROP VIEW operator_vw CASCADE;
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
            CAST(operator_name AS INTEGER)           AS number,
            operator.operator_security_2             AS account_payable_security,
            operator.operator_security_4             AS purchase_order_security,
            operator.operator_security_5             AS general_ledger_security,
            operator.operator_security_10            AS system_administration_security,
            operator.operator_security_11            AS file_maintenance_security,
            operator.operator_security_16            AS bank_reconciliation_security
         FROM ' || r.schema_name || '.level1_operators operator
         INNER JOIN ' || r.schema_name || '.level2_employees employee ON CAST(operator.operator_name AS INTEGER) = employee.emp_nbr
         WHERE operator_type = ''O'' AND isnumeric(operator.operator_name)
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || ' ) operator ORDER BY name, id DESC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW furncol_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'furncol_vw') THEN
      DROP VIEW furncol_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            furncol.id                               AS id,
            ''' || r.schema_name || '''::text        AS dataset,
            furncol.created_at AT TIME ZONE ''UTC''  AS time_created,
            furncol.updated_at AT TIME ZONE ''UTC''  AS time_updated,
            furncol.furn_col_code                    AS number,
            furncol.furn_col_description             AS description
         FROM ' || r.schema_name || '.level1_furn_cols furncol
         WHERE furn_col_rec_type = ''1''
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
   sqlToExec := 'CREATE OR REPLACE VIEW furnfab_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'furnfab_vw') THEN
      DROP VIEW furncol_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            furnfab.id                               AS id,
            ''' || r.schema_name || '''::text        AS dataset,
            furnfab.created_at AT TIME ZONE ''UTC''  AS time_created,
            furnfab.updated_at AT TIME ZONE ''UTC''  AS time_updated,
            furnfab.furn_fab_code                    AS number,
            furnfab.furn_fab_description             AS description
         FROM ' || r.schema_name || '.level1_furn_cols furnfab
         WHERE furn_col_rec_type = ''2''
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
   sqlToExec := 'CREATE OR REPLACE VIEW location_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'location_vw') THEN
      DROP VIEW IF EXISTS location_vw CASCADE;
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
   sqlToExec := 'CREATE OR REPLACE VIEW csv_active_customer_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_active_customer_vw') THEN
      DROP VIEW IF EXISTS csv_active_customer_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_id,
            ''''::text                       AS people_id,
            customers.cust_acct_nbr          AS unique_id,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            case
				when reference.refinfo_cust_mailing_address Is Null
				then customers.cust_address
				else reference.refinfo_cust_mailing_address
				end as address_1,
            case
               when reference.refinfo_cust_mailing_address_2 Is Null
               then customers.cust_address_2
               else reference.refinfo_cust_mailing_address_2
               end as address_2,
            case
               when reference.refinfo_cust_mailing_city Is Null
               then customers.cust_city
               else reference.refinfo_cust_mailing_city
               end as city,
            case
               when reference.refinfo_cust_mailing_state Is Null
               then customers.cust_state
               else reference.refinfo_cust_mailing_state
               end as state,
            case
               when reference.refinfo_cust_mailing_zip_pc Is Null
               then customers.cust_zip_pc
               else reference.refinfo_cust_mailing_zip_pc
               end as zip,
--            customers.cust_address           AS address_1,
--            customers.cust_address_2         AS address_2,
--            customers.cust_city              AS city,
--            customers.cust_state             AS state,
--            customers.cust_zip_pc            AS zip,
            customers.cust_cell_phone        AS cell_phone_number,
            customers.cust_home_phone        AS home_phone_number,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_id,
            agreement_versions.agreement_payment_terms
                                             AS payment_frequency,
            customers.cust_cell_optin        AS text_opt_in,
            agreement_versions.agreement_recur_pmt_switch
                                             AS online_indicator,
            case
               when agreement_versions.agreement_esp_amt > 0 then ''Y''
            else ''N''
            end AS care_plus,
            sum
              (case
                  when agreement_versions.agreement_payment_terms = ''M'' then Round(trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2))
                  when agreement_versions.agreement_payment_terms = ''W'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 4.33))
                  when agreement_versions.agreement_payment_terms = ''B'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                  else Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
              end) AS projected_payout,
            sum
              (case
                  when agreement_versions.agreement_payment_terms = ''M'' then Round(trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2)* 4.33)
                  when agreement_versions.agreement_payment_terms = ''W'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2)))
                  when agreement_versions.agreement_payment_terms = ''B'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2 * 4.33))
                  else Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) * 2))
              end) AS payments_left_in_weeks,
            case
              when agreement_versions.agreement_open_flag = ''Y''
                   and agreement_versions.agreement_next_due_date < current_date
                     then ''Y''
                   else ''N''
            end AS past_due,
            case
              when agreement_versions.agreement_open_flag = ''Y''
                   and agreement_versions.agreement_next_due_date < current_date
                      then current_date - agreement_versions.agreement_next_due_date
                   else null
            end AS days_past_due
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            LEFT JOIN
            ' || r.schema_name || '.level2_refinfos as refereence on reference.customer_id = customers.id
         WHERE
            agreements.agreement_type = ''O'' and agreement_versions.agreement_open_flag = ''Y''
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
            customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag, agreement_versions.agreement_payment_terms,
            customers.cust_cell_optin,
            agreement_versions.agreement_recur_pmt_switch,agreement_versions.agreement_esp_amt, agreement_versions.agreement_next_due_date,
            reference.refinfo_cust_mailing_address,reference.refinfo_cust_mailing_address_2,reference.refinfo_cust_mailing_city, reference.refinfo_cust_mailing_state,
			   reference.refinfo_cust_mailing_zip_pc
          ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY unique_id ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_collection_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_collection_vw') THEN
      DROP VIEW IF EXISTS csv_collection_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc               AS store_id,
            ''''::text                        AS people_id,
            customers.cust_acct_nbr           AS unique_id,
            customers.cust_first_name_mi      AS first_name,
            customers.cust_last_name          AS last_name,
            case
            when reference.refinfo_cust_mailing_address Is Null
            then customers.cust_address
            else reference.refinfo_cust_mailing_address
            end as address_1,
            case
               when reference.refinfo_cust_mailing_address_2 Is Null
               then customers.cust_address_2
               else reference.refinfo_cust_mailing_address_2
               end as address_2,
            case
               when reference.refinfo_cust_mailing_city Is Null
               then customers.cust_city
               else reference.refinfo_cust_mailing_city
               end as city,
            case
               when reference.refinfo_cust_mailing_state Is Null
               then customers.cust_state
               else reference.refinfo_cust_mailing_state
               end as state,
            case
               when reference.refinfo_cust_mailing_zip_pc Is Null
               then customers.cust_zip_pc
               else reference.refinfo_cust_mailing_zip_pc
               end as zip,
--            customers.cust_address            AS address_1,
--            customers.cust_address_2          AS address_2,
--            customers.cust_city               AS city,
--            customers.cust_state              AS state,
--            customers.cust_zip_pc             AS zip,
            customers.cust_cell_phone         AS cell_phone_number,
            customers.cust_home_phone         AS home_phone_number,
            customers.cust_email_address      AS email,
            agreements.agreement_number       AS agreement_id,
            current_date - agreement_versions.agreement_next_due_date AS days_late
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
             LEFT JOIN
            ' || r.schema_name || '.level2_refinfos as refereence on reference.customer_id = customers.id
         WHERE
            agreements.agreement_type = ''O''
            and agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,
            customers.cust_cell_phone,customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag,agreement_versions.agreement_next_due_date,
            reference.refinfo_cust_mailing_address,reference.refinfo_cust_mailing_address_2,reference.refinfo_cust_mailing_city, reference.refinfo_cust_mailing_state,
            reference.refinfo_cust_mailing_zip_pc
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY unique_id ASC';

   EXECUTE sqlToExec;
END $$;


DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_birthday_customer_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_birthday_customer_vw') THEN
      DROP VIEW IF EXISTS csv_birthday_customer_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
           stores.loc_tran_loc               AS store_id,
            ''''::text                       AS people_id,
            customers.cust_acct_nbr          AS unique_id,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            case
            when reference.refinfo_cust_mailing_address Is Null
            then customers.cust_address
            else reference.refinfo_cust_mailing_address
            end as address_1,
            case
               when reference.refinfo_cust_mailing_address_2 Is Null
               then customers.cust_address_2
               else reference.refinfo_cust_mailing_address_2
               end as address_2,
            case
               when reference.refinfo_cust_mailing_city Is Null
               then customers.cust_city
               else reference.refinfo_cust_mailing_city
               end as city,
            case
               when reference.refinfo_cust_mailing_state Is Null
               then customers.cust_state
               else reference.refinfo_cust_mailing_state
               end as state,
            case
               when reference.refinfo_cust_mailing_zip_pc Is Null
               then customers.cust_zip_pc
               else reference.refinfo_cust_mailing_zip_pc
               end as zip,
--            customers.cust_address           AS address_1,
--            customers.cust_address_2         AS address_2,
--            customers.cust_city              AS city,
--            customers.cust_state             AS state,
--            customers.cust_zip_pc            AS zip,
            customers.cust_cell_phone        AS cell_phone_number,
            customers.cust_home_phone        AS home_phone_number,
            customers.cust_email_address     AS email,
            customers.cust_birth_date        AS birth_day
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            LEFT JOIN
            ' || r.schema_name || '.level2_refinfos as refereence on reference.customer_id = customers.id
         WHERE
            agreements.agreement_type = ''O''
            and agreement_versions.agreement_open_flag = ''Y''
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,
            customers.cust_cell_phone,customers.cust_home_phone,customers.cust_email_address, customers.cust_birth_date,
            reference.refinfo_cust_mailing_address,reference.refinfo_cust_mailing_address_2,reference.refinfo_cust_mailing_city, reference.refinfo_cust_mailing_state,
            reference.refinfo_cust_mailing_zip_pc
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY unique_id ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_last_week_deliveries_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_last_week_deliveries_vw') THEN
      DROP VIEW IF EXISTS csv_last_week_deliveries_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
        SELECT a.*,
             case when b.agr_count > ''0'' then ''N'' else ''Y'' end as new_customer
         FROM
         (SELECT
            ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc               AS store_id,
            ''''::text                        AS people_id,
            customers.cust_acct_nbr           AS unique_id,
            customers.cust_first_name_mi      AS first_name,
            customers.cust_last_name          AS last_name,
            case
            when reference.refinfo_cust_mailing_address Is Null
            then customers.cust_address
            else reference.refinfo_cust_mailing_address
            end as address_1,
            case
               when reference.refinfo_cust_mailing_address_2 Is Null
               then customers.cust_address_2
               else reference.refinfo_cust_mailing_address_2
               end as address_2,
            case
               when reference.refinfo_cust_mailing_city Is Null
               then customers.cust_city
               else reference.refinfo_cust_mailing_city
               end as city,
            case
               when reference.refinfo_cust_mailing_state Is Null
               then customers.cust_state
               else reference.refinfo_cust_mailing_state
               end as state,
            case
               when reference.refinfo_cust_mailing_zip_pc Is Null
               then customers.cust_zip_pc
               else reference.refinfo_cust_mailing_zip_pc
               end as zip,
--            customers.cust_address            AS address_1,
--            customers.cust_address_2          AS address_2,
--            customers.cust_city               AS city,
--            customers.cust_state              AS state,
--            customers.cust_zip_pc             AS zip,
            customers.cust_cell_phone         AS cell_phone_number,
            customers.cust_home_phone         AS home_phone_number,
            customers.cust_email_address      AS email,
            agreements.agreement_number       AS agreement_id,
            agreement_contract_date           AS purchase_date,
            case
              when agreement_versions.agreement_open_flag = ''Y''
                  then ''Active''
                  else ''Inactive''
            end AS current_customer_status
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            LEFT JOIN
            ' || r.schema_name || '.level2_refinfos as refereence on reference.customer_id = customers.id
         WHERE
            agreements.agreement_type = ''O''
            and agreement_versions.agreement_contract_date
                   between current_date - extract(dow from current_date)::integer - 7
                       and current_date - extract(dow from current_date)::integer + 6 - 7
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
            customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_contract_date, agreement_versions.agreement_open_flag,
            reference.refinfo_cust_mailing_address,reference.refinfo_cust_mailing_address_2,reference.refinfo_cust_mailing_city, reference.refinfo_cust_mailing_state,
            reference.refinfo_cust_mailing_zip_pc)a
         LEFT JOIN
              (SELECT cust_acct_nbr, count(agreement_contract_date) as agr_count
              FROM ' || r.schema_name || '.level2_agreement_versions as av3
              JOIN ' || r.schema_name || '.level2_agreements as a3 on a3.id = av3.agreement_id
              JOIN ' || r.schema_name || '.level2_customers as c3 on a3.customer_id = c3.id
              WHERE  a3.agreement_type = ''O''
                     and av3.agreement_open_flag = ''Y''
                     and av3.agreement_contract_date not between current_date - extract(dow from current_date)::integer - 7
                     and current_date
              GROUP BY cust_acct_nbr)b   on b.cust_acct_nbr = a.unique_id
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY unique_id ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_last_week_payouts_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_last_week_payouts_vw') THEN
      DROP VIEW IF EXISTS csv_last_week_payouts_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
           stores.loc_tran_loc               AS store_id,
            ''''::text                       AS people_id,
            customers.cust_acct_nbr          AS unique_id,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            case
            when reference.refinfo_cust_mailing_address Is Null
            then customers.cust_address
            else reference.refinfo_cust_mailing_address
            end as address_1,
            case
               when reference.refinfo_cust_mailing_address_2 Is Null
               then customers.cust_address_2
               else reference.refinfo_cust_mailing_address_2
               end as address_2,
            case
               when reference.refinfo_cust_mailing_city Is Null
               then customers.cust_city
               else reference.refinfo_cust_mailing_city
               end as city,
            case
               when reference.refinfo_cust_mailing_state Is Null
               then customers.cust_state
               else reference.refinfo_cust_mailing_state
               end as state,
            case
               when reference.refinfo_cust_mailing_zip_pc Is Null
               then customers.cust_zip_pc
               else reference.refinfo_cust_mailing_zip_pc
               end as zip,
--            customers.cust_address           AS address_1,
--            customers.cust_address_2         AS address_2,
--            customers.cust_city              AS city,
--            customers.cust_state             AS state,
--            customers.cust_zip_pc            AS zip,
            customers.cust_cell_phone        AS cell_phone_number,
            customers.cust_home_phone        AS home_phone_number,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_id,
            ''''::text                       AS final_status,
            agreement_versions.agreement_closed_date
                                             AS payout_date
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            LEFT JOIN
            ' || r.schema_name || '.level2_refinfos as refereence on reference.customer_id = customers.id
         WHERE
            agreements.agreement_type = ''O''
            and agreement_versions.agreement_open_flag <> ''Y''
            and Trim(LEADING ''0'' FROM CAST(agreement_versions.agreement_closed_reason AS TEXT))In(''3'',''4'',''10'')
            and agreement_versions.agreement_closed_date
                    between current_date - extract(dow from current_date)::integer - 7
                        and current_date - extract(dow from current_date)::integer + 6 - 7
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,
            customers.cust_cell_phone,customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_closed_reason, agreement_closed_date,
            reference.refinfo_cust_mailing_address,reference.refinfo_cust_mailing_address_2,reference.refinfo_cust_mailing_city, reference.refinfo_cust_mailing_state,
            reference.refinfo_cust_mailing_zip_pc
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY unique_id ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_future_payout_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_future_payout_vw') THEN
      DROP VIEW IF EXISTS csv_future_payout_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
          ''' || r.schema_name || '''::text AS dataset,
          stores.loc_tran_loc               AS store_id,
            ''''::text                      AS people_id,
            customers.cust_acct_nbr         AS unique_id,
            customers.cust_first_name_mi    AS first_name,
            customers.cust_last_name        AS last_name,
            case
            when reference.refinfo_cust_mailing_address Is Null
            then customers.cust_address
            else reference.refinfo_cust_mailing_address
            end as address_1,
            case
               when reference.refinfo_cust_mailing_address_2 Is Null
               then customers.cust_address_2
               else reference.refinfo_cust_mailing_address_2
               end as address_2,
            case
               when reference.refinfo_cust_mailing_city Is Null
               then customers.cust_city
               else reference.refinfo_cust_mailing_city
               end as city,
            case
               when reference.refinfo_cust_mailing_state Is Null
               then customers.cust_state
               else reference.refinfo_cust_mailing_state
               end as state,
            case
               when reference.refinfo_cust_mailing_zip_pc Is Null
               then customers.cust_zip_pc
               else reference.refinfo_cust_mailing_zip_pc
               end as zip,
--            customers.cust_address          AS address_1,
--            customers.cust_address_2        AS address_2,
--            customers.cust_city             AS city,
--            customers.cust_state            AS state,
--            customers.cust_zip_pc           AS zip,
            customers.cust_cell_phone       AS cell_phone_number,
            customers.cust_home_phone       AS home_phone_number,
            customers.cust_email_address    AS email,
            agreements.agreement_number     AS agreement_id,
            SUM
              (CASE
                WHEN agreement_versions.agreement_payment_terms = ''M''
                     then Round(trunc(1.00 *
                          agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2))
                WHEN agreement_versions.agreement_payment_terms = ''W''
                     then Round((trunc(1.00 *
                          agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 4.33))
                WHEN agreement_versions.agreement_payment_terms = ''B''
                     then Round((trunc(1.00 *
                          agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                ELSE Round((trunc(1.00 *
                            agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
              END) AS number_payments_left_in_months
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            LEFT JOIN
            ' || r.schema_name || '.level2_refinfos as refereence on reference.customer_id = customers.id
         WHERE
            agreements.agreement_type = ''O''
            and agreement_versions.agreement_open_flag = ''Y''
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,
            customers.cust_cell_phone,customers.cust_home_phone,customers.cust_email_address, agreements.agreement_number,
            reference.refinfo_cust_mailing_address,reference.refinfo_cust_mailing_address_2,reference.refinfo_cust_mailing_city, reference.refinfo_cust_mailing_state,
            reference.refinfo_cust_mailing_zip_pc
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY unique_id ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_inactive_customer_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_inactive_customer_vw') THEN
      DROP VIEW IF EXISTS csv_inactive_customer_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
      SELECT a.*, customer_rating FROM
         (SELECT
           ''' || r.schema_name || '''::text AS dataset,
           stores.loc_tran_loc               AS store_id,
            ''''::text                       AS people_id,
            agreements.customer_id           AS customer_id,
            customers.cust_acct_nbr          AS unique_id,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            case
            when reference.refinfo_cust_mailing_address Is Null
            then customers.cust_address
            else reference.refinfo_cust_mailing_address
            end as address_1,
            case
               when reference.refinfo_cust_mailing_address_2 Is Null
               then customers.cust_address_2
               else reference.refinfo_cust_mailing_address_2
               end as address_2,
            case
               when reference.refinfo_cust_mailing_city Is Null
               then customers.cust_city
               else reference.refinfo_cust_mailing_city
               end as city,
            case
               when reference.refinfo_cust_mailing_state Is Null
               then customers.cust_state
               else reference.refinfo_cust_mailing_state
               end as state,
            case
               when reference.refinfo_cust_mailing_zip_pc Is Null
               then customers.cust_zip_pc
               else reference.refinfo_cust_mailing_zip_pc
               end as zip,
--            customers.cust_address           AS address_1,
--            customers.cust_address_2         AS address_2,
--            customers.cust_city              AS city,
--            customers.cust_state             AS state,
--            customers.cust_zip_pc            AS zip,
            customers.cust_cell_phone        AS cell_phone_number,
            customers.cust_home_phone        AS home_phone_number,
            customers.cust_email_address     AS email,
            customers.cust_birth_date        AS birth_day,
            agreements.agreement_number      AS agreement_id,
            agreement_versions.agreement_closed_date
                                             AS inactive_date,
            Trim(LEADING ''0'' FROM CAST(agreement_versions.agreement_closed_reason AS TEXT))
                                             AS reason_indicator,
            CASE
              WHEN Trim(LEADING ''0'' FROM CAST(agreement_versions.agreement_closed_reason AS TEXT)) = ''2'' then ''return''
              WHEN Trim(LEADING ''0'' FROM CAST(agreement_versions.agreement_closed_reason AS TEXT)) = ''3'' then ''payout''
              WHEN Trim(LEADING ''0'' FROM CAST(agreement_versions.agreement_closed_reason AS TEXT)) = ''4'' then ''payout unsatisfactory''
				  WHEN Trim(LEADING ''0'' FROM CAST(agreement_versions.agreement_closed_reason AS TEXT)) = ''5'' then ''return''
				  WHEN Trim(LEADING ''0'' FROM CAST(agreement_versions.agreement_closed_reason AS TEXT)) = ''10'' then ''buy out''
				  ELSE ''''
				END AS reason,
				sum(agreement_versions.agreement_contract_amt - agreement_contract_balance) as amount_paid
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            LEFT JOIN
            ' || r.schema_name || '.level2_refinfos as refereence on reference.customer_id = customers.id
         WHERE
            agreements.agreement_type = ''O''
            and agreement_versions.agreement_open_flag <> ''Y''
            and stores.loc_tran_active_store_indr <> ''N''
            and Trim(LEADING ''0'' FROM CAST(agreement_versions.agreement_closed_reason AS TEXT))In(''2'',''3'',''5'',''10'')
            and agreement_versions.agreement_closed_date > current_date - 1096
            and agreement_versions.agreement_closed_date  < current_date
            and customers.cust_acct_nbr not in (
              (select cust_acct_nbr from ' || r.schema_name || '.level2_customers as c2 join ' || r.schema_name || '.level2_agreements as a2 on c2.id = a2.customer_id
			                  JOIN ' || r.schema_name || '.level2_agreement_versions as av2 on a2.id = av2.agreement_id
                                 WHERE a2.agreement_type = ''O'' and av2.agreement_open_flag = ''Y''))
                                       and customers.cust_acct_nbr not in (
              (select cust_acct_nbr from ' || r.schema_name || '.level2_customers as c4 join ' || r.schema_name || '.level2_agreements as a4 on c4.id = a4.customer_id
			                  JOIN ' || r.schema_name || '.level2_agreement_versions as av4 on a4.id = av4.agreement_id
                                 WHERE a4.agreement_type = ''O'' and av4.agreement_open_flag <> ''Y'' and av4.agreement_closed_date > current_date - 1096
                                 and Trim(LEADING ''0'' FROM CAST(av4.agreement_closed_reason AS TEXT))In(''4'',''6'',''7'',''8'',''9'')))
          GROUP BY stores.loc_tran_loc, agreements.customer_id,customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
                   customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,customers.cust_home_phone,customers.cust_email_address,
                   customers.cust_birth_date, agreements.agreement_number, agreement_versions.agreement_closed_date,agreement_versions.agreement_closed_reason,
                   reference.refinfo_cust_mailing_address,reference.refinfo_cust_mailing_address_2,reference.refinfo_cust_mailing_city, reference.refinfo_cust_mailing_state,
                   reference.refinfo_cust_mailing_zip_pc)a

          JOIN
		       (select cust_acct_nbr, CASE WHEN round(sum((agreement_total_payments - tot_temp)/agreement_total_payments) * 100,2)::NUMERIC = 0 THEN 100
				                           ELSE round(sum((agreement_total_payments - tot_temp)/agreement_total_payments) * 100,2)::NUMERIC END AS customer_rating
					from
						(select cust_acct_nbr,
							Case when (sum(agreement_times_pd_1 +  agreement_times_pd_2 +  agreement_times_pd_3)::numeric) = 0
						         then 1
						         else (sum(agreement_times_pd_1 +  agreement_times_pd_2 +  agreement_times_pd_3)::numeric) end as tot_temp,
							Case when sum(agreement_tot_nbr_pmts) = 0 then 1 else sum(agreement_tot_nbr_pmts)::numeric end as agreement_total_payments
							from ' || r.schema_name || '.level2_agreement_versions avc
							join ' || r.schema_name || '.level2_agreements ac on ac.id = avc.agreement_id
							join ' || r.schema_name || '.level2_customers cc on cc.id = ac.customer_id
				    		WHERE ac.agreement_type = ''O''
						    GROUP BY cc.cust_acct_nbr)e
							GROUP BY cust_acct_nbr)d
		   ON a.unique_id = d.cust_acct_nbr

		   JOIN
           (SELECT customer_id, MAX(agreement_closed_date) as max_date
			   FROM ' || r.schema_name || '.level2_agreement_versions as av3
			   JOIN ' || r.schema_name || '.level2_agreements as a3 on a3.id = av3.agreement_id
			   JOIN ' || r.schema_name || '.level2_customers as c3 on a3.customer_id = c3.id
			   WHERE  a3.agreement_type = ''O'' and av3.agreement_open_flag <> ''Y''
                             and av3.agreement_closed_reason In(''02'',''03'',''05'',''10'') and
			            av3.agreement_closed_date > current_date - 1096 and av3.agreement_closed_date  < current_date and
			            c3.cust_acct_nbr not in (
                              (SELECT cust_acct_nbr from ' || r.schema_name || '.level2_customers as c5
                                        JOIN ' || r.schema_name || '.level2_agreements as a5 on c5.id = a5.customer_id
			                               JOIN ' || r.schema_name || '.level2_agreement_versions as av5 on a5.id = av5.agreement_id
                               WHERE a5.agreement_type = ''O''
                                       and av5.agreement_open_flag <> ''Y''
                                       and av5.agreement_closed_date > current_date - 1096
                                       and Trim(LEADING ''0''
                               FROM CAST(av5.agreement_closed_reason AS TEXT))In(''4'',''6'',''7'',''8'',''9'')))
			                      GROUP BY customer_id)b
      on b.max_date = a.inactive_date and b.customer_id = a.customer_id
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY unique_id ASC';

   EXECUTE sqlToExec;
END $$;

-- views for WOW Brand Integration including second phase added

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_active_inventory_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_active_inventory_vw') THEN
      DROP VIEW IF EXISTS csv_active_inventory_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
          ''' || r.schema_name || '''::text AS dataset,
          stores.loc_tran_loc as store_number,
          models.itemfile_nbr as sku,
          inventories.inv_desc as item_name,
          models.itemfile_desc_2 as item_description,
          count(inventories.inv_serial_nbr_key) as total_quantity
          from
          ' || r.schema_name || '.level2_inventories as inventories
          INNER JOIN
         ' || r.schema_name || '.level2_inventory_statuses as inventory_statuses on inventories.inventory_status_id = inventory_statuses.id
          INNER JOIN
          ' || r.schema_name || '.level2_locations as locations on inventories.location_id = locations.id
          INNER JOIN
          ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
          INNER JOIN
          ' || r.schema_name || '.level2_stores as stores on locations.store_id = stores.id
          where
          inventory_statuses.status_code = ''N'' or inventory_statuses.status_code = ''R''
          group by
          stores.loc_tran_loc, stores.loc_transfer_desc,models.itemfile_nbr, inventories.inv_desc,models.itemfile_desc_2
       ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_single_agreement_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_single_agreement_vw') THEN
      DROP VIEW IF EXISTS csv_single_agreement_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT a.*
          FROM
                  (SELECT
                     ''' || r.schema_name || '''::text AS dataset,
                     stores.loc_tran_loc               AS store_number,
                     customers.cust_acct_nbr           AS customer_number,
                     customers.cust_first_name_mi      AS first_name,
                     customers.cust_last_name          AS last_name,
                     customers.cust_email_address      AS email,
                     agreements.agreement_number       AS agreement_number,
                     models.itemfile_desc_1 AS product,
                     models.itemfile_desc_2 AS description,
                     inventories.inv_serial_nbr_key,
                     sum
                     (case when
                              CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)) = 0
                           then 1
                          	else
                              CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                           end)as payments_remaining

                    FROM ' || r.schema_name || '.level2_agreements as agreements
                    JOIN
                      ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
                    JOIN
                      ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
                    JOIN
                      ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
                    JOIN
                      ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
                    JOIN
                      ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
                    JOIN
                     ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
                    WHERE
                      agreements.agreement_type = ''O'' and agreement_versions.agreement_open_flag = ''Y''
                    GROUP BY
                      stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
                      customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
                      customers.cust_home_phone,customers.cust_email_address,
                      agreements.agreement_number,product,description,inventories.inv_serial_nbr_key)a
              LEFT JOIN
                       (SELECT cust_acct_nbr, count(agreement_contract_date) as agr_count
                       FROM ' || r.schema_name || '.level2_agreement_versions as av3
                       JOIN ' || r.schema_name || '.level2_agreements as a3 on a3.id = av3.agreement_id
                       JOIN ' || r.schema_name || '.level2_customers as c3 on a3.customer_id = c3.id
                       WHERE  a3.agreement_type = ''O''
                              and av3.agreement_open_flag = ''Y''
                       GROUP BY cust_acct_nbr)b   on b.cust_acct_nbr = a.customer_number
         WHERE agr_count = 1

         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_final_payment_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_final_payment_vw') THEN
      DROP VIEW IF EXISTS csv_final_payment_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_number,
            customers.cust_acct_nbr          AS customer_number,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_number,
            models.itemfile_desc_1 AS product,
            agreement_versions.agreement_closed_date
                                             AS payout_date
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            JOIN
           ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
            JOIN
           ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
            JOIN
            ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
         WHERE
            agreements.agreement_type = ''O''
            and agreement_versions.agreement_open_flag <> ''Y''
            and Trim(LEADING ''0'' FROM CAST(agreement_versions.agreement_closed_reason AS TEXT))In(''3'',''4'',''10'')
            and agreement_versions.agreement_closed_date = current_date -1
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,
            customers.cust_email_address,agreements.agreement_number,agreement_closed_date, product
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;


DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_collection_v2_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_collection_v2_vw') THEN
      DROP VIEW IF EXISTS csv_collection_v2_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
            ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc               AS store_number,
            customers.cust_acct_nbr           AS customer_number,
            customers.cust_first_name_mi      AS first_name,
            customers.cust_last_name          AS last_name,
            customers.cust_email_address      AS email,
            agreements.agreement_number       AS agreement_number,
            current_date - agreement_versions.agreement_next_due_date AS days_overdue,
            cast((case when
        		              (case
                              when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                               and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else round(trunc(1.00 * ((current_date + 30 - agreement_versions.agreement_next_due_date)/30)),2) end
                              when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                               and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                              when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                               and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                              when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                               and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                          else 0
                          end) > agreement_versions.agreement_contract_balance
                      then agreement_versions.agreement_contract_balance
                      else
                          (case
                              when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                               and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else CEIL((current_date + 30 - agreement_versions.agreement_next_due_date)/30) end
                              when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                               and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                              when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                               and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                              when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                               and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                             else 0
                             end)
                       end)AS NUMERIC(11,2))::VARCHAR AS overdue_amount,
         models.itemfile_desc_1 AS product
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            JOIN
            ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
            JOIN
            ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
            JOIN
            ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
         WHERE
            agreements.agreement_type = ''O''
            and agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,
            customers.cust_cell_phone,customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag,agreement_versions.agreement_next_due_date,agreement_versions.agreement_payment_amt,
            agreement_versions.agreement_payment_terms, agreement_versions.agreement_contract_balance,product
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_birthday_customer_v2_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_birthday_customer_v2_vw') THEN
      DROP VIEW IF EXISTS csv_birthday_customer_v2_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_number,
            customers.cust_acct_nbr          AS customer_number,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            customers.cust_email_address     AS email,
            customers.cust_birth_date        AS birth_day
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
         WHERE
            agreements.agreement_type = ''O''
            and agreement_versions.agreement_open_flag = ''Y'' and EXTRACT(MONTH FROM customers.cust_birth_date) = EXTRACT(MONTH FROM current_date)
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,
            customers.cust_cell_phone,customers.cust_home_phone,customers.cust_email_address, customers.cust_birth_date
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_account_summary_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_account_summary_vw') THEN
      DROP VIEW IF EXISTS csv_account_summary_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_number,
            customers.cust_acct_nbr          AS customer_number,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_number,
            agreement_versions.agreement_contract_date  AS date_rented,
            agreement_versions.agreement_next_due_date  AS due_date,
            round(((agreement_versions.agreement_contract_amt - agreement_versions.agreement_contract_balance) / nullif(agreement_versions.agreement_contract_amt,0) * 100),2) AS percent_ownership,
            models.itemfile_desc_1 AS product,
            round(agreement_versions.agreement_contract_amt/nullif(agreement_versions.agreement_payment_amt,0),0) AS terms,
            agreement_versions.agreement_payment_amt  AS next_payment_amount,
            customers.cust_address           AS address_1,
            customers.cust_address_2         AS address_2,
            customers.cust_city              AS city,
            customers.cust_state             AS state,
            customers.cust_zip_pc            AS zip,
            (case when
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)) = 0
            then 1
            else
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
            end)as payments_remaining,

            (select current_date +
            				(case
                              when agreement_versions.agreement_payment_terms = ''M'' then Round(trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2))
                              when agreement_versions.agreement_payment_terms = ''W'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 4.33))
                              when agreement_versions.agreement_payment_terms = ''B'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                              else Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                          end ::INTEGER * 30)) as projected_payout_date,

             min
             (case when
                      (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)*2)
                       end) = 0 then 1
             else
                 		 (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                      else CEIL((agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2) * 4.33)
                      end)
            end) AS weeks_remaining,

            min
            (case when
                     (case
                         when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                         when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                         when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     end) = 0 then 1
             else
               	    (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       end)
             end) AS months_remaining,

            case
              when agreement_versions.agreement_open_flag = ''Y''
                   and agreement_versions.agreement_next_due_date < current_date
                     then ''Y''
                   else ''N''
            end AS past_due,
            case
              when agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
              then current_date - agreement_versions.agreement_next_due_date
              else null
              end AS days_overdue,

            Cast((case when
   		             (case
                		      when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                  	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else round(trunc(1.00 * ((current_date + 30 - agreement_versions.agreement_next_due_date)/30)),2) end
                		      when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                 	         when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	      when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                           and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	 else 0
                		 end) > agreement_versions.agreement_contract_balance
            then agreement_versions.agreement_contract_balance
            else
                       (case
                            when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else CEIL((current_date + 30 - agreement_versions.agreement_next_due_date)/30) end
                            when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                            when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                            when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                       else 0
                       end)
             end) AS Numeric(11,2))::VARCHAR AS overdue_amount,

             case when (select count(customer_id) from ' || r.schema_name || '.level2_agreements ag join ' || r.schema_name || '.level2_agreement_versions av4 on av4.agreement_id = ag.id where ag.agreement_type = ''F'' and av4.agreement_open_flag = ''Y'' and ag.customer_id = customers.id)> 0 then ''Y'' else ''N'' end as club_member,
                   (select min(agreement_number) from ' || r.schema_name || '.level2_agreements ag2 join ' || r.schema_name || '.level2_agreement_versions av5 on av5.agreement_id = ag2.id where ag2.agreement_type = ''F'' and av5.agreement_open_flag = ''Y'' and ag2.customer_id = customers.id) as club_number,
                           (select coalesce(min(av2.agreement_payment_amt),0) from ' || r.schema_name || '.level2_agreements ag23
                         			  JOIN ' || r.schema_name || '.level2_agreement_versions av2 on ag23.id = av2.agreement_id
                         			  where ag23.agreement_type = ''F'' and av2.agreement_open_flag = ''Y'' and ag23.customer_id = customers.id) as club_fee,

             agreement_versions.agreement_recur_pmt_switch as autopay,
             agreement_versions.agreement_payment_terms  AS payment_terms
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            JOIN
            ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
           JOIN
            ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
            JOIN
            ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
         WHERE
            agreements.agreement_type = ''O'' and agreement_versions.agreement_open_flag = ''Y''
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.id, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
            customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag, agreement_versions.agreement_payment_terms,
            agreement_versions.agreement_contract_date,	agreement_versions.agreement_next_due_date,
            agreement_versions.agreement_payment_terms, agreement_versions.agreement_payment_amt,agreement_versions.agreement_contract_amt,
            agreement_versions.agreement_contract_balance,
            customers.cust_address,customers.cust_address_2,customers.cust_city,customers.cust_state,customers.cust_zip_pc,product,agreement_versions.agreement_recur_pmt_switch
          ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_all_rto_agreements_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_all_rto_agreements_vw') THEN
      DROP VIEW IF EXISTS csv_all_rto_agreements_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_number,
            customers.cust_acct_nbr          AS customer_number,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_number,
            agreement_versions.agreement_contract_date  AS date_rented,
            agreement_versions.agreement_next_due_date  AS due_date,
            round(((agreement_versions.agreement_contract_amt - agreement_versions.agreement_contract_balance) /nullif(agreement_versions.agreement_contract_amt,0) * 100),2) AS percent_ownership,
            models.itemfile_desc_1 AS product,
            round(agreement_versions.agreement_contract_amt/nullif(agreement_versions.agreement_payment_amt,0),0) AS terms,
            agreement_versions.agreement_payment_amt  AS next_payment_amount,
            customers.cust_address           AS address_1,
            customers.cust_address_2         AS address_2,
            customers.cust_city              AS city,
            customers.cust_state             AS state,
            customers.cust_zip_pc            AS zip,

            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)) = 0
              then 1
              else
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
              end)end) as payments_remaining,
             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (select current_date +
            				(case
                              when agreement_versions.agreement_payment_terms = ''M'' then Round(trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2))
                              when agreement_versions.agreement_payment_terms = ''W'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 4.33))
                              when agreement_versions.agreement_payment_terms = ''B'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                              else Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                          end ::INTEGER * 30))end) as projected_payout_date,

             min
             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (case when
                      (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)*2)
                       end) = 0 then 1
                else
                 		 (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                      else CEIL((agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2) * 4.33)
                      end)
                end)end) AS weeks_remaining,

            min
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     (case
                         when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                         when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                         when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     end) = 0 then 1
               else
               	    (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       end)
               end)end) AS months_remaining,

            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
              when agreement_versions.agreement_open_flag = ''Y''
                   and agreement_versions.agreement_next_due_date < current_date
              then ''Y''
              else ''N''
              end) end) AS past_due,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
              when agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
              then current_date - agreement_versions.agreement_next_due_date
              else null
              end)end) AS days_overdue,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              Cast((case when
   		             (case
                		      when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                  	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else round(trunc(1.00 * ((current_date + 30 - agreement_versions.agreement_next_due_date)/30)),2) end
                		      when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                 	         when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	      when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                           and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	 else 0
                		 end) > agreement_versions.agreement_contract_balance
                   then agreement_versions.agreement_contract_balance
                   else
                       (case
                            when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else CEIL((current_date + 30 - agreement_versions.agreement_next_due_date)/30) end
                            when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                            when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                            when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                       else 0
                       end)
                   end) AS Numeric(11,2))::VARCHAR end) AS overdue_amount,

             case when (select count(customer_id) from ' || r.schema_name || '.level2_agreements ag join ' || r.schema_name || '.level2_agreement_versions av4 on av4.agreement_id = ag.id where ag.agreement_type = ''F'' and av4.agreement_open_flag = ''Y'' and ag.customer_id = customers.id)> 0 then ''Y'' else ''N'' end as club_member,
             (select min(agreement_number) from ' || r.schema_name || '.level2_agreements ag2 join ' || r.schema_name || '.level2_agreement_versions av5 on av5.agreement_id = ag2.id where ag2.agreement_type = ''F'' and av5.agreement_open_flag = ''Y'' and ag2.customer_id = customers.id) as club_number,
             (select coalesce(min(av2.agreement_payment_amt),0) from ' || r.schema_name || '.level2_agreements ag23
                       	  JOIN ' || r.schema_name || '.level2_agreement_versions av2 on ag23.id = av2.agreement_id
                        	  where ag23.agreement_type = ''F'' and av2.agreement_open_flag = ''Y'' and ag23.customer_id = customers.id) as club_fee,

             agreement_versions.agreement_recur_pmt_switch as autopay,
             agreement_versions.agreement_open_flag as active_agreement,
             agreement_versions.agreement_payment_terms as payment_terms,
             agreement_versions.agreement_closed_date as date_closed,
             agreement_versions.agreement_closed_reason as closed_reason
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            JOIN
            ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
           JOIN
            ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
            JOIN
            ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
         WHERE
            agreements.agreement_type = ''O'' and agreement_contract_date >= (current_date - 1826) --5years
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.id, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
            customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag, agreement_versions.agreement_payment_terms,
            agreement_versions.agreement_contract_date,	agreement_versions.agreement_next_due_date,
            agreement_versions.agreement_payment_terms, agreement_versions.agreement_payment_amt,agreement_versions.agreement_contract_amt,
            agreement_versions.agreement_contract_balance,
            customers.cust_address,customers.cust_address_2,customers.cust_city,customers.cust_state,customers.cust_zip_pc,product,agreement_versions.agreement_recur_pmt_switch,
            agreement_versions.agreement_closed_date, agreement_versions.agreement_closed_reason
          ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_new_rentals_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_new_rentals_vw') THEN
      DROP VIEW IF EXISTS csv_new_rentals_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_number,
            customers.cust_acct_nbr          AS customer_number,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_number,
            agreement_versions.agreement_contract_date  AS date_rented,
            agreement_versions.agreement_next_due_date  AS due_date,
            round(((agreement_versions.agreement_contract_amt - agreement_versions.agreement_contract_balance) /nullif(agreement_versions.agreement_contract_amt,0) * 100),2) AS percent_ownership,
            models.itemfile_desc_1 AS product,
            round(agreement_versions.agreement_contract_amt/nullif(agreement_versions.agreement_payment_amt,0),0) AS terms,
            agreement_versions.agreement_payment_amt  AS next_payment_amount,
            customers.cust_address           AS address_1,
            customers.cust_address_2         AS address_2,
            customers.cust_city              AS city,
            customers.cust_state             AS state,
            customers.cust_zip_pc            AS zip,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)) = 0
              then 1
              else
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
              end)end) as payments_remaining,
             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (select current_date +
            				(case
                              when agreement_versions.agreement_payment_terms = ''M'' then Round(trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2))
                              when agreement_versions.agreement_payment_terms = ''W'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 4.33))
                              when agreement_versions.agreement_payment_terms = ''B'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                              else Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                          end ::INTEGER * 30))end) as projected_payout_date,

             min
             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (case when
                      (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)*2)
                       end) = 0 then 1
                else
                 		 (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                      else CEIL((agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2) * 4.33)
                      end)
                end)end) AS weeks_remaining,

            min
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     (case
                         when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                         when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                         when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     end) = 0 then 1
               else
               	    (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       end)
               end)end) AS months_remaining,

            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
              when agreement_versions.agreement_open_flag = ''Y''
                   and agreement_versions.agreement_next_due_date < current_date
              then ''Y''
              else ''N''
              end) end) AS past_due,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
               when agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
               then current_date - agreement_versions.agreement_next_due_date
               else null
               end)end) AS days_overdue,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              Cast((case when
   		             (case
                		      when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                  	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else round(trunc(1.00 * ((current_date + 30 - agreement_versions.agreement_next_due_date)/30)),2) end
                		      when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                 	         when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	      when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                           and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	 else 0
                		 end) > agreement_versions.agreement_contract_balance
                 then agreement_versions.agreement_contract_balance
                 else
                       (case
                            when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else CEIL((current_date + 30 - agreement_versions.agreement_next_due_date)/30) end
                            when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                            when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                            when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                       else 0
                       end)
                 end) AS Numeric(11,2))::VARCHAR end) AS overdue_amount,

             case when (select count(customer_id) from ' || r.schema_name || '.level2_agreements ag join ' || r.schema_name || '.level2_agreement_versions av4 on av4.agreement_id = ag.id where ag.agreement_type = ''F'' and av4.agreement_open_flag = ''Y'' and ag.customer_id = customers.id)> 0 then ''Y'' else ''N'' end as club_member,
             (select min(agreement_number) from ' || r.schema_name || '.level2_agreements ag2 join ' || r.schema_name || '.level2_agreement_versions av5 on av5.agreement_id = ag2.id where ag2.agreement_type = ''F'' and av5.agreement_open_flag = ''Y'' and ag2.customer_id = customers.id) as club_number,
             (select coalesce(min(av2.agreement_payment_amt),0) from ' || r.schema_name || '.level2_agreements ag23
                       	  JOIN ' || r.schema_name || '.level2_agreement_versions av2 on ag23.id = av2.agreement_id
                        	  where ag23.agreement_type = ''F'' and av2.agreement_open_flag = ''Y'' and ag23.customer_id = customers.id) as club_fee,

             agreement_versions.agreement_recur_pmt_switch as autopay,
             agreement_versions.agreement_open_flag as active_agreement,
             agreement_versions.agreement_payment_terms as payment_terms
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            JOIN
            ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
           JOIN
            ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
            JOIN
            ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
         WHERE
            agreements.agreement_type = ''O'' and agreement_contract_date >= (current_date - 30) --last 30 days
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.id, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
            customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag, agreement_versions.agreement_payment_terms,
            agreement_versions.agreement_contract_date,	agreement_versions.agreement_next_due_date,
            agreement_versions.agreement_payment_terms, agreement_versions.agreement_payment_amt,agreement_versions.agreement_contract_amt,
            agreement_versions.agreement_contract_balance,
            customers.cust_address,customers.cust_address_2,customers.cust_city,customers.cust_state,customers.cust_zip_pc,product,agreement_versions.agreement_recur_pmt_switch
          ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_returns_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_returns_vw') THEN
      DROP VIEW IF EXISTS csv_returns_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_number,
            customers.cust_acct_nbr          AS customer_number,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_number,
            agreement_versions.agreement_contract_date  AS date_rented,
            agreement_versions.agreement_next_due_date  AS due_date,
            round(((agreement_versions.agreement_contract_amt - agreement_versions.agreement_contract_balance) /nullif(agreement_versions.agreement_contract_amt,0) * 100),2) AS percent_ownership,
            models.itemfile_desc_1 AS product,
            round(agreement_versions.agreement_contract_amt/nullif(agreement_versions.agreement_payment_amt,0),0) AS terms,
            agreement_versions.agreement_payment_amt  AS next_payment_amount,
            customers.cust_address           AS address_1,
            customers.cust_address_2         AS address_2,
            customers.cust_city              AS city,
            customers.cust_state             AS state,
            customers.cust_zip_pc            AS zip,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)) = 0
               then 1
               else
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
               end)
            end) as payments_remaining,

             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (select current_date +
            				(case
                              when agreement_versions.agreement_payment_terms = ''M'' then Round(trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2))
                              when agreement_versions.agreement_payment_terms = ''W'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 4.33))
                              when agreement_versions.agreement_payment_terms = ''B'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                              else Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                          end ::INTEGER * 30))end) as projected_payout_date,

             min
             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (case when
                      (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)*2)
                       end) = 0 then 1
                else
                 		 (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                      else CEIL((agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2) * 4.33)
                      end)
                end)end) AS weeks_remaining,

            min
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     (case
                         when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                         when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                         when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     end) = 0 then 1
               else
               	    (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       end)
               end)end) AS months_remaining,

            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
               when agreement_versions.agreement_open_flag = ''Y''
                   and agreement_versions.agreement_next_due_date < current_date
               then ''Y''
               else ''N''
               end) end) AS past_due,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
              when agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
              then current_date - agreement_versions.agreement_next_due_date
              else null
              end)end) AS days_overdue,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              Cast((case when
   		             (case
                		      when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                  	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else round(trunc(1.00 * ((current_date + 30 - agreement_versions.agreement_next_due_date)/30)),2) end
                		      when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                 	         when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	      when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                           and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	 else 0
                		 end) > agreement_versions.agreement_contract_balance
                 then agreement_versions.agreement_contract_balance
                 else
                       (case
                            when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else CEIL((current_date + 30 - agreement_versions.agreement_next_due_date)/30) end
                            when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                            when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                            when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                       else 0
                       end)
                 end) AS Numeric(11,2))::VARCHAR end) AS overdue_amount,

             case when (select count(customer_id) from ' || r.schema_name || '.level2_agreements ag join ' || r.schema_name || '.level2_agreement_versions av4 on av4.agreement_id = ag.id where ag.agreement_type = ''F'' and av4.agreement_open_flag = ''Y'' and ag.customer_id = customers.id)> 0 then ''Y'' else ''N'' end as club_member,
             (select min(agreement_number) from ' || r.schema_name || '.level2_agreements ag2 join ' || r.schema_name || '.level2_agreement_versions av5 on av5.agreement_id = ag2.id where ag2.agreement_type = ''F'' and av5.agreement_open_flag = ''Y'' and ag2.customer_id = customers.id) as club_number,
             (select coalesce(min(av2.agreement_payment_amt),0) from ' || r.schema_name || '.level2_agreements ag23
                       	  JOIN ' || r.schema_name || '.level2_agreement_versions av2 on ag23.id = av2.agreement_id
                        	  where ag23.agreement_type = ''F'' and av2.agreement_open_flag = ''Y'' and ag23.customer_id = customers.id) as club_fee,

             agreement_versions.agreement_recur_pmt_switch as autopay,
             agreement_versions.agreement_open_flag as active_agreement,
             agreement_versions.agreement_payment_terms as payment_terms,
             agreement_versions.agreement_closed_date as date_closed,
             agreement_versions.agreement_closed_reason as closed_reason
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            JOIN
            ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
           JOIN
            ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
            JOIN
            ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
         WHERE
            agreements.agreement_type = ''O'' and agreement_closed_date >= (current_date - 120) and agreement_closed_reason In(''02'',''05'') --last 120 days and refund or requested pickup
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.id, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
            customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag, agreement_versions.agreement_payment_terms,
            agreement_versions.agreement_contract_date,	agreement_versions.agreement_next_due_date,
            agreement_versions.agreement_payment_terms, agreement_versions.agreement_payment_amt,agreement_versions.agreement_contract_amt,
            agreement_versions.agreement_contract_balance,
            customers.cust_address,customers.cust_address_2,customers.cust_city,customers.cust_state,customers.cust_zip_pc,product,agreement_versions.agreement_recur_pmt_switch,agreement_versions.agreement_payment_terms, agreement_versions.agreement_closed_date, agreement_versions.agreement_closed_reason
          ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_lost_customer_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_lost_customer_vw') THEN
      DROP VIEW IF EXISTS csv_lost_customer_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_number,
            customers.cust_acct_nbr          AS customer_number,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_number,
            agreement_versions.agreement_contract_date  AS date_rented,
            agreement_versions.agreement_next_due_date  AS due_date,
            round(((agreement_versions.agreement_contract_amt - agreement_versions.agreement_contract_balance) /nullif(agreement_versions.agreement_contract_amt,0) * 100),2) AS percent_ownership,
            models.itemfile_desc_1 AS product,
            round(agreement_versions.agreement_contract_amt/nullif(agreement_versions.agreement_payment_amt,0),0) AS terms,
            agreement_versions.agreement_payment_amt  AS next_payment_amount,
            customers.cust_address           AS address_1,
            customers.cust_address_2         AS address_2,
            customers.cust_city              AS city,
            customers.cust_state             AS state,
            customers.cust_zip_pc            AS zip,

            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)) = 0
               then 1
               else
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
               end)
            end) as payments_remaining,

             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (select current_date +
            				(case
                              when agreement_versions.agreement_payment_terms = ''M'' then Round(trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2))
                              when agreement_versions.agreement_payment_terms = ''W'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 4.33))
                              when agreement_versions.agreement_payment_terms = ''B'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                              else Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                          end ::INTEGER * 30))end) as projected_payout_date,

             min
             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (case when
                      (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)*2)
                       end) = 0 then 1
                else
                 		 (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                      else CEIL((agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2) * 4.33)
                      end)
                end)end) AS weeks_remaining,

            min
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     (case
                         when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                         when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                         when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     end) = 0 then 1
               else
               	    (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       end)
               end)end) AS months_remaining,

            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
               when agreement_versions.agreement_open_flag = ''Y''
                   and agreement_versions.agreement_next_due_date < current_date
               then ''Y''
               else ''N''
               end) end) AS past_due,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
              when agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
              then current_date - agreement_versions.agreement_next_due_date
              else null
              end)end) AS days_overdue,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              Cast((case when
   		             (case
                		      when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                  	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else round(trunc(1.00 * ((current_date + 30 - agreement_versions.agreement_next_due_date)/30)),2) end
                		      when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                 	         when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	      when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                           and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	 else 0
                		 end) > agreement_versions.agreement_contract_balance
                 then agreement_versions.agreement_contract_balance
                 else
                       (case
                            when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else CEIL((current_date + 30 - agreement_versions.agreement_next_due_date)/30) end
                            when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                            when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                            when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                       else 0
                       end)
                 end) AS Numeric(11,2))::VARCHAR end) AS overdue_amount,

             case when (select count(customer_id) from ' || r.schema_name || '.level2_agreements ag join ' || r.schema_name || '.level2_agreement_versions av4 on av4.agreement_id = ag.id where ag.agreement_type = ''F'' and av4.agreement_open_flag = ''Y'' and ag.customer_id = customers.id)> 0 then ''Y'' else ''N'' end as club_member,
             (select min(agreement_number) from ' || r.schema_name || '.level2_agreements ag2 join ' || r.schema_name || '.level2_agreement_versions av5 on av5.agreement_id = ag2.id where ag2.agreement_type = ''F'' and av5.agreement_open_flag = ''Y'' and ag2.customer_id = customers.id) as club_number,
             (select coalesce(min(av2.agreement_payment_amt),0) from ' || r.schema_name || '.level2_agreements ag23
                       	  JOIN ' || r.schema_name || '.level2_agreement_versions av2 on ag23.id = av2.agreement_id
                        	  where ag23.agreement_type = ''F'' and av2.agreement_open_flag = ''Y'' and ag23.customer_id = customers.id) as club_fee,

             agreement_versions.agreement_recur_pmt_switch as autopay,
             agreement_versions.agreement_open_flag as active_agreement,
             agreement_versions.agreement_payment_terms as payment_terms,
             agreement_versions.agreement_closed_date as date_closed,
             agreement_versions.agreement_closed_reason as closed_reason
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            JOIN
            ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
           JOIN
            ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
            JOIN
            ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
         WHERE
            agreements.agreement_type = ''O'' and agreement_closed_date >= (current_date - 274) and agreement_closed_reason In(''02'',''05'',''03'',''04'',''10'') --last 9 months payouts or returns
            and customers.cust_acct_nbr not in (
              (select cust_acct_nbr from ' || r.schema_name || '.level2_customers as c2 join ' || r.schema_name || '.level2_agreements as a2 on c2.id = a2.customer_id
			                  JOIN ' || r.schema_name || '.level2_agreement_versions as av2 on a2.id = av2.agreement_id
                                 WHERE a2.agreement_type = ''O'' and av2.agreement_open_flag = ''Y''))
                                       and customers.cust_acct_nbr not in (
              (select cust_acct_nbr from ' || r.schema_name || '.level2_customers as c4 join ' || r.schema_name || '.level2_agreements as a4 on c4.id = a4.customer_id
			                  JOIN ' || r.schema_name || '.level2_agreement_versions as av4 on a4.id = av4.agreement_id
                                 WHERE a4.agreement_type = ''O'' and av4.agreement_open_flag <> ''Y'' and av4.agreement_closed_date > current_date - 274
                                 and Trim(LEADING ''0'' FROM CAST(av4.agreement_closed_reason AS TEXT))In(''6'',''7'',''8'',''9'')))
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.id, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
            customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag, agreement_versions.agreement_payment_terms,
            agreement_versions.agreement_contract_date,	agreement_versions.agreement_next_due_date,
            agreement_versions.agreement_payment_terms, agreement_versions.agreement_payment_amt,agreement_versions.agreement_contract_amt,
            agreement_versions.agreement_contract_balance,
            customers.cust_address,customers.cust_address_2,customers.cust_city,customers.cust_state,customers.cust_zip_pc,product,agreement_versions.agreement_recur_pmt_switch,agreement_versions.agreement_payment_terms,
            agreement_versions.agreement_closed_date, agreement_versions.agreement_closed_reason
         ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_at_risk_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_at_risk_vw') THEN
      DROP VIEW IF EXISTS csv_at_risk_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_number,
            customers.cust_acct_nbr          AS customer_number,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_number,
            agreement_versions.agreement_contract_date  AS date_rented,
            agreement_versions.agreement_next_due_date  AS due_date,
            round(((agreement_versions.agreement_contract_amt - agreement_versions.agreement_contract_balance) /nullif(agreement_versions.agreement_contract_amt,0) * 100),2) AS percent_ownership,
            models.itemfile_desc_1 AS product,
            round(agreement_versions.agreement_contract_amt/nullif(agreement_versions.agreement_payment_amt,0),0) AS terms,
            agreement_versions.agreement_payment_amt  AS next_payment_amount,
            customers.cust_address           AS address_1,
            customers.cust_address_2         AS address_2,
            customers.cust_city              AS city,
            customers.cust_state             AS state,
            customers.cust_zip_pc            AS zip,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)) = 0
               then 1
               else
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
               end)end) as payments_remaining,
             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (select current_date +
            				(case
                              when agreement_versions.agreement_payment_terms = ''M'' then Round(trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2))
                              when agreement_versions.agreement_payment_terms = ''W'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 4.33))
                              when agreement_versions.agreement_payment_terms = ''B'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                              else Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                          end ::INTEGER * 30))end) as projected_payout_date,

             min
             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (case when
                      (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)*2)
                       end) = 0 then 1
                else
                 		 (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                      else CEIL((agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2) * 4.33)
                      end)
                end)end) AS weeks_remaining,

            min
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     (case
                         when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                         when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                         when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     end) = 0 then 1
               else
               	    (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       end)
               end)end) AS months_remaining,

            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
               when agreement_versions.agreement_open_flag = ''Y''
                   and agreement_versions.agreement_next_due_date < current_date
               then ''Y''
               else ''N''
               end) end) AS past_due,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
              when agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
              then current_date - agreement_versions.agreement_next_due_date
              else null
              end)end) AS days_overdue,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              Cast((case when
   		             (case
                		      when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                  	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else round(trunc(1.00 * ((current_date + 30 - agreement_versions.agreement_next_due_date)/30)),2) end
                		      when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                 	         when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	      when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                           and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	 else 0
                		 end) > agreement_versions.agreement_contract_balance
                 then agreement_versions.agreement_contract_balance
                 else
                       (case
                            when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else CEIL((current_date + 30 - agreement_versions.agreement_next_due_date)/30) end
                            when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                            when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                            when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                       else 0
                       end)
                 end) AS Numeric(11,2))::VARCHAR end) AS overdue_amount,

             case when (select count(customer_id) from ' || r.schema_name || '.level2_agreements ag join ' || r.schema_name || '.level2_agreement_versions av4 on av4.agreement_id = ag.id where ag.agreement_type = ''F'' and av4.agreement_open_flag = ''Y'' and ag.customer_id = customers.id)> 0 then ''Y'' else ''N'' end as club_member,
             (select min(agreement_number) from ' || r.schema_name || '.level2_agreements ag2 join ' || r.schema_name || '.level2_agreement_versions av5 on av5.agreement_id = ag2.id where ag2.agreement_type = ''F'' and av5.agreement_open_flag = ''Y'' and ag2.customer_id = customers.id) as club_number,
             (select coalesce(min(av2.agreement_payment_amt),0) from ' || r.schema_name || '.level2_agreements ag23
                       	  JOIN ' || r.schema_name || '.level2_agreement_versions av2 on ag23.id = av2.agreement_id
                        	  where ag23.agreement_type = ''F'' and av2.agreement_open_flag = ''Y'' and ag23.customer_id = customers.id) as club_fee,

             agreement_versions.agreement_recur_pmt_switch as autopay,
             agreement_versions.agreement_open_flag as active_agreement,
             agreement_versions.agreement_payment_terms as payment_terms
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            JOIN
            ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
           JOIN
            ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
            JOIN
            ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
         WHERE
            agreements.agreement_type = ''O'' and agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.id, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
            customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag, agreement_versions.agreement_payment_terms,
            agreement_versions.agreement_contract_date,	agreement_versions.agreement_next_due_date,
            agreement_versions.agreement_payment_terms, agreement_versions.agreement_payment_amt,agreement_versions.agreement_contract_amt,
            agreement_versions.agreement_contract_balance,
            customers.cust_address,customers.cust_address_2,customers.cust_city,customers.cust_state,customers.cust_zip_pc,product,agreement_versions.agreement_recur_pmt_switch, agreement_versions.agreement_payment_terms
          ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;

DO $$
DECLARE
   argsDatasets TEXT[] := STRING_TO_ARRAY(CURRENT_SETTING('args.datasets'), ',');
   r RECORD;
   sqlToExec VARCHAR;
   unionAll VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW csv_payouts_next_30_vw AS';
   unionAll := '';

   IF EXISTS(SELECT 1 FROM information_schema.views WHERE table_name = 'csv_payouts_next_30_vw') THEN
      DROP VIEW IF EXISTS csv_payouts_next_30_vw CASCADE;
   END IF;

   FOR r IN SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY(argsDatasets)
   LOOP
      sqlToExec := sqlToExec
      || ' '
      || unionAll || '
         SELECT
           ''' || r.schema_name || '''::text AS dataset,
            stores.loc_tran_loc              AS store_number,
            customers.cust_acct_nbr          AS customer_number,
            customers.cust_first_name_mi     AS first_name,
            customers.cust_last_name         AS last_name,
            customers.cust_email_address     AS email,
            agreements.agreement_number      AS agreement_number,
            agreement_versions.agreement_contract_date  AS date_rented,
            agreement_versions.agreement_next_due_date  AS due_date,
            round(((agreement_versions.agreement_contract_amt - agreement_versions.agreement_contract_balance) /nullif(agreement_versions.agreement_contract_amt,0) * 100),2) AS percent_ownership,
            models.itemfile_desc_1 AS product,
            round(agreement_versions.agreement_contract_amt/nullif(agreement_versions.agreement_payment_amt,0),0) AS terms,
            agreement_versions.agreement_payment_amt  AS next_payment_amount,
            customers.cust_address           AS address_1,
            customers.cust_address_2         AS address_2,
            customers.cust_city              AS city,
            customers.cust_state             AS state,
            customers.cust_zip_pc            AS zip,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)) = 0
               then 1
               else
                     CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
               end)
            end) as payments_remaining,

             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (select current_date +
            				(case
                              when agreement_versions.agreement_payment_terms = ''M'' then Round(trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2))
                              when agreement_versions.agreement_payment_terms = ''W'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 4.33))
                              when agreement_versions.agreement_payment_terms = ''B'' then Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                              else Round((trunc(1.00 * agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt, 0), 2) / 2))
                          end ::INTEGER * 30))end) as projected_payout_date,

             min
             (case when agreement_versions.agreement_open_flag = ''Y'' then
               (case when
                      (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)*2)
                       end) = 0 then 1
                else
                 		 (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) * 4.33)
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2 * 4.33)
                      else CEIL((agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0)/ 2) * 4.33)
                      end)
                end)end) AS weeks_remaining,

            min
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case when
                     (case
                         when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                         when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                         when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                     end) = 0 then 1
               else
               	    (case
                          when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                          when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                          when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                       end)
               end)end) AS months_remaining,

            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
               when agreement_versions.agreement_open_flag = ''Y''
                   and agreement_versions.agreement_next_due_date < current_date
               then ''Y''
               else ''N''
               end) end) AS past_due,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              (case
              when agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date < current_date
              then current_date - agreement_versions.agreement_next_due_date
              else null
              end)end) AS days_overdue,
            (case when agreement_versions.agreement_open_flag = ''Y'' then
              Cast((case when
   		             (case
                		      when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                  	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else round(trunc(1.00 * ((current_date + 30 - agreement_versions.agreement_next_due_date)/30)),2) end
                		      when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                 	         when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                   	      and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	      when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                           and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                	 	 else 0
                		 end) > agreement_versions.agreement_contract_balance
                 then agreement_versions.agreement_contract_balance
                 else
                       (case
                            when agreement_versions.agreement_payment_terms = ''M'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * case when ((current_date - agreement_versions.agreement_next_due_date)) < 30 then 1 else CEIL((current_date + 30 - agreement_versions.agreement_next_due_date)/30) end
                            when agreement_versions.agreement_payment_terms = ''W'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 7 - agreement_versions.agreement_next_due_date) / 7),0),2)
                            when agreement_versions.agreement_payment_terms = ''B'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                            when agreement_versions.agreement_payment_terms = ''S'' and agreement_versions.agreement_open_flag = ''Y''
                            and agreement_versions.agreement_next_due_date < current_date then nullif(agreement_versions.agreement_payment_amt, 0) * round(trunc(1.00 * ((current_date + 14 - agreement_versions.agreement_next_due_date) / 15),0 + 1),2)
                       else 0
                       end)
                 end) AS Numeric(11,2))::VARCHAR end) AS overdue_amount,

             case when (select count(customer_id) from ' || r.schema_name || '.level2_agreements ag join ' || r.schema_name || '.level2_agreement_versions av4 on av4.agreement_id = ag.id where ag.agreement_type = ''F'' and av4.agreement_open_flag = ''Y'' and ag.customer_id = customers.id)> 0 then ''Y'' else ''N'' end as club_member,
             (select min(agreement_number) from ' || r.schema_name || '.level2_agreements ag2 join ' || r.schema_name || '.level2_agreement_versions av5 on av5.agreement_id = ag2.id where ag2.agreement_type = ''F'' and av5.agreement_open_flag = ''Y'' and ag2.customer_id = customers.id) as club_number,
             (select coalesce(min(av2.agreement_payment_amt),0) from ' || r.schema_name || '.level2_agreements ag23
                       	  JOIN ' || r.schema_name || '.level2_agreement_versions av2 on ag23.id = av2.agreement_id
                        	  where ag23.agreement_type = ''F'' and av2.agreement_open_flag = ''Y'' and ag23.customer_id = customers.id) as club_fee,

             agreement_versions.agreement_recur_pmt_switch as autopay,
             agreement_versions.agreement_open_flag as active_agreement,
             agreement_versions.agreement_payment_terms as payment_terms
         FROM ' || r.schema_name || '.level2_agreements as agreements
            JOIN
            ' || r.schema_name || '.level2_agreement_versions as agreement_versions on agreements.id = agreement_versions.agreement_id
            JOIN
            ' || r.schema_name || '.level2_stores as stores on agreement_versions.store_id = stores.id
            JOIN
            ' || r.schema_name || '.level2_customers as customers on agreements.customer_id = customers.id
            JOIN
            ' || r.schema_name || '.level2_agreement_version_inventories as agreement_version_inventories on agreement_versions.id = agreement_version_inventories.agreement_version_id
           JOIN
            ' || r.schema_name || '.level2_inventories as inventories on agreement_version_inventories.inventory_id = inventories.id
            JOIN
            ' || r.schema_name || '.level2_models as models on inventories.model_id = models.id
          WHERE
               agreements.agreement_type = ''O'' and agreement_versions.agreement_open_flag = ''Y'' and agreement_versions.agreement_next_due_date >= current_date and
         			(case when agreement_versions.agreement_open_flag = ''Y'' then
                       (case when
                              (case
                                  when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                                  when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                                  when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                              else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                              end) = 0 then 1
                        else
                        	    (case
                                   when agreement_versions.agreement_payment_terms = ''M'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0))
                                   when agreement_versions.agreement_payment_terms = ''W'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 4.33)
                                   when agreement_versions.agreement_payment_terms = ''B'' then CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                                else CEIL(agreement_versions.agreement_contract_balance/nullif(agreement_versions.agreement_payment_amt,0) / 2)
                                end)
                        end)end) <=1
         GROUP BY
            stores.loc_tran_loc, customers.cust_acct_nbr, customers.id, customers.cust_first_name_mi, customers.cust_last_name,customers.cust_address,
            customers.cust_address_2, customers.cust_city,customers.cust_state,customers.cust_zip_pc,customers.cust_cell_phone,
            customers.cust_home_phone,customers.cust_email_address,
            agreements.agreement_number,agreement_versions.agreement_open_flag, agreement_versions.agreement_payment_terms,
            agreement_versions.agreement_contract_date,	agreement_versions.agreement_next_due_date,
            agreement_versions.agreement_payment_terms, agreement_versions.agreement_payment_amt,agreement_versions.agreement_contract_amt,
            agreement_versions.agreement_contract_balance,
            customers.cust_address,customers.cust_address_2,customers.cust_city,customers.cust_state,customers.cust_zip_pc,product,agreement_versions.agreement_recur_pmt_switch, agreement_versions.agreement_payment_terms
        ';

      unionAll := ' UNION ALL ';
   END LOOP;
   sqlToExec := sqlToExec || 'ORDER BY store_number, customer_number, agreement_number ASC';

   EXECUTE sqlToExec;
END $$;


-- End fastinfo setup

-- Begin cynergidb setup
\c cynergidb
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
CREATE EXTENSION IF NOT EXISTS postgres_fdw;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
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
    invoice_number VARCHAR,
    inv_invoice_expensed_date DATE,
    inv_purchase_order_number VARCHAR,
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
    number INTEGER,
    account_payable_security INTEGER,
    purchase_order_security INTEGER,
    general_ledger_security INTEGER,
    system_administration_security INTEGER,
    file_maintenance_security INTEGER,
    bank_reconciliation_security INTEGER
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


CREATE FOREIGN TABLE fastinfo_prod_import.csv_active_customer_vw (
   dataset VARCHAR,
   store_id INTEGER,
   people_id VARCHAR,
   unique_id VARCHAR,
   first_name VARCHAR,
   last_name VARCHAR,
   address_1 VARCHAR,
   address_2 VARCHAR,
   city VARCHAR,
   state VARCHAR,
   zip VARCHAR,
   cell_phone_number VARCHAR,
   home_phone_number VARCHAR,
   email VARCHAR,
   agreement_id VARCHAR,
   payment_frequency VARCHAR,
   text_opt_in VARCHAR,
   online_indicator VARCHAR,
   care_plus VARCHAR,
   projected_payout INTEGER,
   payments_left_in_weeks INTEGER,
   past_due VARCHAR,
   days_past_due INTEGER
) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_active_customer_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_collection_vw (
   dataset VARCHAR,
   store_id INTEGER,
   people_id VARCHAR,
   unique_id VARCHAR,
   first_name VARCHAR,
   last_name VARCHAR,
   address_1 VARCHAR,
   address_2 VARCHAR,
   city VARCHAR,
   state VARCHAR,
   zip VARCHAR,
   cell_phone_number VARCHAR,
   home_phone_number VARCHAR,
   email VARCHAR,
   agreement_id VARCHAR,
   days_late INTEGER
) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_collection_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_birthday_customer_vw (
   dataset VARCHAR,
   store_id INTEGER,
   people_id VARCHAR,
   unique_id VARCHAR,
   first_name VARCHAR,
   last_name VARCHAR,
   address_1 VARCHAR,
   address_2 VARCHAR,
   city VARCHAR,
   state VARCHAR,
   zip VARCHAR,
   cell_phone_number VARCHAR,
   home_phone_number VARCHAR,
   email VARCHAR,
   birth_day DATE
) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_birthday_customer_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_last_week_deliveries_vw (
   dataset VARCHAR,
   store_id INTEGER,
   people_id VARCHAR,
   unique_id VARCHAR,
   first_name VARCHAR,
   last_name VARCHAR,
   address_1 VARCHAR,
   address_2 VARCHAR,
   city VARCHAR,
   state VARCHAR,
   zip VARCHAR,
   cell_phone_number VARCHAR,
   home_phone_number VARCHAR,
   email VARCHAR,
   agreement_id VARCHAR,
   purchase_date DATE,
   current_customer_status VARCHAR,
   new_customer VARCHAR
) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_last_week_deliveries_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_last_week_payouts_vw (
   dataset VARCHAR,
   store_id INTEGER,
   people_id VARCHAR,
   unique_id VARCHAR,
   first_name VARCHAR,
   last_name VARCHAR,
   address_1 VARCHAR,
   address_2 VARCHAR,
   city VARCHAR,
   state VARCHAR,
   zip VARCHAR,
   cell_phone_number VARCHAR,
   home_phone_number VARCHAR,
   email VARCHAR,
   agreement_id VARCHAR,
   final_status VARCHAR,
   payout_date DATE
) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_last_week_payouts_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_future_payout_vw (
   dataset VARCHAR,
   store_id INTEGER,
   people_id VARCHAR,
   unique_id VARCHAR,
   first_name VARCHAR,
   last_name VARCHAR,
   address_1 VARCHAR,
   address_2 VARCHAR,
   city VARCHAR,
   state VARCHAR,
   zip VARCHAR,
   cell_phone_number VARCHAR,
   home_phone_number VARCHAR,
   email VARCHAR,
   agreement_id VARCHAR,
   number_payments_left_in_months INTEGER
) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_future_payout_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_inactive_customer_vw (
   dataset VARCHAR,
   store_id INTEGER,
   people_id VARCHAR,
   unique_id VARCHAR,
   first_name VARCHAR,
   last_name VARCHAR,
   address_1 VARCHAR,
   address_2 VARCHAR,
   city VARCHAR,
   state VARCHAR,
   zip VARCHAR,
   cell_phone_number VARCHAR,
   home_phone_number VARCHAR,
   email VARCHAR,
   birth_day DATE,
   agreement_id VARCHAR,
   inactive_date DATE,
   reason_indicator VARCHAR,
   reason VARCHAR,
   amount_paid NUMERIC,
   customer_rating VARCHAR
 ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_inactive_customer_vw', SCHEMA_NAME 'public');

 CREATE FOREIGN TABLE fastinfo_prod_import.csv_active_inventory_vw (
    dataset VARCHAR,
    store_number INTEGER,
    sku VARCHAR,
    item_name VARCHAR,
    item_description VARCHAR,
    total_quantity INTEGER
 ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_active_inventory_vw', SCHEMA_NAME 'public');

 CREATE FOREIGN TABLE fastinfo_prod_import.csv_single_agreement_vw (
     dataset VARCHAR,
     store_number INTEGER,
     customer_number VARCHAR,
     first_name VARCHAR,
     last_name VARCHAR,
     email VARCHAR,
     agreement_number VARCHAR,
     product VARCHAR,
     description VARCHAR,
     payments_remaining INTEGER
  ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_single_agreement_vw', SCHEMA_NAME 'public');

 CREATE FOREIGN TABLE fastinfo_prod_import.csv_final_payment_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       product VARCHAR,
       payout_date DATE
    ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_final_payment_vw', SCHEMA_NAME 'public');


CREATE FOREIGN TABLE fastinfo_prod_import.csv_birthday_customer_v2_vw (
   dataset VARCHAR,
   store_number INTEGER,
   customer_number VARCHAR,
   first_name VARCHAR,
   last_name VARCHAR,
   email VARCHAR,
   birth_day DATE
) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_birthday_customer_v2_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_collection_v2_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       days_overdue INTEGER,
       overdue_amount NUMERIC,
       product VARCHAR
    ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_collection_v2_vw', SCHEMA_NAME 'public');


 CREATE FOREIGN TABLE fastinfo_prod_import.csv_account_summary_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       date_rented DATE,
       due_date DATE,
       percent_ownership NUMERIC,
       product VARCHAR,
       terms INTEGER,
       next_payment_amount NUMERIC,
       address_1 VARCHAR,
       address_2 VARCHAR,
       city VARCHAR,
       state VARCHAR,
       zip VARCHAR,
       payments_remaining INTEGER,
       projected_payout_date DATE,
       weeks_remaining INTEGER,
       months_remaining INTEGER,
       past_due VARCHAR,
       days_overdue INTEGER,
       overdue_amount NUMERIC,
       club_member VARCHAR,
       club_number VARCHAR,
       club_fee NUMERIC,
       autopay VARCHAR,
       payment_terms VARCHAR
  ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_account_summary_vw', SCHEMA_NAME 'public');


CREATE FOREIGN TABLE fastinfo_prod_import.csv_all_rto_agreements_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       date_rented DATE,
       due_date DATE,
       percent_ownership NUMERIC,
       product VARCHAR,
       terms INTEGER,
       next_payment_amount NUMERIC,
       address_1 VARCHAR,
       address_2 VARCHAR,
       city VARCHAR,
       state VARCHAR,
       zip VARCHAR,
       payments_remaining INTEGER,
       projected_payout_date DATE,
       weeks_remaining INTEGER,
       months_remaining INTEGER,
       past_due VARCHAR,
       days_overdue INTEGER,
       overdue_amount NUMERIC,
       club_member VARCHAR,
       club_number VARCHAR,
       club_fee NUMERIC,
       autopay VARCHAR,
       active_agreement VARCHAR,
       payment_terms VARCHAR,
       date_closed DATE,
       closed_reason INTEGER
  ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_all_rto_agreements_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_new_rentals_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       date_rented DATE,
       due_date DATE,
       percent_ownership NUMERIC,
       product VARCHAR,
       terms INTEGER,
       next_payment_amount NUMERIC,
       address_1 VARCHAR,
       address_2 VARCHAR,
       city VARCHAR,
       state VARCHAR,
       zip VARCHAR,
       payments_remaining INTEGER,
       projected_payout_date DATE,
       weeks_remaining INTEGER,
       months_remaining INTEGER,
       past_due VARCHAR,
       days_overdue INTEGER,
       overdue_amount NUMERIC,
       club_member VARCHAR,
       club_number VARCHAR,
       club_fee NUMERIC,
       autopay VARCHAR,
       active_agreement VARCHAR,
       payment_terms VARCHAR
  ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_new_rentals_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_returns_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       date_rented DATE,
       due_date DATE,
       percent_ownership NUMERIC,
       product VARCHAR,
       terms INTEGER,
       next_payment_amount NUMERIC,
       address_1 VARCHAR,
       address_2 VARCHAR,
       city VARCHAR,
       state VARCHAR,
       zip VARCHAR,
       payments_remaining INTEGER,
       projected_payout_date DATE,
       weeks_remaining INTEGER,
       months_remaining INTEGER,
       past_due VARCHAR,
       days_overdue INTEGER,
       overdue_amount NUMERIC,
       club_member VARCHAR,
       club_number VARCHAR,
       club_fee NUMERIC,
       autopay VARCHAR,
       active_agreement VARCHAR,
       payment_terms VARCHAR,
       date_closed DATE,
       closed_reason INTEGER
  ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_returns_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_lost_customer_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       date_rented DATE,
       due_date DATE,
       percent_ownership NUMERIC,
       product VARCHAR,
       terms INTEGER,
       next_payment_amount NUMERIC,
       address_1 VARCHAR,
       address_2 VARCHAR,
       city VARCHAR,
       state VARCHAR,
       zip VARCHAR,
       payments_remaining INTEGER,
       projected_payout_date DATE,
       weeks_remaining INTEGER,
       months_remaining INTEGER,
       past_due VARCHAR,
       days_overdue INTEGER,
       overdue_amount NUMERIC,
       club_member VARCHAR,
       club_number VARCHAR,
       club_fee NUMERIC,
       autopay VARCHAR,
       active_agreement VARCHAR,
       payment_terms VARCHAR,
       date_closed DATE,
       closed_reason INTEGER
  ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_lost_customer_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_payouts_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       date_rented DATE,
       due_date DATE,
       percent_ownership NUMERIC,
       product VARCHAR,
       terms INTEGER,
       next_payment_amount NUMERIC,
       address_1 VARCHAR,
       address_2 VARCHAR,
       city VARCHAR,
       state VARCHAR,
       zip VARCHAR,
       payments_remaining INTEGER,
       projected_payout_date DATE,
       weeks_remaining INTEGER,
       months_remaining INTEGER,
       past_due VARCHAR,
       days_overdue INTEGER,
       overdue_amount NUMERIC,
       club_member VARCHAR,
       club_number VARCHAR,
       club_fee NUMERIC,
       autopay VARCHAR,
       active_agreement VARCHAR,
       payment_terms VARCHAR,
       date_closed DATE,
       closed_reason INTEGER
  ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_payouts_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_at_risk_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       date_rented DATE,
       due_date DATE,
       percent_ownership NUMERIC,
       product VARCHAR,
       terms INTEGER,
       next_payment_amount NUMERIC,
       address_1 VARCHAR,
       address_2 VARCHAR,
       city VARCHAR,
       state VARCHAR,
       zip VARCHAR,
       payments_remaining INTEGER,
       projected_payout_date DATE,
       weeks_remaining INTEGER,
       months_remaining INTEGER,
       past_due VARCHAR,
       days_overdue INTEGER,
       overdue_amount NUMERIC,
       club_member VARCHAR,
       club_number VARCHAR,
       club_fee NUMERIC,
       autopay VARCHAR,
       active_agreement VARCHAR,
       payment_terms VARCHAR
  ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_at_risk_vw', SCHEMA_NAME 'public');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_payouts_next_30_vw (
       dataset VARCHAR,
       store_number INTEGER,
       customer_number VARCHAR,
       first_name VARCHAR,
       last_name VARCHAR,
       email VARCHAR,
       agreement_number VARCHAR,
       date_rented DATE,
       due_date DATE,
       percent_ownership NUMERIC,
       product VARCHAR,
       terms INTEGER,
       next_payment_amount NUMERIC,
       address_1 VARCHAR,
       address_2 VARCHAR,
       city VARCHAR,
       state VARCHAR,
       zip VARCHAR,
       payments_remaining INTEGER,
       projected_payout_date DATE,
       weeks_remaining INTEGER,
       months_remaining INTEGER,
       past_due VARCHAR,
       days_overdue INTEGER,
       overdue_amount NUMERIC,
       club_member VARCHAR,
       club_number VARCHAR,
       club_fee NUMERIC,
       autopay VARCHAR,
       active_agreement VARCHAR,
       payment_terms VARCHAR
  ) SERVER fastinfo OPTIONS (TABLE_NAME 'csv_payouts_next_30_vw', SCHEMA_NAME 'public');

GRANT USAGE ON SCHEMA fastinfo_prod_import TO cynergiuser;
GRANT SELECT ON ALL TABLES IN SCHEMA fastinfo_prod_import TO cynergiuser;

-- End cynergidb setup
