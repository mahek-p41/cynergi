ALTER TABLE division        ADD COLUMN effective_date DATE DEFAULT CURRENT_DATE;  --adding without null for alter to work
ALTER TABLE division        ADD COLUMN ending_date    DATE;
ALTER TABLE region          ADD COLUMN effective_date DATE DEFAULT CURRENT_DATE;  --adding without null for alter to work
ALTER TABLE region          ADD COLUMN ending_date    DATE;

ALTER TABLE division        ADD CONSTRAINT division_check_effective_dates check(ending_date IS NULL OR effective_date < ending_date);
ALTER TABLE region          ADD CONSTRAINT region_check_effective_dates check(ending_date IS NULL OR effective_date < ending_date);

ALTER TABLE division        ALTER COLUMN effective_date SET NOT NULL;
ALTER TABLE region          ALTER COLUMN effective_date SET NOT NULL;
