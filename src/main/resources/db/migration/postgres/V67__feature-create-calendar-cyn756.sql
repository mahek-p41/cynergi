CREATE TABLE financial_calendar
(
    id                   UUID        DEFAULT uuid_generate_v1()            NOT NULL PRIMARY KEY,
    time_created         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()             NOT NULL,
    time_updated         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()             NOT NULL,
    company_id           UUID REFERENCES company (id)                      NOT NULL,
    overall_period_id    INTEGER REFERENCES overall_period_type_domain (id) NOT NULL,
    period               INTEGER CHECK (period > 0 AND period < 13)        NOT NULL,
    period_from          DATE                                              NOT NULL,
    period_to            DATE                                              NOT NULL,
    fiscal_year          INTEGER                                           NOT NULL,
    general_ledger_open  BOOLEAN     DEFAULT FALSE                         NOT NULL,
    account_payable_open BOOLEAN     DEFAULT FALSE                         NOT NULL,
    UNIQUE (company_id, overall_period_id, period)
);
CREATE TRIGGER update_financial_calendar_trg
    BEFORE UPDATE
    ON financial_calendar
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX financial_calendar_company_id_idx ON financial_calendar(company_id);
CREATE INDEX financial_calendar_overall_period_id_idx ON financial_calendar(overall_period_id);

COMMENT ON TABLE financial_calendar IS 'The financial calendar is where your current fiscal year periods should be set up. These need to be accurate with NO
overlapping dates. These periods are used in the calculations of year end closes, as well as account analysis screen
period summaries. It will also determine what dates you can use when running financial statements.';


COMMENT ON COLUMN financial_calendar.overall_period_id IS 'Foreign key to overall_period domain table which will indicate the overall period for the entry
i.e. Current, Previous, Future ....';
