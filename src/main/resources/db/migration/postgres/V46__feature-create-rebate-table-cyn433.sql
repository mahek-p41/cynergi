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
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    company_id         BIGINT REFERENCES company (id)                           NOT NULL,
    vendor_id BIGINT REFERENCES vendor(id)                                      NOT NULL,
    status_type_id BIGINT REFERENCES account_status_type_domain(id)                     NOT NULL,
    description character varying(40) CHECK (char_length(trim(description)) > 1)   NOT NULL,
    rebate_type_id BIGINT REFERENCES rebate_type_domain (id)                    NOT NULL, -- equates to REBATE-PERCENT-UNIT-IND CNRG DD
    percent Numeric(8,7),
    amount_per_unit Numeric (11,2),
    accrual_indicator BOOLEAN DEFAULT FALSE                                     NOT NULL,
    general_ledger_debit_account_id BIGINT REFERENCES account(id),
    general_ledger_credit_account_id BIGINT REFERENCES account(id)              NOT NULL
 );

CREATE UNIQUE INDEX rebate_id_desc_uq ON rebate USING btree (vendor_id, (UPPER(description)));

CREATE INDEX rebate_vendor_id_idx ON rebate(vendor_id);
CREATE INDEX rebate_status_type_id_idx ON rebate(status_type_id);
CREATE INDEX rebate_type_id_idx ON rebate(rebate_type_id);
CREATE INDEX rebate_gl_debit_account_id_idx ON rebate(general_ledger_debit_account_id);
CREATE INDEX rebate_gl_credit_account_id_idx ON rebate(general_ledger_credit_account_id);

CREATE TRIGGER update_rebate_trg
   BEFORE UPDATE
   ON rebate
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
