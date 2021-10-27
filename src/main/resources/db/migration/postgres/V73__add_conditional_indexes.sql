ALTER TABLE division
ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL;

CREATE INDEX division_deleted_idx ON division (deleted)
WHERE deleted = false;

ALTER TABLE region
ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE audit_permission
ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL;

CREATE INDEX audit_permission_deleted_idx ON audit_permission (deleted)
WHERE deleted = false;
