CREATE TABLE bank_reconciliation_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO bank_reconciliation_type_domain(id, value, description, localization_code)
VALUES (1, 'C', 'Check', 'check'),
       (2, 'D', 'Deposit', 'deposit'),
       (3, 'F', 'Fee', 'fee'),
       (4, 'I', 'Interest', 'interest'),
       (5, 'M', 'Miscellaneous', 'miscellaneous'),
       (6, 'S', 'Service Charge', 'service.charge'),
       (7, 'T', 'Transfer', 'transfer'),
       (8, 'R', 'Return Check', 'return.check'),
       (9, 'V', 'Void', 'void');



CREATE TABLE bank_reconciliation
(
    id               BIGSERIAL                                              NOT NULL PRIMARY KEY,
    uu_row_id        UUID        DEFAULT uuid_generate_v1()                 NOT NULL,
    time_created     TIMESTAMPTZ DEFAULT clock_timestamp()                  NOT NULL,
    time_updated     TIMESTAMPTZ DEFAULT clock_timestamp()                  NOT NULL,
    company_id       BIGINT REFERENCES company (id)                         NOT NULL,
    bank_id          BIGINT REFERENCES bank (id)                            NOT NULL,
    type_id          BIGINT REFERENCES bank_reconciliation_type_domain (id) NOT NULL,
    transaction_date DATE                                                   NOT NULL,
    -- transaction date is populated by different programs but the user can specify a different date in program GLJE
    cleared_date     DATE,
    amount           NUMERIC(12, 2)                                         NOT NULL,
    description      VARCHAR(15)                                            NOT NULL,
    search_vector    TSVECTOR                                               NOT NULL,
    -- tsv_description will allow for faster search allowing the use of index below due to what is stored in this field
    document         INTEGER --- This holds the check number in relation to AP Check, system date for SUMGLINT and GLJE
);
CREATE TRIGGER bank_reconciliation_trg
    BEFORE UPDATE
    ON bank_reconciliation
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX bank_reconciliation_company_id_idx ON bank_reconciliation (company_id);
CREATE INDEX bank_reconciliation_bank_id_idx ON bank_reconciliation (bank_id);
CREATE INDEX bank_reconciliation_type_domain_id_idx ON bank_reconciliation (type_id);
CREATE INDEX bank_reconciliation_vector_idx ON bank_reconciliation (search_vector);

CREATE OR REPLACE FUNCTION bank_reconciliation_search_update_fn()
    RETURNS TRIGGER AS
$$
BEGIN
    new.search_vector := to_tsvector(new.description);

    RETURN new;
END;
$$
    LANGUAGE plpgsql STRICT;

CREATE TRIGGER bank_reconciliation_search_update_trg
    BEFORE INSERT OR UPDATE
    ON bank_reconciliation FOR EACH ROW EXECUTE PROCEDURE bank_reconciliation_search_update_fn();
