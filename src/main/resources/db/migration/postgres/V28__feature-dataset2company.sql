ALTER TABLE audit
    ADD COLUMN company_id BIGINT REFERENCES company(id);

UPDATE audit a
SET company_id = comp.id
FROM company comp
WHERE a.dataset = comp.dataset_code;

ALTER TABLE audit
    ALTER COLUMN company_id SET NOT NULL;

ALTER TABLE audit DROP COLUMN dataset;
