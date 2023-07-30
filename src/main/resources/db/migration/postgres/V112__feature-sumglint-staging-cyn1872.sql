--Add unique constraints required by ON CONFLICT statements
ALTER TABLE verify_staging
ADD CONSTRAINT verify_staging_unique_constraint
UNIQUE (company_id, store_number_sfk, business_date, deleted);

ALTER TABLE deposits_staging
ADD CONSTRAINT deposits_staging_unique_constraint
UNIQUE (verify_id, deposit_type_id, deleted);

ALTER TABLE accounting_entries_staging
ADD CONSTRAINT accounting_entries_staging_unique_constraint
UNIQUE (company_id, store_number_sfk, business_date, deleted);

