CREATE TABLE rebate_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)               NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO rebate_type_domain (id, value, description, localization_code)
VALUES (1, 'P', 'Percent', 'percent'),
       (2, 'U', 'Unit', 'unit');


CREATE TABLE rebate
(
    id                               UUID        DEFAULT uuid_generate_v1()                 NOT NULL PRIMARY KEY,
    time_created                     TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                  NOT NULL,
    time_updated                     TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                  NOT NULL,
    company_id                       UUID REFERENCES company (id)                           NOT NULL,
    status_type_id                   BIGINT REFERENCES account_status_type_domain (id)      NOT NULL,
    description                      VARCHAR(40) CHECK (CHAR_LENGTH(TRIM(description)) > 1) NOT NULL,
    rebate_type_id                   BIGINT REFERENCES rebate_type_domain (id)              NOT NULL, -- equates to REBATE-PERCENT-UNIT-IND CNRG DD
    percent                          NUMERIC(8, 7),
    amount_per_unit                  NUMERIC(11, 2),
    accrual_indicator                BOOLEAN     DEFAULT FALSE                              NOT NULL,
    general_ledger_debit_account_id  UUID REFERENCES account (id),
    general_ledger_credit_account_id UUID REFERENCES account (id)                           NOT NULL
);
CREATE TRIGGER update_rebate_trg
    BEFORE UPDATE
    ON rebate
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE UNIQUE INDEX rebate_id_desc_uq ON rebate USING btree (company_id, UPPER(description));

CREATE INDEX rebate_status_type_id_idx ON rebate(status_type_id);
CREATE INDEX rebate_type_id_idx ON rebate(rebate_type_id);
CREATE INDEX rebate_gl_debit_account_id_idx ON rebate(general_ledger_debit_account_id);
CREATE INDEX rebate_gl_credit_account_id_idx ON rebate(general_ledger_credit_account_id);

CREATE TABLE rebate_to_vendor
(
    rebate_id UUID REFERENCES rebate (id) NOT NULL,
    vendor_id UUID REFERENCES vendor (id)   NOT NULL,
    UNIQUE (rebate_id, vendor_id)
);
