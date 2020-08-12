CREATE TABLE print_currency_indicator_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO print_currency_indicator_type_domain(id, value, description, localization_code)
VALUES (1, 'B', 'Bank', 'bank'),
       (2, 'N', 'No', 'no'),
       (3, 'V', 'Vendor', 'vendor');

CREATE TABLE account_payable_check_form_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO account_payable_check_form_type_domain(id, value, description, localization_code)
VALUES (1, 'L2', 'Laser 2', 'laser.2'),
       (2, 'L3', 'Laser 3', 'laser.3'),
       (3, 'T', 'Tractor', 'tractor');


CREATE TABLE purchase_order_number_required_indicator_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO purchase_order_number_required_indicator_type_domain(id, value, description, localization_code)
VALUES (1, 'M', 'Sometimes Validate', 'sometimes.validate'),
       (2, 'N', 'Never Validate', 'never.validate'),
       (3, 'V', 'Validate', 'validate');

CREATE TABLE account_payable_control
(
    id                                               BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                                        UUID        DEFAULT uuid_generate_v1()                                      NOT NULL,
    time_created                                     TIMESTAMPTZ DEFAULT clock_timestamp()                                       NOT NULL,
    time_updated                                     TIMESTAMPTZ DEFAULT clock_timestamp()                                       NOT NULL,
    company_id                                       BIGINT REFERENCES company (id)                                              NOT NULL,
    account_payable_check_form_type_id               BIGINT REFERENCES account_payable_check_form_type_domain (id)               NOT NULL,
    pay_after_discount_date                          BOOLEAN     DEFAULT FALSE                                                   NOT NULL,
    reset_expense                                    BOOLEAN     DEFAULT FALSE                                                   NOT NULL,
    use_rebates_indicator                            BOOLEAN     DEFAULT FALSE                                                   NOT NULL,
    trade_company_indicator                          BOOLEAN     DEFAULT FALSE                                                   NOT NULL,
    print_currency_indicator_type_id                 BIGINT REFERENCES print_currency_indicator_type_domain (id)                 NOT NULL,
    lock_inventory_indicator                         BOOLEAN     DEFAULT FALSE                                                   NOT NULL,
    purchase_order_number_required_indicator_type_id BIGINT REFERENCES purchase_order_number_required_indicator_type_domain (id) NOT NULL,
    general_ledger_inventory_clearing_account_id     BIGINT REFERENCES account(id)                                               NOT NULL,
    general_ledger_inventory_account_id              BIGINT REFERENCES account(id)                                               NOT NULL,
    UNIQUE (company_id)
);
CREATE TRIGGER account_payable_control_trg
    BEFORE UPDATE
    ON account_payable_control
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX account_payable_ctl_company_id_idx
    ON account_payable_control (company_id);
CREATE INDEX account_payable_ctl_print_currency_indicator_type_id_idx
    ON account_payable_control (print_currency_indicator_type_id);
CREATE INDEX account_payable_ctl_po_nbr_required_indicator_type_id_idx
    ON account_payable_control (purchase_order_number_required_indicator_type_id);
CREATE INDEX account_payable_ctl_clearing_account_idx
    ON account_payable_control (general_ledger_inventory_clearing_account_id);
CREATE INDEX account_payable_ctl_inventory_account_idx
    ON account_payable_control (general_ledger_inventory_account_id);


