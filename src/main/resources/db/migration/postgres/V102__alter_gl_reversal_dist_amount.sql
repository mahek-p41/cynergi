ALTER TABLE general_ledger_reversal_distribution
ALTER COLUMN general_ledger_reversal_distribution_amount TYPE NUMERIC(13, 2);

ALTER TABLE general_ledger_recurring_distribution
ALTER COLUMN general_ledger_distribution_amount TYPE NUMERIC(13, 2);
