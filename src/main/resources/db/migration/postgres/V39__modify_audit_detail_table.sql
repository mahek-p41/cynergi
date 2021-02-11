ALTER TABLE audit_detail
   ADD column lookup_key VARCHAR(200);

-- delete the duplicate data
DELETE
FROM audit_detail
WHERE id NOT in (
   SELECT MAX(id)
   FROM audit_detail i
   GROUP BY audit_id, alt_id, serial_number
);

-- migrate the lookup key
UPDATE audit_detail d
SET lookup_key = i.lookup_key
FROM fastinfo_prod_import.inventory_vw i
WHERE d.alt_id = i.alt_id AND d.serial_number = i.serial_number;

-- add unique constraint
ALTER TABLE audit_detail
ADD CONSTRAINT unique_inventory_uq UNIQUE (lookup_key, audit_id);

