ALTER TABLE employee
    DROP CONSTRAINT employee_pass_code_check;
ALTER TABLE employee
    ALTER COLUMN pass_code TYPE VARCHAR(60);

UPDATE employee
SET pass_code = '$2a$10$s62wpFGlz2iJ3yCzhgnof.6d7RQ8BZLesaKVzoo5YtneqYXUNFBvO'
WHERE number = 998;

ALTER TABLE employee
    DROP CONSTRAINT employee_number_pass_code_key;
