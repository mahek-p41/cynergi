UPDATE audit_exception
SET lookup_key = barcode
WHERE lookup_key is NULL
;

ALTER TABLE audit_exception
   DROP CONSTRAINT IF EXISTS audit_exception_alt_id_check,
   DROP CONSTRAINT IF EXISTS audit_exception_bar_code_check,
   ALTER COLUMN barcode DROP NOT NULL,
   ALTER COLUMN lookup_key SET NOT NULL
;

UPDATE audit_detail
SET lookup_key = barcode
WHERE lookup_key is NULL
;

ALTER TABLE audit_detail
   DROP CONSTRAINT IF EXISTS audit_detail_alt_id_check,
   DROP CONSTRAINT IF EXISTS audit_detail_bar_code_check,
   ALTER COLUMN alt_id DROP NOT NULL,
   ALTER COLUMN barcode DROP NOT NULL,
   ALTER COLUMN serial_number DROP NOT NULL,
   ALTER COLUMN lookup_key SET NOT NULL
;
