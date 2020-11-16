CREATE TABLE account_status_type_domain
(
    id                integer                                                        NOT NULL PRIMARY KEY,
    value             varchar(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       varchar(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code varchar(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO account_status_type_domain(id, value, description, localization_code)
VALUES (1, 'A', 'Active', 'active'),
       (2, 'I', 'Inactive', 'inactive');

-- begin account setup
CREATE TABLE account_type_domain
(
    id                integer                                                        NOT NULL PRIMARY KEY,
    value             varchar(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       varchar(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code varchar(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO account_type_domain(id, value, description, localization_code)
VALUES (1, 'A', 'Asset Account', 'account.asset'),
       (2, 'C', 'Capital Account', 'account.capital'),
       (3, 'E', 'Expense Account', 'account.expense'),
       (4, 'L', 'Liability Account', 'account.liability'),
       (5, 'R', 'Revenue Account', 'account.revenue');

CREATE TABLE normal_account_balance_type_domain
(
    id                integer                                                        NOT NULL PRIMARY KEY,
    value             varchar(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       varchar(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code varchar(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO normal_account_balance_type_domain(id, value, description, localization_code)
VALUES (1, 'C', 'Credit', 'credit'),
       (2, 'D', 'Debit', 'debit');

-- being account setup
CREATE TABLE account
(
    id                             BIGSERIAL                                                     NOT NULL PRIMARY KEY,
    uu_row_id                      UUID                        DEFAULT uuid_generate_v1()        NOT NULL,
    time_created                   TIMESTAMPTZ                 DEFAULT clock_timestamp()         NOT NULL,
    time_updated                   TIMESTAMPTZ                 DEFAULT clock_timestamp()         NOT NULL,
    company_id                     BIGINT REFERENCES company (id)                                NOT NULL,
    number                         BIGINT CHECK ( number > 0 )                                   NOT NULL,
    name                           VARCHAR(100) CHECK ( char_length(trim(name)) > 1)             NOT NULL,
    type_id                        BIGINT REFERENCES account_type_domain (id)                    NOT NULL,
    normal_account_balance_type_id BIGINT REFERENCES normal_account_balance_type_domain (id)     NOT NULL,
    status_type_id                 BIGINT REFERENCES account_status_type_domain (id)                     NOT NULL,
    form_1099_field                INTEGER, -- field # on the 1099 form for this account
    corporate_account_indicator    BOOLEAN                     DEFAULT FALSE                     NOT NULL,
    search_vector                  TSVECTOR                                                      NOT NULL,
    UNIQUE (company_id, number)
);

CREATE OR REPLACE FUNCTION account_search_update_fn()
    RETURNS TRIGGER AS
$$
DECLARE
    accountNum CONSTANT TEXT := CAST(new.number AS TEXT);
    accountDesc CONSTANT TEXT := new.name;
BEGIN
    new.search_vector :=
        setweight(to_tsvector(accountNum), 'A') ||
        setweight(to_tsvector(accountDesc), 'B');

    RETURN new;
END;
$$
    LANGUAGE plpgsql STRICT;

CREATE INDEX account_company_id_idx ON account (company_id);
CREATE INDEX account_type_id_idx ON account (type_id);
CREATE INDEX account_status_type_id_idx ON account (status_type_id);
CREATE INDEX account_search_idx ON account USING gist(name gist_trgm_ops);
CREATE INDEX account_vector_idx ON account USING gin(search_vector);

CREATE TRIGGER account_search_update_trg
    BEFORE INSERT OR UPDATE
    ON account FOR EACH ROW EXECUTE PROCEDURE account_search_update_fn();

CREATE TRIGGER account_account_trg
    BEFORE UPDATE
    ON account
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
-- end account setup

-- begin bank setup
CREATE TABLE bank
(
    id                               BIGSERIAL                                                  NOT NULL PRIMARY KEY,
    uu_row_id                        UUID                        DEFAULT uuid_generate_v1()     NOT NULL,
    time_created                     TIMESTAMPTZ                 DEFAULT clock_timestamp()      NOT NULL,
    time_updated                     TIMESTAMPTZ                 DEFAULT clock_timestamp()      NOT NULL,
    company_id                       BIGINT REFERENCES company (id)                             NOT NULL,
    number                           BIGINT CHECK ( number > 0 ) DEFAULT currval('bank_id_seq') NOT NULL,
    name                             varchar(50) CHECK ( char_length(trim(name)) > 1)           NOT NULL,
    general_ledger_profit_center_sfk INTEGER CHECK ( general_ledger_profit_center_sfk > 0 )     NOT NULL, --profit center is store or possibly home office
    general_ledger_account_id        BIGINT REFERENCES account (id)                             NOT NULL
);
CREATE TRIGGER update_bank_trg
    BEFORE UPDATE
    ON bank
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX bank_company_id_idx ON bank (company_id);
-- end bank setup
