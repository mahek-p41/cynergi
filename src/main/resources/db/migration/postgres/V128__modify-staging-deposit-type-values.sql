UPDATE deposits_staging_deposit_type_domain
SET value = REPLACE(value, '_', '-')
WHERE value LIKE 'DEP_%';
