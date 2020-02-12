-- Alter audit table
ALTER TABLE audit ADD COLUMN company_id BIGINT REFERENCES company(id);

UPDATE audit a
SET company_id = comp.id
FROM company comp
WHERE a.dataset = comp.dataset_code;

ALTER TABLE audit ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE audit DROP COLUMN dataset;

-- Alter employee table
ALTER TABLE employee ADD COLUMN company_id BIGINT REFERENCES company(id);

UPDATE employee e
SET company_id = comp.id
FROM company comp
WHERE e.dataset = comp.dataset_code;

ALTER TABLE employee ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE employee DROP COLUMN dataset;
