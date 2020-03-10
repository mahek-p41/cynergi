DELETE FROM audit_permission WHERE type_id = 24;  -- delete any previously defined update permissions

UPDATE audit_permission
SET type_id = 24
WHERE type_id = 25;

DELETE from audit_permission_type_domain WHERE id = 25; -- finally delete what was the delete permission
UPDATE audit_permission_type_domain -- make update permission look like delete permission
SET value = 'auditPermission-delete',
    description = 'Allow user to delete an audit permission',
    localization_code = 'audit.permission.delete'
WHERE id = 24;
