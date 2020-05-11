ALTER TABLE schedule
    ADD column company_id BIGINT REFERENCES company(id);

UPDATE schedule SET company_id = (SELECT id FROM company ORDER BY id LIMIT 1);

ALTER TABLE schedule
    ALTER COLUMN company_id SET NOT NULL;
