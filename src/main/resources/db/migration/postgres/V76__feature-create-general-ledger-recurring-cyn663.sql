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
    id                 UUID        DEFAULT uuid_generate_v1()                       NOT NULL PRIMARY KEY,
    time_created       TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                        NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                        NOT NULL,
    company_id         UUID REFERENCES company (id)                                 NOT NULL,
    source_id          UUID REFERENCES general_ledger_source_codes (id)             NOT NULL,
    type_id            INTEGER REFERENCES general_ledger_recurring_type_domain (id) NOT NULL,
    reverse_indicator  BOOLEAN     DEFAULT FALSE                                    NOT NULL,
    message            TEXT,
    begin_date         DATE                                                         NOT NULL,
    end_date           DATE,
    last_transfer_date DATE,
    deleted            BOOLEAN     DEFAULT FALSE                                    NOT NULL
);
CREATE TRIGGER update_general_ledger_recurring_trg
    BEFORE UPDATE
    ON general_ledger_recurring
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX general_ledger_recurring_company_id_idx ON general_ledger_recurring(company_id) WHERE deleted is FALSE;
CREATE INDEX general_ledger_recurring_source_id_idx ON general_ledger_recurring(source_id) WHERE deleted is FALSE;
CREATE INDEX general_ledger_recurring_type_id_idx ON general_ledger_recurring(type_id) WHERE deleted is FALSE;

CREATE TABLE general_ledger_recurring_distribution
(
    id                                               UUID        DEFAULT uuid_generate_v1()        NOT NULL PRIMARY KEY,
    time_created                                     TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()         NOT NULL,
    time_updated                                     TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()         NOT NULL,
    general_ledger_recurring_id                      UUID REFERENCES general_ledger_recurring (id) NOT NULL,
    general_ledger_distribution_account_id           UUID REFERENCES account (id)                  NOT NULL,
    general_ledger_distribution_profit_center_id_sfk INTEGER                                       NOT NULL,
    general_ledger_distribution_amount               NUMERIC(11, 2)                                NOT NULL,
    deleted                                          BOOLEAN     DEFAULT FALSE                     NOT NULL
);
CREATE TRIGGER update_general_ledger_recurring_distribution_trg
    BEFORE UPDATE
    ON general_ledger_recurring_distribution
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


CREATE INDEX general_ledger_recurring_id_idx ON general_ledger_recurring_distribution (general_ledger_recurring_id) WHERE deleted is FALSE;
CREATE INDEX general_ledger_recurring_dist_account_idx ON general_ledger_recurring_distribution (general_ledger_distribution_account_id) WHERE deleted is FALSE;

COMMENT ON TABLE  general_ledger_recurring IS 'Table holds the defaults/values to allow manual creation of general ledger journal entries.';
COMMENT ON TABLE  general_ledger_recurring_distribution IS 'Table holds the general ledger recurring distributions associated with general_ledger_recurring table.';
