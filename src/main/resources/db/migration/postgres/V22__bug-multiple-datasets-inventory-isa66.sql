ALTER TABLE employee
    ADD COLUMN dataset VARCHAR(6);
ALTER TABLE audit
    ADD COLUMN dataset VARCHAR(6) CHECK ( char_length(trim(dataset)) = 6 ) NOT NULL;
