CREATE TABLE audit_inventory (
   id                      BIGSERIAL                                          NOT NULL PRIMARY KEY,
   uu_row_id               UUID        DEFAULT uuid_generate_v1()             NOT NULL,
   time_created            TIMESTAMPTZ DEFAULT clock_timestamp()              NOT NULL,
   time_updated            TIMESTAMPTZ DEFAULT clock_timestamp()              NOT NULL,
   audit_id                BIGINT       REFERENCES audit (id)                 NOT NULL,
   dataset                 VARCHAR,
   serial_number           VARCHAR,
   lookup_key              VARCHAR,
   lookup_key_type         TEXT,
   barcode                 VARCHAR,
   alt_id                  VARCHAR,
   brand                   VARCHAR,
   model_number            VARCHAR,
   product_code            VARCHAR,
   description             VARCHAR,
   received_date           DATE,
   original_cost           NUMERIC,
   actual_cost             NUMERIC,
   model_category          VARCHAR,
   times_rented            INTEGER,
   total_revenue           NUMERIC,
   remaining_value         NUMERIC,
   sell_price              NUMERIC,
   assigned_value          NUMERIC,
   idle_days               INTEGER,
   condition               VARCHAR,
   returned_date           DATE,
   location                INTEGER,
   status                  VARCHAR,
   primary_location        INTEGER,
   location_type           INTEGER
);

CREATE TRIGGER update_audit_inventory_trg BEFORE
UPDATE ON audit_inventory
FOR EACH ROW EXECUTE PROCEDURE last_updated_column_fn();

COMMENT ON TABLE audit_inventory IS 'The financial calendar stores the inventory snapshot for an COMPLETED | CANCELED audit.';

ALTER TABLE audit
DROP COLUMN inventory_count;


-- Migration script to create inventory snapshot for completed, approved, canceled audits
-- Approved audits also have completed status so the dataset in select clause will be duplicate after joining
--    Option 1: filter by statuses: (completed, approved, canceled), use distinct to eliminate the duplicate data
--    Option 2(chosen one): filter by statuses: (completed, canceled)
-- Inventory snapshot creates snapshot for all inventory items, not only items in statuses ('N', 'R')
-- because we don't know when do we need those items
INSERT INTO audit_inventory
   (audit_id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alt_id, brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type)
SELECT a.id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alt_id, brand, model_number, product_code, i.description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type
FROM fastinfo_prod_import.inventory_vw i
   JOIN company comp ON i.dataset = comp.dataset_code
   JOIN audit a ON a.company_id = comp.id
   JOIN audit_action action ON a.id = action.audit_id
   JOIN audit_status_type_domain status ON action.status_id = status.id
WHERE status.value IN ('COMPLETED', 'CANCELED')
ORDER BY a.id;
