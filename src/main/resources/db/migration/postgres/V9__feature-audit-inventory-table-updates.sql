ALTER TABLE audit_detail
    DROP COLUMN inventory_status,
    DROP COLUMN notes;
ALTER TABLE audit_detail
    RENAME COLUMN inventory_id TO serial_number;
ALTER TABLE audit_detail
    RENAME COLUMN bar_code TO barcode;
ALTER TABLE audit_detail
    ADD COLUMN product_code VARCHAR(100) CHECK ( char_length(product_code) > 1 ),
    ADD COLUMN alt_id VARCHAR(100) CHECK ( char_length(alt_id) > 1 );
ALTER TABLE audit_detail
    ALTER COLUMN inventory_brand DROP NOT NULL;
UPDATE audit_detail
SET
    product_code = 'UNKNOWN',
    alt_id = barcode
;
ALTER TABLE audit_detail
    ALTER COLUMN product_code SET NOT NULL;
ALTER TABLE audit_detail
    ALTER COLUMN alt_id SET NOT NULL;

CREATE INDEX audit_detail_scan_area_id_idx ON audit_detail (scan_area_id);

ALTER TABLE audit_discrepancy
    RENAME TO audit_exception;
ALTER TABLE audit_exception
    RENAME COLUMN inventory_id TO serial_number;
ALTER TABLE audit_exception
    RENAME COLUMN notes TO exception_code;
ALTER TABLE audit_exception
    RENAME COLUMN bar_code TO barcode;
ALTER TABLE audit_exception
    ALTER COLUMN serial_number DROP NOT NULL,
    ALTER COLUMN inventory_brand DROP NOT NULL,
    ALTER COLUMN inventory_model DROP NOT NULL;
ALTER TABLE audit_exception
    ADD COLUMN scan_area_id INTEGER REFERENCES audit_scan_area_type_domain (id),
    ADD COLUMN product_code VARCHAR(100) CHECK ( char_length(product_code) > 1 ),
    ADD COLUMN alt_id VARCHAR(100) CHECK ( char_length(alt_id) > 1 ),
    ADD COLUMN signed_off BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE audit_exception
    RENAME CONSTRAINT audit_discrepancy_bar_code_check TO audit_exception_bar_code_check;
ALTER TABLE audit_exception
    RENAME CONSTRAINT audit_discrepancy_inventory_brand_check TO audit_exception_inventory_brand_check;
ALTER TABLE audit_exception
    RENAME CONSTRAINT audit_discrepancy_inventory_id_check TO audit_exception_inventory_id_check;
ALTER TABLE audit_exception
    RENAME CONSTRAINT audit_discrepancy_inventory_model_check TO audit_exception_inventory_model_check;
ALTER TABLE audit_exception
    RENAME CONSTRAINT audit_discrepancy_notes_check TO audit_exception_notes_check;
ALTER TABLE audit_exception
    RENAME CONSTRAINT audit_discrepancy_scanned_by_check TO audit_exception_scanned_by_check;
ALTER TABLE audit_exception
    RENAME CONSTRAINT audit_discrepancy_audit_id_fkey to audit_exception_audit_id_fkey;
ALTER TRIGGER update_audit_discrepancy_trg ON audit_exception RENAME TO update_audit_exception_trg;
CREATE INDEX audit_exception_audit_id_idx ON audit_exception (audit_id);
ALTER INDEX audit_discrepancy_pkey RENAME TO audit_exception_pkey;

CREATE TABLE inventory_location_type_domain (
    id                INTEGER                                                  NOT NULL PRIMARY KEY,
    value             VARCHAR(15) CHECK ( char_length(value) > 1 )             NOT NULL,
    description       VARCHAR(50) CHECK ( char_length(description) > 3 )       NOT NULL,
    localization_code VARCHAR(50) CHECK ( char_length(localization_code) > 3 ) NOT NULL
);
CREATE UNIQUE INDEX inventory_location_type_domain_value ON inventory_location_type_domain (value);

INSERT INTO inventory_location_type_domain(id, value, description, localization_code) VALUES (1, 'STORE', 'Store', 'inventory.value.location');
INSERT INTO inventory_location_type_domain(id, value, description, localization_code) VALUES (2, 'WAREHOUSE', 'Warehouse', 'inventory.value.warehouse');
INSERT INTO inventory_location_type_domain(id, value, description, localization_code) VALUES (3, 'PENDING', 'Pending', 'inventory.value.pending');
INSERT INTO inventory_location_type_domain(id, value, description, localization_code) VALUES (4, 'CUSTOMER', 'Customer', 'inventory.value.customer');
INSERT INTO inventory_location_type_domain(id, value, description, localization_code) VALUES (5, 'LOANER', 'Loaner', 'inventory.value.loaner');
INSERT INTO inventory_location_type_domain(id, value, description, localization_code) VALUES (6, 'SERVICE', 'Service', 'inventory.value.service');
INSERT INTO inventory_location_type_domain(id, value, description, localization_code) VALUES (7, 'STOLEN', 'Stolen', 'inventory.value.stolen');
INSERT INTO inventory_location_type_domain(id, value, description, localization_code) VALUES (8, 'CHARGEOFF', 'Chargeoff', 'inventory.value.chargeoff');

CREATE TABLE audit_exception_note (
    id                 BIGSERIAL                                    NOT NULL PRIMARY KEY,
    uu_row_id          UUID         DEFAULT uuid_generate_v1()      NOT NULL,
    time_created       TIMESTAMPTZ  DEFAULT clock_timestamp()       NOT NULL,
    time_updated       TIMESTAMPTZ  DEFAULT clock_timestamp()       NOT NULL,
    note               VARCHAR(200) CHECK ( char_length(note) > 3 ) NOT NULL,
    entered_by         INTEGER      CHECK ( entered_by > -1 )       NOT NULL,
    audit_exception_id BIGINT       REFERENCES audit_exception(id)  NOT NULL
);
CREATE INDEX audit_exception_note_audit_exception_id_idx ON audit_exception_note (audit_exception_id);
CREATE TRIGGER update_audit_exception_note_trg
    BEFORE UPDATE
    ON audit_exception_note
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
