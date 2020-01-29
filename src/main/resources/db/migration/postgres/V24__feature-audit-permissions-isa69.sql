CREATE TABLE audit_permission_type_domain
(
    id                INTEGER CHECK ( id > 0 )                                  NOT NULL PRIMARY KEY,
    value             VARCHAR(50) CHECK ( length(trim(value)) > 0 )             NOT NULL,
    description       VARCHAR(50) CHECK ( length(trim(description)) > 0 )       NOT NULL,
    localization_code VARCHAR(50) CHECK ( length(trim(localization_code)) > 0 ) NOT NULL,
    UNIQUE (value)
);
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (1, 'audit-fetchOne', 'Find audit by ID', 'audit.fetch.one');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (2, 'audit-fetchAll', 'List audits', 'audit.fetch.all');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (3, 'audit-fetchAllStatusCounts', 'List stats for audits', 'audit.fetch.all.status.counts');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (4, 'audit-create', 'Create an audit', 'audit.create');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (5, 'audit-CompleteOrCancel', 'Complete or Cancel an audit', 'audit.complete.or.cancel');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (6, 'audit-updateSignOff', 'Update an audit''s status', 'audit.update.sign.off');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (7, 'audit-updateSignOffAllExceptions', 'Update an audit''s status', 'audit.update.sign.off.all.exceptions');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (8, 'auditDetail-fetchOne', 'Find an audit inventory item by ID', 'audit.detail.fetch.one');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (9, 'auditDetail-fetchAll', 'List audit inventory items', 'audit.detail.fetch.all');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (10, 'auditDetail-save', 'Create a found inventory item', 'audit.detail.save');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (11, 'auditException-fetchOne', 'Find an audit exception by ID', 'audit.exception.fetch.one');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (12, 'auditException-fetchAll', 'List audit exceptions', 'audit.exception.fetch.all');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (13, 'auditException-create', 'Create an audit exception', 'audit.exception.create');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (14, 'auditException-update', 'Update an audit exception note or status', 'audit.exception.create');
INSERT INTO audit_permission_type_domain(id, value, description, localization_code)VALUES (15, 'auditException-signOff', 'Allow user to sign-off on an audit', 'audit.exception.sign.off');

CREATE TABLE audit_permission
(
    id                  BIGSERIAL                                            NOT NULL PRIMARY KEY,
    uu_row_id           UUID        DEFAULT uuid_generate_v1()               NOT NULL,
    time_created        TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    time_updated        TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    department          VARCHAR(3) CHECK ( length(trim(department)) > 1 )    NOT NULL,
    audit_permission_id INTEGER REFERENCES audit_permission_type_domain (id) NOT NULL,
    company_id          BIGINT REFERENCES company (id)                       NOT NULL,
    UNIQUE (department, audit_permission_id)
);
CREATE TRIGGER update_audit_permission_trg
    BEFORE UPDATE
    ON audit_permission
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
