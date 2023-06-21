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

UPDATE module_type_domain
SET localization_code = 'list.po.by.sequence.number'
WHERE id = 33;

DELETE FROM module_type_domain
WHERE menu_type_id IN (1, 36, 39);

DELETE FROM menu_type_domain
WHERE id IN (1, 36, 39);

