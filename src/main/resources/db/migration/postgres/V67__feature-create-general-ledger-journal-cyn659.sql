CREATE TABLE general_ledger_journal
(
    id                   UUID        DEFAULT uuid_generate_v1()           NOT NULL PRIMARY KEY,
    time_created         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()            NOT NULL,
    time_updated         TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()            NOT NULL,
    company_id           UUID REFERENCES company (id)                     NOT NULL,
    account_id           UUID REFERENCES account (id)                     NOT NULL,
    profit_center_id_sfk INTEGER                                          NOT NULL, --foreign key to store/primary location
    date                 DATE                                             NOT NULL,
    source_id            UUID REFERENCES general_ledger_source_codes (id) NOT NULL,
    amount               NUMERIC(13, 2)                                   NOT NULL,
    message              TEXT
);
CREATE TRIGGER general_ledger_journal_trg
    BEFORE UPDATE
    ON general_ledger_journal
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE INDEX general_ledger_journal_company_id_idx
    ON general_ledger_journal (company_id);
CREATE INDEX general_ledger_journal_account_id_idx
    ON general_ledger_journal (account_id);
CREATE INDEX general_ledger_journal_source_id_idx
    ON general_ledger_journal (source_id);


COMMENT ON COLUMN general_ledger_journal.profit_center_id_sfk IS 'Soft foreign key which will use Fastinfo store view until store is no longer shared.';
COMMENT ON COLUMN general_ledger_journal.account_id IS 'Foreign key which joins to the account table';
COMMENT ON COLUMN general_ledger_journal.amount IS 'Amount for transaction(Debit=positive  Credit=negative)';

COMMENT ON TABLE  general_ledger_journal IS 'Table holds the accounting entries for the user to review, report on, export and post. Records can stay in this table for months
depending on how efficient the user is.';
