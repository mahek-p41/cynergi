ALTER TABLE general_ledger_reversal
ADD COLUMN deleted    BOOLEAN DEFAULT FALSE   NOT NULL;

DROP INDEX general_ledger_reversal_company_id_idx;
CREATE INDEX general_ledger_reversal_company_id_idx ON general_ledger_reversal (company_id, deleted)
WHERE deleted = FALSE;
DROP INDEX general_ledger_reversal_source_id_idx;
CREATE INDEX general_ledger_reversal_source_id_idx ON general_ledger_reversal (source_id, deleted)
WHERE deleted = FALSE;

ALTER TABLE general_ledger_reversal_distribution
ADD COLUMN deleted    BOOLEAN DEFAULT FALSE   NOT NULL;

DROP INDEX general_ledger_reversal_id_idx;
CREATE INDEX general_ledger_reversal_id_idx ON general_ledger_reversal_distribution (general_ledger_reversal_id, deleted)
WHERE deleted = FALSE;
DROP INDEX general_ledger_reversal_dist_account_idx;
CREATE INDEX general_ledger_reversal_dist_account_idx ON general_ledger_reversal_distribution (general_ledger_reversal_distribution_account_id, deleted)
WHERE deleted = FALSE;
