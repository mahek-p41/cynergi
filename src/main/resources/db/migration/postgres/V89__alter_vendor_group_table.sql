DROP INDEX vendor_group_unique_idx;

CREATE UNIQUE INDEX vendor_group_unique_idx ON vendor_group USING btree (company_id, LOWER(value))
WHERE deleted = false;

DROP INDEX general_ledger_source_codes_unique_idx;

CREATE UNIQUE INDEX general_ledger_source_codes_unique_idx ON general_ledger_source_codes USING btree (company_id, LOWER(value))
WHERE deleted = false;
