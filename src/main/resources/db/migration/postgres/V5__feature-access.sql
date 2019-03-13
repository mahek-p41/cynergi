CREATE TABLE menu (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY, -- TODO consider not making this generated off of a sequence but rather hard coded in INSERT statements in this file
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   name         VARCHAR(10) UNIQUE                     NOT NULL,
   literal      TEXT                                   NOT NULL
);
CREATE TRIGGER update_menu_trg
   BEFORE UPDATE
   ON menu
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE module (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY, -- TODO consider not making this generated off of a sequence but rather hard coded in INSERT statements in this file
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   name         VARCHAR(10) UNIQUE                     NOT NULL,
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
   name            TEXT                                   NOT NULL,
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
   level        NUMERIC(2)                             NOT NULL,
   company_id   BIGINT REFERENCES company(id)          NOT NULL
);
CREATE TRIGGER update_department_trg
   BEFORE UPDATE
   ON department
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE employee ( -- TODO convert to a view in the cynergi-data-migration project
   id            BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id     UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created  TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated  TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   user_id       VARCHAR(8)                             NOT NULL,
   password      VARCHAR(8)                             NOT NULL,
   first_name    TEXT                                   NOT NULL,
   last_name     TEXT                                   NOT NULL,
   department_id BIGINT REFERENCES department(id)       NOT NULL
);
CREATE TRIGGER update_employee_trg
   BEFORE UPDATE
   ON employee
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX employee_username_password_idx ON employee(user_id, password);
CREATE INDEX employee_department_idx ON employee(department_id);

CREATE TABLE company_module_access (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   level        NUMERIC(2)                             NOT NULL,
   company_id   BIGINT REFERENCES company(id)          NOT NULL,
   module_id    BIGINT REFERENCES module(id)           NOT NULL
);
CREATE TRIGGER update_company_module_access_trg
   BEFORE UPDATE
   ON company_module_access
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

INSERT INTO menu(name, literal) VALUES ('AP', 'Accounts Payable');
INSERT INTO module(name, literal, menu_id) VALUES ('APADD', 'Add Vendor Invoices', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APCHG', 'Change Vendor Invoices', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APDEL', 'Delete Vendor Invoices', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APSTATUS', 'Display Vendor Status', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APUTIL', 'File Maintenance', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APRECUR', 'Frequent/Recurring Entries', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APGLRPT', 'General Ledger Interface', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APCHKLST', 'List Checks', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APLST', 'List Vendor Invoices', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APCHECK', 'Print Checks', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APREPORT', 'Reports Menu', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APSEL', 'Select Invoices By Vendor', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APSELDUE', 'Select Invoices By Due Date', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APSHO', 'Show Vendor Invoices', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('APVOID', 'Void Checks', (SELECT id FROM menu WHERE name = 'AP'));
INSERT INTO module(name, literal, menu_id) VALUES ('PRTAPDST', 'Print Distribution Template', (SELECT id FROM menu WHERE name = 'AP'));

INSERT INTO menu(name, literal) VALUES ('APRECUR', 'Accounts Payable - Add a Recurring Entry');
INSERT INTO module(name, literal, menu_id) VALUES ('ADDAPREC', 'Add a Recurring Entry', (SELECT id FROM menu WHERE name = 'APRECUR'));
INSERT INTO module(name, literal, menu_id) VALUES ('CHGAPREC', 'Change a Recurring Entry', (SELECT id FROM menu WHERE name = 'APRECUR'));
INSERT INTO module(name, literal, menu_id) VALUES ('DELAPREC', 'Delete a Recurring Entry', (SELECT id FROM menu WHERE name = 'APRECUR'));
INSERT INTO module(name, literal, menu_id) VALUES ('LSTAPREC', 'List Recurring Entries', (SELECT id FROM menu WHERE name = 'APRECUR'));
INSERT INTO module(name, literal, menu_id) VALUES ('PRTAPREC', 'Print Recurring Entry Report', (SELECT id FROM menu WHERE name = 'APRECUR'));
INSERT INTO module(name, literal, menu_id) VALUES ('SHOAPREC', 'Show a Recurring Entry', (SELECT id FROM menu WHERE name = 'APRECUR'));
INSERT INTO module(name, literal, menu_id) VALUES ('TRNAPREC', 'Transfer Recurring Entries', (SELECT id FROM menu WHERE name = 'APRECUR'));

INSERT INTO menu(name, literal) VALUES ('APREPORT', 'Accounts Payable - Aging Report');
INSERT INTO module(name, literal, menu_id) VALUES ('APAGERPT', 'Aging Report', (SELECT id FROM menu WHERE name = 'APREPORT'));
INSERT INTO module(name, literal, menu_id) VALUES ('FLOWANAL', 'Cash Flow Report', (SELECT id FROM menu WHERE name = 'APREPORT'));
INSERT INTO module(name, literal, menu_id) VALUES ('APCHKRPT', 'Check Report', (SELECT id FROM menu WHERE name = 'APREPORT'));
INSERT INTO module(name, literal, menu_id) VALUES ('APEXPENS', 'Expense Report', (SELECT id FROM menu WHERE name = 'APREPORT'));
INSERT INTO module(name, literal, menu_id) VALUES ('APRPT', 'Invoice Report', (SELECT id FROM menu WHERE name = 'APREPORT'));
INSERT INTO module(name, literal, menu_id) VALUES ('APPREVUE', 'Preview Report', (SELECT id FROM menu WHERE name = 'APREPORT'));
INSERT INTO module(name, literal, menu_id) VALUES ('CASHOUT', 'Requirements Report', (SELECT id FROM menu WHERE name = 'APREPORT'));
INSERT INTO module(name, literal, menu_id) VALUES ('APTRLBAL', 'Trial Balance Report', (SELECT id FROM menu WHERE name = 'APREPORT'));
INSERT INTO module(name, literal, menu_id) VALUES ('AP1099', 'Vendor 1099''s', (SELECT id FROM menu WHERE name = 'APREPORT'));
INSERT INTO module(name, literal, menu_id) VALUES ('APUNDO', 'Account Maintenance', (SELECT id FROM menu WHERE name = 'APREPORT'));
