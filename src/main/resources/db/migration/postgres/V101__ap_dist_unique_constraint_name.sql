CREATE UNIQUE INDEX account_payable_distribution_template_unique_idx ON account_payable_distribution_template USING btree (company_id, LOWER(name), deleted)
WHERE deleted = false;
