CREATE TABLE account_payable_invoice_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO account_payable_invoice_type_domain(id, value, description, localization_code)
VALUES (1, 'E', 'Non Inventory', 'non.inventory'),
       (2, 'P', 'Inventory', 'inventory');

CREATE TABLE account_payable_invoice_status_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO account_payable_invoice_status_type_domain(id, value, description, localization_code)
VALUES (1, 'H', 'Hold', 'hold'),
       (2, 'O', 'Open', 'open'),
       (3, 'P', 'Paid', 'paid'),
       (4, 'D', 'Deleted','deleted');

CREATE TABLE account_payable_invoice_selected_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO account_payable_invoice_selected_type_domain(id, value, description, localization_code)
VALUES (1, 'Y', 'Yes', 'yes'),
       (2, 'N', 'No', 'no'),
       (3, 'H', 'On Hold', 'on.hold');

CREATE TABLE account_payable_invoice
(
    id                            BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                     UUID                        DEFAULT uuid_generate_v1()                      NOT NULL,
    time_created                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    time_updated                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    company_id                    BIGINT REFERENCES company (id)                                              NOT NULL,
    vendor_id                     BIGINT REFERENCES vendor (id)                                               NOT NULL,
    invoice                       VARCHAR(20)                                                                 NOT NULL,
    purchase_order_id             BIGINT REFERENCES purchase_order_header (id)                                NOT NULL,
    invoice_date                  DATE                        DEFAULT current_date                            NOT NULL,
    invoice_amount                NUMERIC(11, 2)                                                              NOT NULL,
    discount_amount               NUMERIC(11, 2),
    discount_percent              NUMERIC(7,6),
    auto_distribution_applied     BOOLEAN DEFAULT FALSE                                                       NOT NULL,
    discount_taken                NUMERIC(12,2),
    entry_date                    DATE                        DEFAULT current_date                            NOT NULL,
    expense_date                  DATE                        DEFAULT current_date                            NOT NULL,
    discount_date                 DATE,
    employee_number_sfk           INTEGER                                                                     NOT NULL,
    message                       TEXT,
    selected_id                   BIGINT REFERENCES account_payable_invoice_selected_type_domain (id)         NOT NULL,
    multiple_payment_indicator    BOOLEAN   DEFAULT FALSE                                                     NOT NULL,
    paid_amount                   NUMERIC (12,2)                                                              NOT NULL,
    selected_amount               NUMERIC(12,2),
    type_id                       BIGINT REFERENCES account_payable_invoice_type_domain (id)                  NOT NULL,
    status_id                     BIGINT REFERENCES account_payable_invoice_status_type_domain (id)           NOT NULL,
    pay_to_id                     BIGINT REFERENCES vendor (id)                                               NOT NULL,
    separate_check_indicator      BOOLEAN                                                                     NOT NULL,  -- The default for this is based upon the setting in the vendor record.
    use_tax_indicator             BOOLEAN DEFAULT FALSE                                                       NOT NULL,
    receive_date                  DATE,
    location_id_sfk               INTEGER                                                                     NOT NULL
);
CREATE TRIGGER update_account_payable_invoice_trg
    BEFORE UPDATE
    ON account_payable_invoice
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX account_payable_invoice_company_id_idx ON account_payable_invoice (company_id);
CREATE INDEX account_payable_invoice_vendor_id_idx ON account_payable_invoice (vendor_id);
CREATE INDEX account_payable_invoice_purchase_order_id_idx ON account_payable_invoice (purchase_order_id);
CREATE INDEX account_payable_invoice_selected_id_idx ON account_payable_invoice (selected_id);
CREATE INDEX account_payable_invoice_tupe_id_idx ON account_payable_invoice (type_id);
CREATE INDEX account_payable_invoice_status_id_idx ON account_payable_invoice (status_id);
CREATE INDEX account_payable_invoice_pay_to_id_idx ON account_payable_invoice (pay_to_id);


CREATE TABLE account_payable_invoice_distribution
(
    id                            BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                     UUID                        DEFAULT uuid_generate_v1()                      NOT NULL,
    time_created                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    time_updated                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    invoice_id                    BIGINT REFERENCES account_payable_invoice(id)                               NOT NULL,
    distribution_account          BIGINT REFERENCES account (id)                                              NOT NULL,
    distribution_profit_center_sfk INTEGER                                                                    NOT NULL,
    distribution_amount            NUMERIC(11,2)                                                              NOT NULL
);
CREATE TRIGGER update_account_payable_invoice_distribution_trg
    BEFORE UPDATE
    ON account_payable_invoice_distribution
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX account_payable_invoice_distribution_idx ON account_payable_invoice_distribution (invoice_id);

