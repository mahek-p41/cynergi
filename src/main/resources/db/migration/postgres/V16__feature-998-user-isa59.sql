ALTER TABLE employee
    DROP CONSTRAINT employee_pass_code_check;
ALTER TABLE employee
    ADD CONSTRAINT employee_pass_code_check CHECK ( char_length(pass_code) > 58 );
ALTER TABLE employee
    ALTER COLUMN pass_code TYPE TEXT;

UPDATE employee
SET pass_code = '$2a$10$s62wpFGlz2iJ3yCzhgnof.6d7RQ8BZLesaKVzoo5YtneqYXUNFBvO'
WHERE number = 998;

