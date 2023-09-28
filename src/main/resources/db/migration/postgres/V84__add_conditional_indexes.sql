ALTER TABLE division
ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL;

CREATE INDEX division_deleted_idx ON division (deleted)
WHERE deleted = false;

ALTER TABLE region
ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL;

CREATE INDEX region_deleted_idx ON region (deleted)
WHERE deleted = false;

ALTER TABLE audit_permission
ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL;

CREATE INDEX audit_permission_deleted_idx ON audit_permission (deleted)
WHERE deleted = false;

DROP INDEX company_dataset_code_idx;
CREATE UNIQUE INDEX company_dataset_code_idx ON company (dataset_code, deleted)
WHERE deleted = false;
