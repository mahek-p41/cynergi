-- drop temp indexs if they exist
DROP INDEX IF EXISTS bk_audit_company_id_idx;
DROP INDEX IF EXISTS bk_audit_action_audit_id_idx;
DROP INDEX IF EXISTS bk_audit_action_status_id_idx;
DROP INDEX IF EXISTS bk_audit_detail_audit_id_idx;
DROP INDEX IF EXISTS bk_audit_detail_lookup_key_idx;
DROP INDEX IF EXISTS bk_audit_detail_scan_area_id_idx;
DROP INDEX IF EXISTS bk_audit_exception_audit_id_idx;
DROP INDEX IF EXISTS bk_audit_exception_note_audit_exception_id_idx;
DROP INDEX IF EXISTS bk_audit_inventory_audit_id;
DROP INDEX IF EXISTS bk_audit_inventory_count_idx;

-- create indexes that were not created during uuid migration or original table creation
--- create audit indexes
CREATE INDEX audit_company_id_idx ON audit (company_id);

-- create audit_detail indexes
CREATE INDEX audit_detail_audit_id_idx ON audit_detail (audit_id);
CREATE INDEX audit_detail_scan_area_id_idx ON audit_detail (scan_area_id);
CREATE INDEX audit_detail_scanned_by_idx ON audit_detail (scanned_by);
CREATE INDEX audit_detail_lookup_key_idx ON audit_detail (lookup_key);
CREATE INDEX audit_detail_exists_idx ON audit_detail (audit_id, lookup_key);

--- create audit_scan_area indexes
CREATE INDEX audit_scan_area_store_number_idx ON audit_scan_area (store_number_sfk);
CREATE INDEX audit_scan_area_company_id_idx ON audit_scan_area (company_id);

-- create audit_exception indexes
CREATE INDEX audit_exception_audit_id_idx ON audit_exception (audit_id);
CREATE INDEX audit_exception_exists_idx ON audit_exception (exception_code, audit_id, lookup_key);

-- create audit_exception_note indexes
CREATE INDEX audit_exception_note_audit_exception_id_idx ON audit_exception_note (audit_exception_id);
ALTER TABLE audit_exception_note ADD CONSTRAINT audit_exception_note_pkey PRIMARY KEY (id);

-- create audit_action indexes
CREATE INDEX audit_action_audit_id_idx ON audit_action (audit_id);
CREATE INDEX audit_action_status_id_idx ON audit_action (status_id);

-- create audit_inventory indexes
CREATE INDEX audit_inventory_audit_id_idx ON audit_inventory (audit_id);

-- create missed primary keys that were not created during uuid migration or original table creation
ALTER TABLE audit_detail ADD CONSTRAINT audit_detail_pkey PRIMARY KEY (id);
ALTER TABLE audit_detail DROP CONSTRAINT audit_detail_scan_area_id_2_fkey;
ALTER TABLE audit_detail ADD CONSTRAINT audit_detail_scan_area_id_fkey FOREIGN KEY (scan_area_id) REFERENCES audit_scan_area (id);
