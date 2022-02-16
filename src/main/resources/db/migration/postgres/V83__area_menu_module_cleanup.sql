ALTER TABLE area_type_domain
ADD COLUMN menu_visible BOOLEAN DEFAULT TRUE NOT NULL;

UPDATE area_type_domain
SET menu_visible = FALSE
WHERE id IN (5, 6);

UPDATE menu_type_domain
SET localization_code = 'po.report.with.export'
WHERE id = 22;

UPDATE menu_type_domain
SET area_type_id = NULL
WHERE id IN (22, 23, 24, 25, 26);
