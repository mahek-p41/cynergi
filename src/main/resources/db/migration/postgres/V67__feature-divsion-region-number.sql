-- employee updates
CREATE SEQUENCE employee_number_seq START 10000;
ALTER TABLE employee
    ALTER COLUMN number TYPE BIGINT;

ALTER TABLE employee
    ALTER COLUMN number SET DEFAULT nextval('employee_number_seq');
-- employee updates

-- CYN-808 Restrict values (N|R|D|A) for alternative_store_indicator
ALTER TABLE employee
   ADD CONSTRAINT check_alternative_store_indicator_restricted_values
   CHECK (UPPER(alternative_store_indicator) IN ('N', 'R', 'D', 'A' ));
