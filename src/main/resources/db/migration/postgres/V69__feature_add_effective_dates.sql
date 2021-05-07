ALTER TABLE division        ADD COLUMN effective_date date;  --adding withOUT null for alter to work
ALTER TABLE division        ADD COLUMN ending_date    date;
ALTER TABLE region          ADD COLUMN effective_date date;  --adding withOUT null for alter to work
ALTER TABLE region          ADD COLUMN ending_date    date;
ALTER TABLE region_to_store ADD COLUMN effective_date date;  --adding withOUT null for alter to work
ALTER TABLE region_to_store ADD COLUMN ending_date    date;

ALTER TABLE division        ADD CONSTRAINT check_effective_dates check(effective_date < ending_date);
ALTER TABLE region          ADD CONSTRAINT check_effective_dates check(effective_date < ending_date);
ALTER TABLE region_to_store ADD CONSTRAINT check_effective_dates check(effective_date < ending_date);

UPDATE division        SET effective_date = '1970-01-01';
UPDATE region          SET effective_date = '1970-01-01';
UPDATE region_to_store SET effective_date = '1970-01-01';

ALTER TABLE division        ALTER COLUMN effective_date SET NOT NULL;
ALTER TABLE region          ALTER COLUMN effective_date SET NOT NULL;
ALTER TABLE region_to_store ALTER COLUMN effective_date SET NOT NULL;
