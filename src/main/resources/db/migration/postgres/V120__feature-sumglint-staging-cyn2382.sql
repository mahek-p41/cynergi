ALTER TABLE accounting_entries_staging
   DROP CONSTRAINT accounting_entries_staging_unique_constraint;

ALTER TABLE accounting_entries_staging
   ADD COLUMN deposit_type_id    INTEGER;

ALTER TABLE accounting_entries_staging
  ADD CONSTRAINT accounting_entries_staging_deposit_type_id_fkey FOREIGN KEY (deposit_type_id)
  REFERENCES deposits_staging_deposit_type_domain (id);

ALTER TABLE accounting_entries_staging
ADD CONSTRAINT accounting_entries_staging_unique_constraint
UNIQUE (company_id, store_number_sfk, business_date, account_id, deposit_type_id, deleted);
