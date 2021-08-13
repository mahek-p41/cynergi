CREATE INDEX division_deleted_idx ON division (deleted)
WHERE deleted = false;

CREATE INDEX audit_permission_deleted_idx ON audit_permission (deleted)
WHERE deleted = false;
