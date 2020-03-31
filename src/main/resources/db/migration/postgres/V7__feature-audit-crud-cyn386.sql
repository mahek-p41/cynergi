--- begin audit status domain table
CREATE TABLE audit_status_type_domain (
   id                INTEGER                                NOT NULL PRIMARY KEY,
   value             VARCHAR(15)                            NOT NULL,
   description       VARCHAR(50)                            NOT NULL,
   localization_code VARCHAR(50)                            NOT NULL
);
CREATE UNIQUE INDEX audit_status_type_domain_value ON audit_status_type_domain (value);

---- insert initial status values
INSERT INTO audit_status_type_domain(id, value, description, localization_code) VALUES (1, 'OPENED', 'Opened', 'audit.status.opened');  -- this is required for the server logic to function
INSERT INTO audit_status_type_domain(id, value, description, localization_code) VALUES (2, 'IN-PROGRESS', 'In Progress', 'audit.status.in-progress'); -- this is required for the server logic to function
INSERT INTO audit_status_type_domain(id, value, description, localization_code) VALUES (3, 'COMPLETED', 'Completed', 'audit.status.completed'); -- this is required for the server logic to function
INSERT INTO audit_status_type_domain(id, value, description, localization_code) VALUES (4, 'CANCELED', 'Canceled', 'audit.status.canceled'); -- this is required for the server logic to function
INSERT INTO audit_status_type_domain(id, value, description, localization_code) VALUES (5, 'SIGNED-OFF', 'Signed Off', 'audit.status.signed-off');
INSERT INTO audit_status_type_domain(id, value, description, localization_code) VALUES (6, 'CLOSED', 'Closed', 'audit.status.closed');

---- create transitions table
CREATE TABLE audit_status_transitions_type_domain (
   status_from INTEGER REFERENCES audit_status_type_domain (id)  NOT NULL,
   status_to   INTEGER REFERENCES audit_status_type_domain (id)  NOT NULL,
   UNIQUE (status_from, status_to)
);

---- insert initial transitions values
INSERT INTO audit_status_transitions_type_domain (status_from, status_to) VALUES (1, 2); -- OPENED -> IN-PROGRESS
INSERT INTO audit_status_transitions_type_domain (status_from, status_to) VALUES (1, 4); -- OPENED -> CANCELED
INSERT INTO audit_status_transitions_type_domain (status_from, status_to) VALUES (2, 3); -- IN-PROGRESS -> COMPLETED
INSERT INTO audit_status_transitions_type_domain (status_from, status_to) VALUES (2, 4); -- IN-PROGRESS -> CANCELED
INSERT INTO audit_status_transitions_type_domain (status_from, status_to) VALUES (3, 5); -- COMPLETED -> APPROVED
INSERT INTO audit_status_transitions_type_domain (status_from, status_to) VALUES (4, 5); -- CANCELED -> APPROVED
INSERT INTO audit_status_transitions_type_domain (status_from, status_to) VALUES (5, 6); -- APPROVED -> CLOSED
--- end audit status domain table

--- begin audit table
CREATE TABLE audit (
   id             BIGSERIAL                                            NOT NULL PRIMARY KEY,
   uu_row_id      UUID        DEFAULT uuid_generate_v1()               NOT NULL,
   time_created   TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
   time_updated   TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
   store_number   INTEGER     CHECK ( store_number > 0 )               NOT NULL -- soft reference to store that will need to be change to a foreign key once the store data has been pulled into Postgres
);
CREATE TRIGGER update_audit_trg
   BEFORE UPDATE
   ON audit
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
--- end audit table

--- being audit detail's scan area domain table
CREATE TABLE audit_scan_area_type_domain (
   id                INTEGER                                NOT NULL PRIMARY KEY,
   value             VARCHAR(15)                            NOT NULL,
   description       VARCHAR(50)                            NOT NULL,
   localization_code VARCHAR(50)                            NOT NULL
);
CREATE UNIQUE INDEX audit_scan_area_type_domain_value ON audit_scan_area_type_domain (value);

INSERT INTO audit_scan_area_type_domain(id, value, description, localization_code) VALUES (1, 'SHOWROOM', 'Showroom', 'audit.scan.area.showroom');
INSERT INTO audit_scan_area_type_domain(id, value, description, localization_code) VALUES (2, 'STOREROOM', 'Storeroom', 'audit.scan.area.storeroom');
INSERT INTO audit_scan_area_type_domain(id, value, description, localization_code) VALUES (3, 'WAREHOUSE', 'Warehouse', 'audit.scan.area.warehouse');
--- end audit detail's scan area domain table

--- begin audit detail table
CREATE TABLE audit_detail (
   id                  BIGSERIAL                                                NOT NULL PRIMARY KEY,
   uu_row_id           UUID         DEFAULT uuid_generate_v1()                  NOT NULL,
   time_created        TIMESTAMPTZ  DEFAULT clock_timestamp()                   NOT NULL,
   time_updated        TIMESTAMPTZ  DEFAULT clock_timestamp()                   NOT NULL,
   scan_area_id        INTEGER      REFERENCES audit_scan_area_type_domain (id) NOT NULL,
   bar_code            VARCHAR(200) CHECK ( char_length(bar_code) > 1 )         NOT NULL,
   inventory_id        VARCHAR(100) CHECK ( char_length(inventory_id) > 1 )     NOT NULL,
   inventory_brand     VARCHAR(100) CHECK ( char_length(inventory_brand) >1 )   NOT NULL,
   inventory_model     VARCHAR(100) CHECK ( char_length(inventory_model) > 1 )  NOT NULL,
   scanned_by          INTEGER      CHECK ( scanned_by > -1 )                   NOT NULL, -- soft reference to the employees defined by existing system as well as in the Employee table
   inventory_status    VARCHAR(100) CHECK ( char_length(inventory_status) > 0 ) NOT NULL,
   notes               VARCHAR(500),
   audit_id            BIGINT       REFERENCES audit (id)                       NOT NULL
);
CREATE TRIGGER update_audit_detail_trg
   BEFORE UPDATE
   ON audit_detail
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX audit_detail_audit_id_idx ON audit_detail (audit_id);
--- end audit detail table

CREATE TABLE audit_action (
   id              BIGSERIAL                                            NOT NULL PRIMARY KEY,
   uu_row_id       UUID        DEFAULT uuid_generate_v1()               NOT NULL,
   time_created    TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
   time_updated    TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
   changed_by      INTEGER     CHECK ( changed_by > -1 )                NOT NULL,
   status_id       INTEGER     REFERENCES audit_status_type_domain (id) NOT NULL,
   audit_id        BIGINT      REFERENCES audit (id)                    NOT NULL,
   UNIQUE (status_id, audit_id)
);
CREATE TRIGGER update_audit_action_trg
   BEFORE UPDATE
   ON audit_action
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX audit_action_audit_id_idx ON audit_action (audit_id);

CREATE TABLE audit_discrepancy (
   id              BIGSERIAL                                               NOT NULL PRIMARY KEY,
   uu_row_id       UUID         DEFAULT uuid_generate_v1()                 NOT NULL,
   time_created    TIMESTAMPTZ  DEFAULT clock_timestamp()                  NOT NULL,
   time_updated    TIMESTAMPTZ  DEFAULT clock_timestamp()                  NOT NULL,
   bar_code        VARCHAR(200) CHECK ( char_length(bar_code) > 1 )        NOT NULL,
   inventory_id    VARCHAR(100) CHECK ( char_length(inventory_id) > 1 )    NOT NULL,
   inventory_brand VARCHAR(100) CHECK ( char_length(inventory_brand) >1 )  NOT NULL,
   inventory_model VARCHAR(100) CHECK ( char_length(inventory_model) > 1 ) NOT NULL,
   scanned_by      INTEGER      CHECK ( scanned_by > -1 )                  NOT NULL,
   notes           VARCHAR(500) CHECK ( char_length(notes) > 2)            NOT NULL,
   audit_id        BIGINT       REFERENCES audit (id)                      NOT NULL
);
CREATE TRIGGER update_audit_discrepancy_trg
   BEFORE UPDATE
   ON audit_discrepancy
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
