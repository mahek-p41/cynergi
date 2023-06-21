DROP INDEX general_ledger_source_codes_unique_idx;

CREATE UNIQUE INDEX general_ledger_source_codes_unique_idx ON general_ledger_source_codes USING btree (company_id, LOWER(value), deleted)
WHERE deleted = false;

