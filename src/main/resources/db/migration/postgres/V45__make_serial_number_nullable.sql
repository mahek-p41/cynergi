ALTER TABLE audit_exception
   DROP CONSTRAINT IF EXISTS audit_exception_inventory_id_check;

ALTER TABLE audit_detail
   DROP CONSTRAINT IF EXISTS audit_detail_inventory_id_check;
