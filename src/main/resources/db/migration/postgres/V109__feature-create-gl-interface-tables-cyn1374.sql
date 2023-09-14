CREATE TABLE verify_staging
(
    id                                                   UUID        DEFAULT uuid_generate_v1()                  NOT NULL PRIMARY KEY,
    time_created                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    time_updated                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    company_id                                           UUID        REFERENCES company (id)                     NOT NULL,
    store_number_sfk                                     INTEGER                                                 NOT NULL,
    business_date                                        DATE                                                    NOT NULL,
    verify_successful                                    BOOLEAN                                                 NOT NULL,
    error_amount                                         NUMERIC(13,2)                                           NOT NULL,
    moved_to_pending_journal_entries                     BOOLEAN     DEFAULT FALSE                               NOT NULL,
    deleted                                              BOOLEAN     DEFAULT FALSE                               NOT NULL
);

CREATE TRIGGER update_verify_staging_trg
    BEFORE UPDATE
    ON verify_staging
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX verify_staging_company_id_idx
    ON verify_staging (company_id);

COMMENT ON TABLE verify_staging IS 'Used by SUMMARY to General Ledger Interface. Record for each store for each business day.';



CREATE TABLE deposits_staging_deposit_type_domain
(
    id                                                   INTEGER                                                        NOT NULL PRIMARY KEY,
    value                                                VARCHAR(10)                                                    NOT NULL,
    description                                          VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code                                    VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE(value)
);

COMMENT ON TABLE deposits_staging_deposit_type_domain IS 'Used by SUMMARY to General Ledger Interface. Defines each of the deposit types available.';

INSERT INTO deposits_staging_deposit_type_domain(id, value, description, localization_code)
VALUES (1, 'DEP_1', 'Deposit Cash', 'deposit.cash'),
       (2, 'DEP_2', 'Deposit For Other Stores', 'deposit.for.other.stores'),
       (3, 'DEP_3', 'Deposit From Other Stores', 'deposit.from.other.stores'),
       (4, 'DEP_4', 'Deposit CC In Store', 'deposit..cc.in.store'),
       (5, 'DEP_5', 'Deposit ACH OLP', 'deposit.ach.old'),
       (6, 'DEP_6', 'Deposit CC OLP', 'deposit.cc.olp'),
       (7, 'DEP_7', 'Deposit Debit Card', 'deposit.debit.card');



CREATE TABLE deposits_staging
(
    id                                                   UUID        DEFAULT uuid_generate_v1()                         NOT NULL PRIMARY KEY,
    time_created                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                          NOT NULL,
    time_updated                                         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                          NOT NULL,
    company_id                                           UUID REFERENCES company (id)                                   NOT NULL,
    verify_id                                            UUID REFERENCES verify_staging (id)                            NOT NULL,
    store_number_sfk                                     INTEGER                                                        NOT NULL,
    business_date                                        DATE                                                           NOT NULL,
    deposit_type_id                                      INTEGER REFERENCES deposits_staging_deposit_type_domain (id)   NOT NULL,
    deposit_amount                                       NUMERIC(13,2)                                                  NOT NULL,
    deleted                                              BOOLEAN     DEFAULT FALSE                                      NOT NULL
);

CREATE TRIGGER update_deposits_staging_trg
    BEFORE UPDATE
    ON deposits_staging
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX deposits_staging_company_id_idx
    ON deposits_staging (company_id);

CREATE INDEX deposits_staging_verify_id_idx
    ON deposits_staging (verify_id);

CREATE INDEX deposits_staging_deposit_type_id_idx
    ON deposits_staging (deposit_type_id);

COMMENT ON TABLE deposits_staging IS 'Used by SUMMARY to General Ledger Interface. Record for each store for each deposit type for each business day.';
