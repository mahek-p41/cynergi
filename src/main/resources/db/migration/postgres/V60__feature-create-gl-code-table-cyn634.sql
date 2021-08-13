CREATE TABLE general_ledger_source_codes
(
    id           UUID        DEFAULT uuid_generate_v1() NOT NULL PRIMARY KEY,
    time_created TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()  NOT NULL,
    time_updated TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()  NOT NULL,
    company_id   UUID REFERENCES company (id)           NOT NULL,
    value        VARCHAR(3)                             NOT NULL,
    description  VARCHAR(30)                            NOT NULL,
    deleted      BOOLEAN     DEFAULT FALSE              NOT NULL,
    UNIQUE (company_id, value)
);
CREATE TRIGGER general_ledger_source_codes_trg
    BEFORE UPDATE
    ON general_ledger_source_codes
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


CREATE INDEX general_ledger_source_codes_company_idx ON general_ledger_source_codes (company_id) WHERE deleted is FALSE;
