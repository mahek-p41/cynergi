ALTER TABLE division DROP CONSTRAINT uq_division;
ALTER TABLE division ADD CONSTRAINT division_company_number_uq UNIQUE (company_id, number);

ALTER TABLE region DROP CONSTRAINT uq_region;
ALTER TABLE region ADD CONSTRAINT region_division_number_uq UNIQUE (division_id, number);
