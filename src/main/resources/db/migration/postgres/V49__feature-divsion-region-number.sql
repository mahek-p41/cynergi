-- division updates
ALTER TABLE division
    ALTER COLUMN number SET DEFAULT currval('division_id_seq');
-- division updates

-- region updates
ALTER TABLE region
    ALTER COLUMN number SET DEFAULT currval('region_id_seq');
-- region updates

-- employee updates
ALTER TABLE employee
    ALTER COLUMN number TYPE BIGINT;

ALTER TABLE employee
    ALTER COLUMN number SET DEFAULT currval('employee_id_seq');
-- employee updates

-- CYN-808 Restrict values (N|R|D|A) for alternative_store_indicator
ALTER TABLE employee
   ADD CONSTRAINT check_alternative_store_indicator_restricted_values
   CHECK (UPPER(alternative_store_indicator) IN ('N', 'R', 'D', 'A' ));
