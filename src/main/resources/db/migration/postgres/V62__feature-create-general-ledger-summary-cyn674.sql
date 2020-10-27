CREATE TABLE overall_period_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    abbreviation      VARCHAR(100) CHECK (char_length (trim(abbreviation)) > 1)      NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);
INSERT INTO overall_period_type_domain(id, value, abbreviation, description, localization_code)
VALUES (1, 'R', 'Prior to Prev','Prior to Previous Financial Period', 'prior.to.previous.financial.period'),
       (2, 'P', 'Prev', 'Previous Financial Period', 'previous.financial.period'),
       (3, 'C', 'Curr', 'Current Financial Period', 'current.financial.period'),
       (4, 'N', 'Next','Next Financial Period', 'next.financial.period');



CREATE TABLE general_ledger_summary
(
    id                                              BIGSERIAL                              NOT NULL PRIMARY KEY,
    uu_row_id                                       UUID        DEFAULT uuid_generate_v1() NOT NULL,
    time_created                                    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    time_updated                                    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    company_id                                      BIGINT REFERENCES company (id)         NOT NULL,
    account_id                                      BIGINT REFERENCES account (id)         NOT NULL,
    profit_center_id_sfk                            INTEGER                                NOT NULL, --foreign key to store/primary location
    overall_period_id                               BIGINT REFERENCES overall_period_type_domain (id) NOT NULL,
    net_activity_period_1                           NUMERIC(13,2),
    net_activity_period_2                           NUMERIC(13,2),
    net_activity_period_3                           NUMERIC(13,2),
    net_activity_period_4                           NUMERIC(13,2),
    net_activity_period_5                           NUMERIC(13,2),
    net_activity_period_6                           NUMERIC(13,2),
    net_activity_period_7                           NUMERIC(13,2),
    net_activity_period_8                           NUMERIC(13,2),
    net_activity_period_9                           NUMERIC(13,2),
    net_activity_period_10                          NUMERIC(13,2),
    net_activity_period_11                          NUMERIC(13,2),
    net_activity_period_12                          NUMERIC(13,2),
    beginning_balance                               NUMERIC(13,2),
    closing_balance                                 NUMERIC(13,2),
    UNIQUE (company_id, account_id, profit_center_id_sfk)
 );
CREATE TRIGGER general_ledger_summary_trg
    BEFORE UPDATE
    ON general_ledger_summary
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX general_ledger_summary_company_id_idx
    ON general_ledger_summary (company_id);
CREATE INDEX general_ledger_summary_account_id_idx
    ON general_ledger_summary (account_id);


COMMENT ON COLUMN general_ledger_summary.profit_center_id_sfk IS 'Soft foreign key which will use Fastinfo store view until store is no longer shared';
COMMENT ON COLUMN general_ledger_summary.account_id IS 'Foreign key which joins to the account table';
COMMENT ON COLUMN general_ledger_summary.overall_period_id IS 'Foreign key to the overall_period_type_domain which indicates
which row in the calendar table to reference current, last year , prior to last year';


COMMENT ON TABLE  general_ledger_summary IS 'Aggregate Table holds the net activity values for the general ledger entries for the different periods defined in
 the financial_calendar as well as beginning and closing balances.';

