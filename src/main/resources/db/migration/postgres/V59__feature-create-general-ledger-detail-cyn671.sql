CREATE TABLE general_ledger_detail
(
    id                                              BIGSERIAL                              NOT NULL PRIMARY KEY,
    uu_row_id                                       UUID        DEFAULT uuid_generate_v1() NOT NULL,
    time_created                                    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    time_updated                                    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
    company_id                                      BIGINT REFERENCES company (id)         NOT NULL,
    account_id                                      BIGINT REFERENCES account (id)         NOT NULL,
    profit_center_id_sfk                            INTEGER                                NOT NULL, --foreign key to store/primary location
    date                                            DATE                                   NOT NULL,
    source_id                                       BIGINT REFERENCES general_ledger_source_codes (id)  NOT NULL,
    amount                                          NUMERIC(13,2)                                       NOT NULL,
    message                                         TEXT,
    employee_number_id_sfk                          INTEGER,
    journal_entry_number                            INTEGER
   );
CREATE TRIGGER general_ledger_detail_trg
    BEFORE UPDATE
    ON general_ledger_detail
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE INDEX general_ledger_detail_company_id_idx
    ON general_ledger_detail (company_id);
CREATE INDEX general_ledger_detail_account_id_idx
    ON general_ledger_detail (account_id);
CREATE INDEX general_ledger_detail_source_id_idx
    ON general_ledger_detail (source_id);


COMMENT ON COLUMN general_ledger_detail.employee_number_id_sfk IS 'Employee ID number can be null due to migration';
COMMENT ON COLUMN general_ledger_detail.profit_center_id_sfk IS 'Soft foreign key which will use Fastinfo store view until store is no longer shared';
COMMENT ON COLUMN general_ledger_detail.account_id IS 'Foreign key which joins to the account table';
COMMENT ON COLUMN general_ledger_detail.amount IS 'Amount for transaction(Debit=positive  Credit=negative)';

COMMENT ON TABLE  general_ledger_detail IS 'Table holds the details for the general ledger entries contains 1 row per
                 transaction detail.';
