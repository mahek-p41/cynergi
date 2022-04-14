CREATE UNIQUE INDEX newidx ON general_ledger_source_codes USING btree (company_id, LOWER(value), deleted)
WHERE deleted = false;

DROP INDEX general_ledger_source_codes_unique_idx;

ALTER INDEX newidx RENAME to general_ledger_source_codes_unique_idx;
