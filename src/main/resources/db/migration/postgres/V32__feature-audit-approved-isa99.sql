ALTER TABLE audit_exception
    RENAME COLUMN signed_off TO approved;

ALTER TABLE audit_exception
    RENAME COLUMN signed_off_by TO approved_by;

UPDATE audit_status_type_domain SET value = 'APPROVED' WHERE id = 5;
UPDATE audit_status_type_domain SET description = 'Approved' WHERE id = 5;
UPDATE audit_status_type_domain SET localization_code = 'audit.status.approved' WHERE id = 5;

UPDATE audit_permission_type_domain SET value = 'audit-updateApproved' WHERE id = 6;
UPDATE audit_permission_type_domain SET localization_code = 'audit.update.approved' WHERE id = 6;

UPDATE audit_permission_type_domain SET value = 'audit-updateApprovedAllExceptions' WHERE id = 7;
UPDATE audit_permission_type_domain SET localization_code = 'audit.update.approved.all.exceptions' WHERE id = 7;

UPDATE audit_permission_type_domain SET value = 'auditException-approved' WHERE id = 15;
UPDATE audit_permission_type_domain SET description = 'Allow user to approve an audit' WHERE id = 15;
UPDATE audit_permission_type_domain SET localization_code = 'audit.exception.approved' WHERE id = 15;
