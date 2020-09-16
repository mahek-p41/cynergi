CREATE TABLE general_ledger_source_codes
(
    id                                               BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                                        UUID        DEFAULT uuid_generate_v1()                                      NOT NULL,
    time_created                                     TIMESTAMPTZ DEFAULT clock_timestamp()                                       NOT NULL,
    time_updated                                     TIMESTAMPTZ DEFAULT clock_timestamp()                                       NOT NULL,
    company_id                                       BIGINT REFERENCES company (id)                                              NOT NULL,
    value                                            varchar(3)                                               NOT NULL,
    description                                      varchar(30)                                              NOT NULL,
    UNIQUE (company_id, value)
);
CREATE TRIGGER general_ledger_source_codes_trg
    BEFORE UPDATE
    ON general_ledger_source_codes
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE INDEX general_ledger_source_codes_company_idx ON general_ledger_source_codes (company_id);
