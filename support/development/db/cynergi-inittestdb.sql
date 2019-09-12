CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA fastinfo_prod_import;

CREATE TABLE fastinfo_prod_import.department_vw (
    id               BIGSERIAL                              NOT NULL PRIMARY KEY,
    code             VARCHAR(2)                             NOT NULL,
    description      VARCHAR(12)                            NOT NULL,
    security_profile INTEGER                                NOT NULL,
    default_menu     VARCHAR(8)                             NOT NULL,
    time_created     TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    time_updated     TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL
);
INSERT INTO fastinfo_prod_import.department_vw (code, description, security_profile, default_menu) VALUES ('OF', 'OFFICE', 90001, 'RSSSMENU');
INSERT INTO fastinfo_prod_import.department_vw (code, description, security_profile, default_menu) VALUES ('MG', 'MANAGEMENT', 90000, 'RSSSMENU');
INSERT INTO fastinfo_prod_import.department_vw (code, description, security_profile, default_menu) VALUES ('DM', 'DISTRICT MGR', 90002, 'MENUR2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, security_profile, default_menu) VALUES ('SM', 'STORE MANAGE', 90003, 'MENUR2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, security_profile, default_menu) VALUES ('AM', 'ACCOUNT MGR', 90004, 'MENUR2');
INSERT INTO fastinfo_prod_import.department_vw (code, description, security_profile, default_menu) VALUES (' S', 'ASST MGR', 90006, 'MENUR2');

CREATE TABLE fastinfo_prod_import.employee_vw ( -- create stand-in table that will be created by the cynergi-data-migration project
    id            BIGSERIAL                                           NOT NULL PRIMARY KEY,
    number        INTEGER     CHECK( number > 0 )                     NOT NULL,
    store_number  INTEGER                                             NOT NULL,
    last_name     VARCHAR(15) CHECK( char_length(last_name) > 1)      NOT NULL,
    first_name_mi VARCHAR(15) CHECK( char_length(first_name_mi) > 1)  NOT NULL,
    pass_code     VARCHAR(6)  CHECK( char_length(pass_code) > 0 )     NOT NULL,
    department    VARCHAR(2),
    active        BOOLEAN     DEFAULT TRUE                            NOT NULL,
    time_created  TIMESTAMPTZ DEFAULT clock_timestamp()               NOT NULL,
    time_updated  TIMESTAMPTZ DEFAULT clock_timestamp()               NOT NULL
);
INSERT INTO fastinfo_prod_import.employee_vw (number, last_name, first_name_mi, pass_code, store_number) VALUES (123, 'user', 'test', 'pass', 1); -- create a user that can be used for testing and is also ignored by the truncate service

CREATE TABLE fastinfo_prod_import.store_vw (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   number       INTEGER,
   name         VARCHAR(27),
   dataset      VARCHAR(6)                             NOT NULL,
   time_created TIMESTAMPTZ  DEFAULT clock_timestamp() NOT NULL,
   time_updated TIMESTAMPTZ  DEFAULT clock_timestamp() NOT NULL
);
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (1, 'KANSAS CITY', 'testds');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (3, 'INDEPENDENCE', 'testds');
INSERT INTO fastinfo_prod_import.store_vw (number, name, dataset) VALUES (9000, 'HOME OFFICE', 'testds');

CREATE TABLE fastinfo_prod_import.inventory_vw (
   id               BIGINT                                NOT NULL PRIMARY KEY,
   serial_number    VARCHAR(10)                           NOT NULL,
   lookup_key       VARCHAR(10)                           NOT NULL,
   lookup_key_type  VARCHAR(10)                           NOT NULL,
   barcode          VARCHAR(10)                           NOT NULL,
   alt_id           VARCHAR(30)                           NOT NULL,
   brand            VARCHAR(30),
   model_number     VARCHAR(18)                           NOT NULL,
   product_code     TEXT                                  NOT NULL,
   description      VARCHAR(28)                           NOT NULL,
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
   returned_date    DATE,
   location         INTEGER                               NOT NULL,
   status           VARCHAR(1)                            NOT NULL,
   primary_location INTEGER                               NOT NULL,
   location_type    INTEGER                               NOT NULL,
   time_created     TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
   time_updated     TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL
);

COPY fastinfo_prod_import.inventory_vw(
   id,
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
   returned_date,
   location,
   status,
   primary_location,
   location_type,
   time_created,
   time_updated
)
FROM '/tmp/test-inventory.csv' DELIMITER ',' CSV HEADER;
