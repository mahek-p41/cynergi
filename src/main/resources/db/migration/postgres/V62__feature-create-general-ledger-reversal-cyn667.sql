CREATE TABLE general_ledger_reversal
(
    id                            BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                     UUID                        DEFAULT uuid_generate_v1()                      NOT NULL,
    time_created                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    time_updated                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    company_id                    BIGINT REFERENCES company (id)                                              NOT NULL,
    source_id                     BIGINT REFERENCES general_ledger_source_codes (id)                          NOT NULL,
    date                          DATE                                                                        NOT NULL,
    reversal_date                 DATE                                                                        NOT NULL,
    comment                       TEXT,
    entry_month                   INTEGER                                                                      NOT NULL,
    entry_number                  INTEGER                                                                      NOT NULL
 );

CREATE TRIGGER update_general_ledger_reversal_trg
    BEFORE UPDATE
    ON general_ledger_reversal
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX general_ledger_reversal_company_id_idx ON general_ledger_reversal(company_id);
CREATE INDEX general_ledger_reversal_source_id_idx ON general_ledger_reversal(source_id);


CREATE TABLE general_ledger_reversal_distribution
(
    id                            BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                     UUID                        DEFAULT uuid_generate_v1()                      NOT NULL,
    time_created                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    time_updated                  TIMESTAMPTZ                 DEFAULT clock_timestamp()                       NOT NULL,
    general_ledger_reversal_id    BIGINT REFERENCES general_ledger_reversal(id)                               NOT NULL,
    general_ledger_reversal_distribution_account_id       BIGINT REFERENCES account (id)                      NOT NULL,
    general_ledger_reversal_distribution_profit_center_id_sfk INTEGER                                         NOT NULL,
    general_ledger_reversal_distribution_amount            NUMERIC(11,2)                                      NOT NULL
);
CREATE TRIGGER update_general_ledger_reversal_distribution_trg
    BEFORE UPDATE
    ON general_ledger_reversal_distribution
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE INDEX general_ledger_reversal_id_idx ON general_ledger_reversal_distribution (general_ledger_reversal_id);
CREATE INDEX general_ledger_reversal_dist_account_idx ON general_ledger_reversal_distribution (general_ledger_reversal_distribution_account_id);

COMMENT ON TABLE  general_ledger_reversal IS 'Table holds the store reversed accounting entries waiting to be posted.';
COMMENT ON TABLE  general_ledger_reversal_distribution IS 'Table holds the general ledger reversal distributions associated with general_ledger_reversal table.';
