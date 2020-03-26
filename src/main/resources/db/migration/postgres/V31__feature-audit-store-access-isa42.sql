UPDATE employee
SET alternative_store_indicator = 'A'
WHERE number = 998;

ALTER TABLE division
  RENAME COLUMN employee_number TO manager_number;
ALTER TABLE division
  ALTER COLUMN manager_number DROP DEFAULT;
ALTER TABLE region
  RENAME COLUMN employee_number TO manager_number;
ALTER TABLE region
    ALTER COLUMN manager_number DROP DEFAULT;
