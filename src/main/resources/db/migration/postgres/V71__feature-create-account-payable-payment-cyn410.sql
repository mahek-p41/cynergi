CREATE TABLE account_payable_payment_status_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO account_payable_payment_status_type_domain(id, value, description, localization_code)
VALUES (1, 'P', 'Paid', 'paid'),
       (2, 'V', 'Void', 'void');

CREATE TABLE account_payable_payment_type_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO account_payable_payment_type_type_domain(id, value, description, localization_code)
VALUES (1, 'A', 'ACH', 'ach'),
       (2, 'C', 'Check', 'check');


CREATE TABLE account_payable_payment
(
    id                                UUID        DEFAULT uuid_generate_v1()                             NOT NULL PRIMARY KEY,
    time_created                      TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                              NOT NULL,
    time_updated                      TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                              NOT NULL,
    company_id                        UUID REFERENCES company (id)                                       NOT NULL,
    bank_id                           UUID REFERENCES bank (id)                                          NOT NULL,
    vendor_id                         UUID REFERENCES vendor (id)                                        NOT NULL,
    account_payable_payment_status_id INTEGER REFERENCES account_payable_payment_status_type_domain (id) NOT NULL,
    account_payable_payment_type_id   INTEGER REFERENCES account_payable_payment_type_type_domain (id)   NOT NULL,
    payment_number                    VARCHAR(20)                                                        NOT NULL, -- was apc-check
    payment_date                      DATE                                                               NOT NULL,
    date_cleared                      DATE,
    date_voided                       DATE,
    amount                            NUMERIC(13, 2)                                                     NOT NULL,
    payment_interfaced_indicator      BOOLEAN   DEFAULT FALSE                                            NOT NULL,
    void_interfaced_indicator         BOOLEAN   DEFAULT FALSE                                            NOT NULL,
    UNIQUE (company_id, bank_id, account_payable_payment_type_id, payment_number)
);
CREATE TRIGGER account_payable_payment_trg
    BEFORE UPDATE
    ON account_payable_payment
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX account_payable_payment_company_id_idx
    ON account_payable_payment (company_id);
CREATE INDEX account_payable_payment_bank_id_idx
    ON account_payable_payment (bank_id);
CREATE INDEX account_payable_payment_vendor_id_idx
    ON account_payable_payment (vendor_id);
CREATE INDEX account_payable_payment_payment_interfaced_indicator_idx
   ON account_payable_payment (payment_interfaced_indicator)


COMMENT ON COLUMN account_payable_payment.company_id IS 'Foreign key which joins to the company table';
COMMENT ON COLUMN account_payable_payment.bank_id IS 'Foreign key which joins to the bank table';
COMMENT ON COLUMN account_payable_payment.vendor_id IS 'Foreign key which joins to the vendor table, represents the pay to.';

COMMENT ON TABLE  account_payable_payment IS 'Table holds the payment header associated with account payable.';


CREATE TABLE account_payable_payment_detail
(
    id                         UUID        DEFAULT uuid_generate_v1()       NOT NULL PRIMARY KEY,
    time_created               TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()        NOT NULL,
    time_updated               TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()        NOT NULL,
    company_id                 UUID REFERENCES company (id)                 NOT NULL,
    account_payable_invoice_id UUID REFERENCES account_payable_invoice (id) NOT NULL,
    payment_number_id          UUID REFERENCES account_payable_payment (id) NOT NULL,
    vendor_id                  UUID REFERENCES vendor (id),
    amount                     NUMERIC(13, 2)                               NOT NULL,
    discount                   NUMERIC(13, 2)
);
CREATE TRIGGER account_payable_payment_detail_trg
    BEFORE UPDATE
    ON account_payable_payment_detail
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX account_payable_payment_detail_company_id_idx
    ON account_payable_payment_detail (company_id);
CREATE INDEX account_payable_payment_detail_account_payable_invoice_id_idx
    ON account_payable_payment_detail (account_payable_invoice_id);
CREATE INDEX account_payable_payment_detail_payment_number_id_idx
    ON account_payable_payment_detail (payment_number_id);
CREATE INDEX account_payable_payment_detail_vendor_id_idx
    ON account_payable_payment_detail (vendor_id);


COMMENT ON COLUMN account_payable_payment_detail.company_id IS 'Foreign key which joins to the company table';
COMMENT ON COLUMN account_payable_payment_detail.account_payable_invoice_id IS 'Foreign key which joins to the account_payable_invoice table';

COMMENT ON TABLE  account_payable_payment_detail IS 'Table holds the payment details associated with account payable payment table. ';


