CREATE TABLE account_payable_distribution_template
(
    id                                               BIGSERIAL                                                                   NOT NULL PRIMARY KEY,
    uu_row_id                                        UUID        DEFAULT uuid_generate_v1()                                      NOT NULL,
    time_created                                     TIMESTAMPTZ DEFAULT clock_timestamp()                                       NOT NULL,
    time_updated                                     TIMESTAMPTZ DEFAULT clock_timestamp()                                       NOT NULL,
    name                                             VARCHAR (10)                                                                NOT NULL,
    profit_center_sfk                                INTEGER                                                                     NOT NULL, --will be foreign key to store/home office
    account_id                                       BIGINT REFERENCES account (id)                                              NOT NULL,
    company_id                                       BIGINT REFERENCES company (id)                                              NOT NULL,
    percent                                          NUMERIC(8,7)                                                                NOT NULL,
    UNIQUE (company_id, name, profit_center_sfk, account_id)
 );

CREATE TRIGGER account_payable_distribution_template_trg
    BEFORE UPDATE
    ON account_payable_distribution_template
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX account_payable_distribution_account_idx ON account_payable_distribution_template (account_id);

CREATE INDEX account_payable_distribution_company_idx ON account_payable_distribution_template (company_id);

COMMENT ON TABLE account_payable_distribution_template IS 'Table holds the template values which can be used in the account payable invoice and journal entry creation
 processes';
