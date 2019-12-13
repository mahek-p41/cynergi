ALTER TABLE employee
    ADD column allow_auto_store_assign BOOLEAN DEFAULT FALSE,
    ALTER COLUMN store_number DROP NOT NULL ;

UPDATE employee
SET allow_auto_store_assign = true,
    store_number = null
WHERE number = 998;
