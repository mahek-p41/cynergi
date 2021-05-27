CREATE TABLE general_ledger_recurring_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO general_ledger_recurring_type_domain(id, value, description, localization_code)
VALUES (1, 'D', 'Daily', 'daily'),
       (2, 'M', 'Monthly', 'monthly'),
       (3, 'W', 'Weekly', 'weekly'),
       (4, 'O', 'Other', 'other');


CREATE TABLE general_ledger_recurring
(
    id                            BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                     UUID                        DEFAULT uuid_generate_v1()                      NOT NULL,
    time_created                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    time_updated                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    company_id                    BIGINT REFERENCES company (id)                                              NOT NULL,
    source_id                     BIGINT REFERENCES general_ledger_source_codes (id)                          NOT NULL,
    type_id                       BIGINT REFERENCES general_ledger_recurring_type_domain (id)                 NOT NULL,
    reverse_indicator             BOOLEAN DEFAULT FALSE                                                       NOT NULL,
    message                       TEXT,
    begin_date                    DATE                                                                        NOT NULL,
    end_date                      DATE,
    last_transfer_date            DATE
   );

CREATE TRIGGER update_general_ledger_recurring_trg
    BEFORE UPDATE
    ON general_ledger_recurring
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX general_ledger_recurring_company_id_idx ON general_ledger_recurring(company_id);
CREATE INDEX general_ledger_recurring_source_id_idx ON general_ledger_recurring(source_id);
CREATE INDEX general_ledger_recurring_type_id_idx ON general_ledger_recurring(type_id);

CREATE TABLE general_ledger_recurring_distribution
(
    id                            BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                     UUID                        DEFAULT uuid_generate_v1()                      NOT NULL,
    time_created                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    time_updated                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    general_ledger_recurring_id                  BIGINT REFERENCES general_ledger_recurring(id)               NOT NULL,
    general_ledger_distribution_account_id       BIGINT REFERENCES account (id)                               NOT NULL,
    general_ledger_distribution_profit_center_id_sfk INTEGER                                                  NOT NULL,
    general_ledger_distribution_amount            NUMERIC(11,2)                                               NOT NULL
);
CREATE TRIGGER update_general_ledger_recurring_distribution_trg
    BEFORE UPDATE
    ON general_ledger_recurring_distribution
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE INDEX general_ledger_recurring_id_idx ON general_ledger_recurring_distribution (general_ledger_recurring_id);
CREATE INDEX general_ledger_recurring_dist_account_idx ON general_ledger_recurring_distribution (general_ledger_distribution_account_id);

COMMENT ON TABLE  general_ledger_recurring IS 'Table holds the defaults/values to allow manual creation of general ledger journal entries.';
COMMENT ON TABLE  general_ledger_recurring_distribution IS 'Table holds the general ledger recurring distributions associated with general_ledger_recurring table.';
