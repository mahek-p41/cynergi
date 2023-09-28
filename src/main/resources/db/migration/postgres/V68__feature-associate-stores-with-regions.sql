ALTER TABLE region_to_store
    ADD COLUMN company_id UUID;

UPDATE region_to_store
   SET company_id = division.company_id
FROM region_to_store r2s
      JOIN region ON  r2s.region_id = region.id
			   JOIN division ON region.division_id = division.id;

ALTER TABLE region_to_store ADD FOREIGN KEY (company_id) REFERENCES company (id);
ALTER TABLE region_to_store ALTER COLUMN company_id SET NOT NULL;
