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
ALTER TABLE employee RENAME COLUMN allow_auto_store_assign TO cynergi_system_admin;
ALTER TABLE employee ALTER COLUMN cynergi_system_admin SET DEFAULT FALSE;
ALTER TABLE employee ALTER COLUMN cynergi_system_admin SET NOT NULL;
ALTER TABLE employee ADD COLUMN alternative_store_indicator VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE employee ADD COLUMN alternative_area INTEGER DEFAULT 0 NOT NULL;

UPDATE employee
SET cynergi_system_admin        = true,
    alternative_store_indicator = 'A',
    pass_code = '$2a$10$DARfnaJtvWn02b8EfUoQSOPBnfQkyc.U/FO7RmSis28c7BqlZMy3y'
WHERE number = 998;

-- Alter company table
ALTER TABLE company RENAME COLUMN federal_tax_number TO federal_id_number;
