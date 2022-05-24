DROP INDEX vendor_group_unique_idx;

CREATE UNIQUE INDEX vendor_group_unique_idx ON vendor_group USING btree (company_id, LOWER(value), deleted)
WHERE deleted = false;

