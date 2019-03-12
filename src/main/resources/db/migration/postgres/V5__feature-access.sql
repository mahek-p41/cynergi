CREATE TABLE menu (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   name         VARCHAR(6)                             NOT NULL,
   literal      TEXT                                   NOT NULL
);
CREATE TRIGGER update_menu_trg
   BEFORE UPDATE
   ON menu
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE module (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   name         VARCHAR(6)                             NOT NULL,
   literal      TEXT                                   NOT NULL,
   menu_id      BIGINT REFERENCES menu(id)             NOT NULL
);
CREATE TRIGGER update_module_trg
   BEFORE UPDATE
   ON module
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX module_menu_idx ON module(menu_id);

CREATE TABLE organization (
   id              BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id       UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   name            TEXT                                   NOT NULL,
   billing_account TEXT                                   NOT NULL
);
CREATE TRIGGER update_organization_trg
   BEFORE UPDATE
   ON organization
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE company (
   id              BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id       UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   organization_id BIGINT REFERENCES organization(id)     NOT NULL
);
CREATE TRIGGER update_company_trg
   BEFORE UPDATE
   ON company
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX company_organization_idx ON company(organization_id);

CREATE TABLE store (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   name         TEXT                                   NOT NULL,
   company_id   BIGINT REFERENCES company(id)          NOT NULL
);
CREATE TRIGGER update_store_trg
   BEFORE UPDATE
   ON store
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX store_company_idx ON store(company_id);

CREATE TABLE area (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   company_id   BIGINT REFERENCES company(id)          NOT NULL,
   menu_id      BIGINT REFERENCES menu(id)             NOT NULL,
   level        NUMERIC(2)                             NOT NULL
);
CREATE TRIGGER update_area_trg
   BEFORE UPDATE
   ON area
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX area_company_idx ON area(company_id);
CREATE INDEX area_menu_idx ON area(menu_id);

CREATE TABLE department (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   name         TEXT                                   NOT NULL,
   level        NUMERIC(2)                             NOT NULL
);
CREATE TRIGGER update_department_trg
   BEFORE UPDATE
   ON department
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE employee (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   user_id      VARCHAR(8)                             NOT NULL,
   password     VARCHAR(8)                             NOT NULL,
   first_name   TEXT                                   NOT NULL,
   last_name    TEXT                                   NOT NULL,
   level        NUMERIC(2)                             NOT NULL,
   company_id   BIGINT REFERENCES company(id)          NOT NULL
);
CREATE TRIGGER update_employee_trg
   BEFORE UPDATE
   ON employee
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX employee_username_idx ON employee(user_id);

CREATE TABLE department_access (
   id            BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id     UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created  TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated  TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   department_id BIGINT REFERENCES department(id)       NOT NULL,
   area_id       BIGINT REFERENCES area(id)             NOT NULL
);
CREATE TRIGGER update_department_access_trg
   BEFORE UPDATE
   ON department_access
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX department_access_department ON department_access(department_id);
CREATE INDEX department_access_area ON department_access(area_id);

CREATE TABLE company_module_access (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   company_id   BIGINT REFERENCES company(id)          NOT NULL,
   module_id    BIGINT REFERENCES module(id)           NOT NULL,
   level        NUMERIC(2)                             NOT NULL
);
CREATE TRIGGER update_company_module_access_trg
   BEFORE UPDATE
   ON company_module_access
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
