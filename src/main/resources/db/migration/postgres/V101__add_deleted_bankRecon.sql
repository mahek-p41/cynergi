ALTER TABLE bank_reconciliation ADD COLUMN deleted    BOOLEAN DEFAULT FALSE   NOT NULL;

DROP INDEX bank_reconciliation_company_id_idx;
CREATE INDEX bank_reconciliation_company_id_idx ON bank_reconciliation (company_id, deleted)
WHERE deleted = FALSE;

DROP INDEX bank_reconciliation_bank_id_idx;
CREATE INDEX bank_reconciliation_bank_id_idx ON bank_reconciliation (bank_id, deleted)
WHERE deleted = FALSE;

DROP INDEX bank_reconciliation_type_domain_id_idx;
CREATE INDEX bank_reconciliation_type_domain_id_idx ON bank_reconciliation (type_id, deleted)
WHERE deleted = FALSE;
