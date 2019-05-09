CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA fastinfo_prod_import;
CREATE TABLE fastinfo_prod_import.employee (
   id                BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id         UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   number            VARCHAR(6)                             NOT NULL,
   pass_code         VARCHAR(6)                             NOT NULL,
   active            BOOLEAN DEFAULT TRUE                   NOT NULL
);

