DELETE FROM audit_permission
WHERE type_id IN (1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 25);
DELETE FROM audit_permission_type_domain
WHERE id IN (1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 25);

ALTER TABLE audit_permission
   DROP CONSTRAINT audit_permission_type_id_fkey;

UPDATE audit_permission_type_domain
SET id = 1,
    value = 'audit-approver',
    description = 'Approve audits',
    localization_code = 'audit.approve'
WHERE id = 6;

UPDATE audit_permission_type_domain
SET id = 2,
    value = 'audit-permission-manager',
    description = 'Audit permission manager',
    localization_code = 'audit.permission.manager'
WHERE id = 23;

UPDATE audit_permission
SET type_id = 1 WHERE type_id = 6;
UPDATE audit_permission
SET type_id = 2 WHERE type_id = 23;

ALTER TABLE audit_permission
    ADD CONSTRAINT audit_permission_type_id_fkey FOREIGN KEY (type_id) REFERENCES audit_permission_type_domain (id);
