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
    id                                              BIGSERIAL                              NOT NULL PRIMARY KEY,
    uu_row_id                                       UUID        DEFAULT uuid_generate_v1() NOT NULL,
    time_created                                    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    time_updated                                    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    company_id                                      BIGINT REFERENCES company (id)         NOT NULL,
    bank_id                                         BIGINT REFERENCES bank (id)            NOT NULL,
    vendor_id                                       BIGINT REFERENCES vendor (id)          NOT NULL,
    account_payable_payment_status_id               BIGINT REFERENCES account_payable_payment_status_type_domain(id) NOT NULL,
    account_payable_payment_type_id                 BIGINT REFERENCES account_payable_payment_type_type_domain(id) NOT NULL,
    payment_number                                  VARCHAR(20)                            NOT NULL, -- was apc-check
    payment_date                                    DATE                                   NOT NULL,
    date_cleared                                    DATE,
    date_voided                                     DATE,
    amount                                          NUMERIC(13,2)                          NOT NULL,
    UNIQUE (company_id,bank_id, account_payable_payment_type_id, payment_number)
 );
CREATE TRIGGER account_payable_payment_trg
    BEFORE UPDATE
    ON account_payable_payment
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX account_payable_payment_company_id_idx
    ON account_payable_payment (company_id);
CREATE INDEX account_payable_payment_bank_id_idx
    ON account_payable_payment (bank_id);
CREATE INDEX account_payable_payment_vendor_id_idx
    ON account_payable_payment (vendor_id);


COMMENT ON COLUMN account_payable_payment.company_id IS 'Foreign key which joins to the company table';
COMMENT ON COLUMN account_payable_payment.bank_id IS 'Foreign key which joins to the bank table';
COMMENT ON COLUMN account_payable_payment.vendor_id IS 'Foreign key which joins to the vendor table, represents the pay to.';

COMMENT ON TABLE  account_payable_payment IS 'Table holds the payment header associated with account payable.';


CREATE TABLE account_payable_payment_detail
(
    id                                              BIGSERIAL                              NOT NULL PRIMARY KEY,
    uu_row_id                                       UUID        DEFAULT uuid_generate_v1() NOT NULL,
    time_created                                    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    time_updated                                    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    company_id                                      BIGINT REFERENCES company (id)         NOT NULL,
    account_payable_invoice_id                      BIGINT REFERENCES account_payable_invoice (id)  NOT NULL,
    payment_number_id                               BIGINT REFERENCES account_payable_payment(id)NOT NULL,
    vendor_id                                       BIGINT REFERENCES vendor(id),
    sequence                                        INTEGER                                NOT NULL,
    amount                                          NUMERIC(13,2)                          NOT NULL,
    discount                                        NUMERIC(13,2)
 );
CREATE TRIGGER account_payable_payment_detail_trg
    BEFORE UPDATE
    ON account_payable_payment_detail
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

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

--- Begin account_payable_payment_detail.sequence generator
CREATE OR REPLACE FUNCTION account_payable_payment_detail_sequence_increment_fn()
    RETURNS TRIGGER AS
$$
DECLARE
    accountPayablePayment CONSTANT   INT := new.account_payable_payment_id;
    maxAccountPayablePaymentDetailSequence INT;
BEGIN
    PERFORM pg_advisory_xact_lock(accountPayablePayment);

    maxAccountPayablePaymentDetailSequence := (SELECT COALESCE(MAX(sequence), 0) + 1
                                       FROM account_payable_payment_detail
                                       WHERE account_payable_payment_id = accountPayablePayment);

    new.sequence := maxAccountPayablePaymentDetailSequence;

    RETURN new;
END;
$$
    LANGUAGE plpgsql STRICT;
CREATE TRIGGER account_payable_payment_detail_sequence_auto_trg
    BEFORE INSERT
    ON account_payable_payment_detail
    FOR EACH ROW
EXECUTE PROCEDURE account_payable_payment_detail_sequence_increment_fn();
--- End account_payable_payment_detail.sequence generator --

