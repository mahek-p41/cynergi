ALTER TABLE audit_exception
    RENAME COLUMN signed_off TO approved;

ALTER TABLE audit_exception
    RENAME COLUMN signed_off_by TO approved_by;

UPDATE audit_status_type_domain SET value = 'APPROVED' WHERE id = 5;
UPDATE audit_status_type_domain SET description = 'Approved' WHERE id = 5;
UPDATE audit_status_type_domain SET localization_code = 'audit.status.approved' WHERE id = 5;

UPDATE audit_permission_type_domain
SET description = 'Edit permissions'
WHERE id = 2;
