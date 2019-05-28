CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA fastinfo_prod_import;
CREATE TABLE fastinfo_prod_import.employee_vw ( -- create stand-in table that will be created by the cynergi-data-migration project
   id           BIGSERIAL                                       NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1()          NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()           NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()           NOT NULL,
   number       INTEGER CHECK ( number > 0 )                    NOT NULL,
   pass_code    VARCHAR(6) CHECK ( char_length(pass_code) > 0 ) NOT NULL,
   active       BOOLEAN     DEFAULT TRUE                        NOT NULL
);
INSERT INTO fastinfo_prod_import.employee_vw (number, pass_code) VALUES (123, 'pass'); -- create a user that can be used for testing and is also ignored by the truncate service

