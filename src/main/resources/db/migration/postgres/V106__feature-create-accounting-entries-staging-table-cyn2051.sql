CREATE TABLE accounting_entries_staging
(
    id                     UUID         DEFAULT uuid_generate_v1()                     NOT NULL PRIMARY KEY,
    time_created           TIMESTAMPTZ  DEFAULT clock_timestamp()                      NOT NULL,
    time_updated           TIMESTAMPTZ  DEFAULT clock_timestamp()                      NOT NULL,
    company_id             UUID         REFERENCES company (id)                        NOT NULL,
    verify_id              UUID         REFERENCES verify_staging (id)                 NOT NULL,
    store_number_sfk       INTEGER                                                     NOT NULL,
    business_date          DATE                                                        NOT NULL,
    account_id             UUID         REFERENCES account (id)                        NOT NULL,
    profit_center_id_sfk   INTEGER                                                     NOT NULL,
    source_id              UUID         REFERENCES general_ledger_source_codes (id)    NOT NULL,
    journal_entry_amount   NUMERIC(13,2)                                               NOT NULL
);

COMMENT ON TABLE security_access_point_type_domain IS 'Security points for HOA application.';


CREATE TRIGGER update_accounting_entries_staging_trg
    BEFORE UPDATE
    ON accounting_entries_staging
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX accounting_entries_staging_verify_id_idx ON accounting_entries_staging (verify_id);
CREATE INDEX accounting_entries_staging_account_id_idx ON accounting_entries_staging (account_id);
CREATE INDEX accounting_entries_staging_source_id_idx ON accounting_entries_staging (source_id);
