CREATE TABLE account_payable_recurring_invoice_status_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO account_payable_recurring_invoice_status_type_domain(id, value, description, localization_code)
VALUES (1, 'A', 'Active', 'active'),
       (2, 'I', 'Inactive', 'inactive');

CREATE TABLE expense_month_creation_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO expense_month_creation_type_domain(id, value, description, localization_code)
VALUES (1, 'C', 'Current Month', 'current.month'),
       (2, 'N', 'Next Month', 'next.month');

CREATE TABLE account_payable_recurring_invoice
(
    id                            BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                     UUID                        DEFAULT uuid_generate_v1()                      NOT NULL,
    time_created                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    time_updated                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    company_id                    BIGINT REFERENCES company (id)                                              NOT NULL,
    vendor_id                     BIGINT REFERENCES vendor (id)                                               NOT NULL,
    invoice                       VARCHAR(20)                                                                 NOT NULL,
    invoice_amount                NUMERIC(11, 2)                                                              NOT NULL,
    fixed_amount_indicator        BOOLEAN DEFAULT TRUE                                                        NOT NULL,
    employee_number_id_sfk        INTEGER                                                                     NOT NULL,
    message                       TEXT,
    code_indicator                VARCHAR(3),    -- This was decided to be a free form field.
    type                          VARCHAR (1)  CHECK (type = 'E')                                             NOT NULL, -- this field can only contain an E for expense included for future possible use
    pay_to_id                     BIGINT REFERENCES vendor (id)                                               NOT NULL,
    last_transfer_to_create_invoice_date DATE,
    status_id                     BIGINT REFERENCES account_payable_recurring_invoice_status_type_domain (id)  NOT NULL,
    due_days                      INTEGER                                                                      NOT NULL, -- will be used to calculate due date from invoice_date + days due_date
    automated_indicator           BOOLEAN DEFAULT FALSE                                                        NOT NULL,
    separate_check_indicator      BOOLEAN                                                                      NOT NULL,     -- The default for this is based upon the setting in the vendor record.
    expense_month_creation_indicator_id BIGINT REFERENCES expense_month_creation_type_domain (id)              NOT NULL,
    invoice_day                   INTEGER                                                                      NOT NULL,
    expense_day                   INTEGER                                                                      NOT NULL,
    schedule_id                   BIGINT REFERENCES schedule (id),
    last_created_in_period        DATE, -- Display only the day and month associated with the date the schedule ran which populated the expense_date
    next_creation_date            DATE, -- This will be the next creation date based on the schedule date and the expense_month_creation_indicator_id display only
    next_invoice_date             DATE, -- Display Only this will be calculated by the schedule date and the value in
      -- expense_month_creation_indicator_id and the invoice_days.
    next_expense_date             DATE -- Display Only This will be the last expense date calculated by the taking the schedule run date and then looking at the
    -- expense_month_creation_indicator_id and adding a month if next month and then selecting the specific day from the value in expense_day field
 );

CREATE TRIGGER update_account_payable_recurring_invoice_trg
    BEFORE UPDATE
    ON account_payable_recurring_invoice
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX account_payable_recurring_invoice_company_id_idx ON account_payable_recurring_invoice (company_id);
CREATE INDEX account_payable_recurring_invoice_vendor_id_idx ON account_payable_recurring_invoice (vendor_id);
CREATE INDEX account_payable_recurring_invoice_pay_to_id_idx ON account_payable_recurring_invoice (pay_to_id);
CREATE INDEX account_payable_recurring_invoice_status_id_idx ON account_payable_recurring_invoice (status_id);
CREATE INDEX account_payable_recurring_invoice_expense_month_id_idx ON account_payable_recurring_invoice (expense_month_creation_indicator_id);

CREATE TABLE account_payable_recurring_invoice_distribution
(
    id                            BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                     UUID                        DEFAULT uuid_generate_v1()                      NOT NULL,
    time_created                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    time_updated                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    recurring_invoice_id          BIGINT REFERENCES account_payable_recurring_invoice (id)                    NOT NULL,
    distribution_account_id       BIGINT REFERENCES account (id)                                              NOT NULL,
    distribution_profit_center_id_sfk INTEGER                                                                 NOT NULL,
    distribution_amount            NUMERIC(11,2)                                                              NOT NULL
);
CREATE TRIGGER update_account_payable_recurring_invoice_distribution_trg
    BEFORE UPDATE
    ON account_payable_recurring_invoice_distribution
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE INDEX ap_recurring_invoice_distribution_recurring_invoice_id_idx ON account_payable_recurring_invoice_distribution (recurring_invoice_id);
CREATE INDEX ap_recurring_invoice_distribution_recurring_account_id_idx ON account_payable_recurring_invoice_distribution (distribution_account_id);

COMMENT ON COLUMN account_payable_recurring_invoice.employee_number_id_sfk IS 'Employee ID number';
COMMENT ON COLUMN account_payable_recurring_invoice.last_transfer_to_create_invoice_date IS 'This is the date that the
      recurring invoice was used/transferred to create a new account payable invoice.';
COMMENT ON COLUMN account_payable_recurring_invoice.separate_check_indicator IS 'This is the default that will be the value for the account payable invoice
separate check indicator field. The default is derived from the setting in the vendor record.';
COMMENT ON COLUMN account_payable_recurring_invoice.type IS 'For a recurring invoice this will always be an E, a non-inventory expense, included for migrating the data and future use if needed.';
COMMENT ON COLUMN account_payable_recurring_invoice.due_days IS 'This holds the days that will be used to calculate the due date. Due Date = invoice date + due_days.';
COMMENT ON COLUMN account_payable_recurring_invoice.code_indicator IS 'This was decided to be a free form field to allow user definition.
Allows the user to organize the account payable recurring entries. i.e. organize by rent, loans, interest, mileage, etc.';


COMMENT ON TABLE  account_payable_recurring_invoice IS 'Table holds the defaults/values to allow manual or automated creation of account payable invoices template.';
COMMENT ON TABLE  account_payable_recurring_invoice_distribution IS 'Table holds the account payable recurring distributions associated with account_payable_recurring_invoice table.
These values will be used to update the account_payable_invoice_distribution table associated with the accoount payable invoice that will be created manually or automatically.';
