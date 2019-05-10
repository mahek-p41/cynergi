CREATE TABLE legacy_importation (
   id           BIGSERIAL                             NOT NULL PRIMARY KEY,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
   filename     VARCHAR(1000) UNIQUE                  NOT NULL,
   hash         VARCHAR(64) UNIQUE                    NOT NULL
);

CREATE TABLE employee (
   id                BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id         UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   number            INTEGER                                NOT NULL,
   pass_code         VARCHAR(6)                             NOT NULL,
   active            BOOLEAN DEFAULT TRUE                   NOT NULL,
   UNIQUE (number, pass_code)
);
CREATE TRIGGER update_employee_trg
   BEFORE UPDATE
   ON employee
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
