CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS file_fdw;
CREATE SCHEMA fastinfo_prod_import;

CREATE TABLE fastinfo_prod_import.store_vw (
   id           BIGSERIAL                                             NOT NULL,
   number       INTEGER,
   name         VARCHAR(27),
   dataset      VARCHAR(6)                                            NOT NULL,
   time_created TIMESTAMPTZ  DEFAULT clock_timestamp()                NOT NULL,
   time_updated TIMESTAMPTZ  DEFAULT clock_timestamp()                NOT NULL
);
COPY fastinfo_prod_import.store_vw(
id, dataset, number, name
)
FROM '/tmp/fastinfo/test-store.csv' DELIMITER ',' CSV HEADER;

CREATE TABLE fastinfo_prod_import.department_vw (
    id               BIGSERIAL                              NOT NULL,
    code             VARCHAR(2)                             NOT NULL,
    description      VARCHAR(12),
    dataset          VARCHAR(6)                             NOT NULL,
    time_created     TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    time_updated     TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL
);
COPY fastinfo_prod_import.department_vw(
id, dataset, code, description
)
FROM '/tmp/fastinfo/test-department.csv' DELIMITER ',' CSV HEADER;


CREATE TABLE fastinfo_prod_import.employee_vw (
    id                          BIGINT                                           NOT NULL,
    number                      INTEGER      CHECK( number > 0 )                 NOT NULL,
    store_number                INTEGER                                          NOT NULL,
    dataset                     VARCHAR(6)   CHECK( char_length(dataset) = 6 )   NOT NULL,
    last_name                   VARCHAR(15)  CHECK( char_length(last_name) > 1 ) NOT NULL,
    first_name_mi               VARCHAR(15),
    pass_code                   VARCHAR(6)   CHECK( char_length(pass_code) > 0 ) NOT NULL,
    department                  VARCHAR(2),
    active                      BOOLEAN      DEFAULT TRUE                        NOT NULL,
    cynergi_system_admin        BOOLEAN      DEFAULT FALSE                       NOT NULL,
    alternative_store_indicator VARCHAR(1)   DEFAULT 'N'                         NOT NULL,
    alternative_area            INTEGER      DEFAULT 0                           NOT NULL,
    time_created  TIMESTAMPTZ                DEFAULT clock_timestamp()           NOT NULL,
    time_updated  TIMESTAMPTZ                DEFAULT clock_timestamp()           NOT NULL,
    UNIQUE(id, dataset)
);
COPY fastinfo_prod_import.employee_vw(
       id,
       dataset,
       time_created,
       time_updated,
       number,
       last_name,
       first_name_mi,
       pass_code,
       store_number,
       active,
       department,
       cynergi_system_admin,
       alternative_store_indicator,
       alternative_area
)
FROM '/tmp/fastinfo/test-employee.csv' DELIMITER ',' CSV HEADER;

DROP SERVER IF EXISTS fastinfo CASCADE;
CREATE SERVER fastinfo
   FOREIGN DATA WRAPPER file_fdw;

CREATE TABLE fastinfo_prod_import.inventory_vw (
   id               BIGSERIAL                             NOT NULL,
   dataset          VARCHAR(6)                            NOT NULL,
   serial_number    VARCHAR(10),
   lookup_key       VARCHAR(20),
   lookup_key_type  VARCHAR(10)                           NOT NULL,
   barcode          VARCHAR(10),
   alt_id           VARCHAR(30),
   brand            VARCHAR(30),
   model_number     VARCHAR(18)                           NOT NULL,
   product_code     TEXT                                  NOT NULL,
   description      VARCHAR(28),
   received_date    DATE,
   original_cost    NUMERIC(11,2)                         NOT NULL,
   actual_cost      NUMERIC(11,2)                         NOT NULL,
   model_category   VARCHAR(1)                            NOT NULL,
   times_rented     INTEGER                               NOT NULL,
   total_revenue    NUMERIC(11,2)                         NOT NULL,
   remaining_value  NUMERIC(11,2)                         NOT NULL,
   sell_price       NUMERIC(7,2)                          NOT NULL,
   assigned_value   NUMERIC(11,2)                         NOT NULL,
   idle_days        INTEGER                               NOT NULL,
   condition        VARCHAR(15),
   invoice_number VARCHAR,
   inv_invoice_expensed_date DATE,
   inv_purchase_order_number VARCHAR,
   returned_date    DATE,
   location         INTEGER                               NOT NULL,
   status           VARCHAR(1)                            NOT NULL,
   primary_location INTEGER                               NOT NULL,
   location_type    INTEGER                               NOT NULL
);

COPY fastinfo_prod_import.inventory_vw(
   id,
   dataset,
   serial_number,
   lookup_key,
   lookup_key_type,
   barcode,
   alt_id,
   brand,
   model_number,
   product_code,
   description,
   received_date,
   original_cost,
   actual_cost,
   model_category,
   times_rented,
   total_revenue,
   remaining_value,
   sell_price,
   assigned_value,
   idle_days,
   condition,
   invoice_number,
   inv_invoice_expensed_date,
   inv_purchase_order_number,
   returned_date,
   location,
   status,
   primary_location,
   location_type
)
FROM '/tmp/fastinfo/test-inventory.csv' DELIMITER ',' CSV HEADER;

CREATE TABLE fastinfo_prod_import.product_class_master_file_vw (
   dataset                                                   VARCHAR(6)                            NOT NULL,
   class_code                                                VARCHAR(10),
   class_description                                         VARCHAR(20),
   straight_line_book_depreciation_start_date                DATE,
   straight_line_book_depreciation_end_date                  DATE,
   income_forecasting_book_depreciation_start_date           DATE,
   income_forecasting_book_depreciation_end_date             DATE,
   macrs_book_start_date                                     DATE,
   macrs_book_end_date                                       DATE,
   macrs_tax_start_date                                      DATE,
   macrs_tax_end_date                                        DATE,
   rent_switch                                               VARCHAR(1)                            NOT NULL,
   transition_into_switch                                    VARCHAR(1)                            NOT NULL,
   transition_out_of_switch                                  VARCHAR(1)                            NOT NULL,
   cash_sale_switch                                          VARCHAR(1)                            NOT NULL,
   allow_depreciation_switch                                 VARCHAR(1)                            NOT NULL,
   allow_straight_line_life_switch                           VARCHAR(1)                            NOT NULL,
   time_created  TIMESTAMPTZ                                 DEFAULT clock_timestamp()             NOT NULL,
   time_updated  TIMESTAMPTZ                                 DEFAULT clock_timestamp()             NOT NULL
);

COPY fastinfo_prod_import.product_class_master_file_vw(
   dataset,
   class_code,
   class_description,
   straight_line_book_depreciation_start_date,
   straight_line_book_depreciation_end_date,
   income_forecasting_book_depreciation_start_date,
   income_forecasting_book_depreciation_end_date,
   macrs_book_start_date,
   macrs_book_end_date,
   macrs_tax_start_date,
   macrs_tax_end_date,
   rent_switch,
   transition_into_switch,
   transition_out_of_switch,
   cash_sale_switch,
   allow_depreciation_switch,
   allow_straight_line_life_switch
)
FROM '/tmp/fastinfo/test-product-class-master-file.csv' DELIMITER ',' CSV HEADER;

CREATE FOREIGN TABLE fastinfo_prod_import.location_vw (
   id BIGINT,
   dataset VARCHAR,
   number INTEGER,
   name VARCHAR
) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-location.csv', format 'csv', header 'TRUE');

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
) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-active-customer.csv', format 'csv', header 'TRUE');

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
) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-collection.csv', format 'csv', header 'TRUE');

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
) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-birthday-customer.csv', format 'csv', header 'TRUE');

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
) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-last-week-deliveries.csv', format 'csv', header 'TRUE');

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
) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-last-week-payouts.csv', format 'csv', header 'TRUE');

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
) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-future-payout.csv', format 'csv', header 'TRUE');

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
   amount_paid VARCHAR,
   customer_rating VARCHAR
) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-inactive-customer.csv', format 'csv', header 'TRUE');

CREATE FOREIGN TABLE fastinfo_prod_import.csv_active_inventory_vw (
    dataset VARCHAR,
    store_number INTEGER,
    sku VARCHAR,
    item_name VARCHAR,
    item_description VARCHAR,
    total_quantity INTEGER
 ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-active-inventory.csv', format 'csv', header 'TRUE');

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
  ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-single-agreement.csv', format 'csv', header 'TRUE');

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
    ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-final-payment.csv', format 'csv', header 'TRUE');


CREATE FOREIGN TABLE fastinfo_prod_import.csv_birthday_customer_v2_vw (
   dataset VARCHAR,
   store_number INTEGER,
   customer_number VARCHAR,
   first_name VARCHAR,
   last_name VARCHAR,
   email VARCHAR,
   birth_day DATE
) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-birthday-customer-v2.csv', format 'csv', header 'TRUE');

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
    ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-collection-v2.csv', format 'csv', header 'TRUE');

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
  ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-account-summary.csv', format 'csv', header 'TRUE');

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
         active_agreement CHAR,
         payment_terms VARCHAR,
         date_closed DATE,
         closed_reason INTEGER
    ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-all-rto-agreements.csv', format 'csv', header 'TRUE');

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
           active_agreement CHAR,
           payment_terms VARCHAR
 ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-new-rentals.csv', format 'csv', header 'TRUE');

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
             active_agreement CHAR,
             payment_terms VARCHAR,
             date_closed DATE,
             closed_reason INTEGER
   ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-returns.csv', format 'csv', header 'TRUE');


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
            active_agreement CHAR,
            payment_terms VARCHAR,
            date_closed DATE,
            closed_reason INTEGER
  ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-lost-customer.csv', format 'csv', header 'TRUE');

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
              active_agreement CHAR,
              payment_terms VARCHAR,
              date_closed DATE,
              closed_reason INTEGER
    ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-payouts.csv', format 'csv', header 'TRUE');

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
                  active_agreement CHAR,
                  payment_terms VARCHAR
        ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-at-risk.csv', format 'csv', header 'TRUE');

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
                 active_agreement CHAR,
                 payment_terms VARCHAR
      ) SERVER fastinfo OPTIONS(filename '/tmp/fastinfo/test-csv-payouts-next-30.csv', format 'csv', header 'TRUE');

CREATE TABLE fastinfo_prod_import.itemfile_vw(
   id                     BIGSERIAL                             NOT NULL,
   dataset                VARCHAR(6)                            NOT NULL,
   time_created           TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
   time_updated           TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
   number                 VARCHAR(18)                           NOT NULL,
   description_1          VARCHAR(28)                           NOT NULL,
   description_2          VARCHAR(26),
   vendor_number          INTEGER                               NOT NULL,
   discontinued_indicator VARCHAR(1)
);

COPY fastinfo_prod_import.itemfile_vw(
   id,
   dataset,
   time_created,
   time_updated,
   number,
   description_1,
   description_2,
   vendor_number,
   discontinued_indicator
) FROM '/tmp/fastinfo/test-itemfile.csv' DELIMITER ',' CSV HEADER;

CREATE TABLE fastinfo_prod_import.customer_vw(
   id                     BIGSERIAL                             NOT NULL,
   dataset                VARCHAR(6)                            NOT NULL,
   time_created           TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
   time_updated           TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
   number                 INTEGER                               NOT NULL,
   first_name_mi          VARCHAR(50)                           NOT NULL,
   last_name              VARCHAR(50)
);

COPY fastinfo_prod_import.customer_vw(
   id,
   dataset,
   time_created,
   time_updated,
   number,
   first_name_mi,
   last_name
) FROM '/tmp/fastinfo/test-customer.csv' DELIMITER ',' CSV HEADER;

CREATE TABLE fastinfo_prod_import.operator_vw(
    id                             BIGSERIAL                             NOT NULL,
    dataset                        VARCHAR(6)                            NOT NULL,
    time_created                   TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
    time_updated                   TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
    name                           VARCHAR(20)                           NOT NULL,
    number                         INTEGER                               NOT NULL,
    account_payable_security       INTEGER                               NOT NULL,
    purchase_order_security        INTEGER                               NOT NULL,
    general_ledger_security        INTEGER                               NOT NULL,
    system_administration_security INTEGER                               NOT NULL,
    file_maintenance_security      INTEGER                               NOT NULL,
    bank_reconciliation_security   INTEGER                               NOT NULL
);

/*
 select
   CASE
      WHEN dataset = 'corrto' THEN 'coravt'
      WHEN dataset = 'corptp' THEN 'corrto'
   END AS dataset,
   time_created,
   time_updated,
   name,
   number,
   account_payable_security,
   purchase_order_security,
   general_ledger_security,
   system_administration_security,
   file_maintenance_security,
   bank_reconciliation_security
 from operator_vw
*/

COPY fastinfo_prod_import.operator_vw(
    id,
    dataset,
    time_created,
    time_updated,
    name,
    number,
    account_payable_security,
    purchase_order_security,
    general_ledger_security,
    system_administration_security,
    file_maintenance_security,
    bank_reconciliation_security
) FROM '/tmp/fastinfo/test-operator.csv' DELIMITER ',' CSV HEADER;

CREATE TABLE fastinfo_prod_import.furncol_vw(
    id                             BIGSERIAL                             NOT NULL,
    dataset                        VARCHAR(6)                            NOT NULL,
    time_created                   TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
    time_updated                   TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
    number                         INTEGER                               NOT NULL,
    description                    VARCHAR                               NOT NULL
);

COPY fastinfo_prod_import.furncol_vw(
    dataset,
    number,
    description
) FROM '/tmp/fastinfo/test-furncol.csv' DELIMITER ',' CSV HEADER;

CREATE TABLE fastinfo_prod_import.furnfab_vw(
    id                             BIGSERIAL                             NOT NULL PRIMARY KEY,
    dataset                        VARCHAR(6)                            NOT NULL,
    time_created                   TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
    time_updated                   TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
    number                         INTEGER                               NOT NULL,
    description                    VARCHAR                               NOT NULL
);

COPY fastinfo_prod_import.furnfab_vw(
    id,
    dataset,
    time_created,
    time_updated,
    number,
    description
) FROM '/tmp/fastinfo/test-furnfab.csv' DELIMITER ',' CSV HEADER;

