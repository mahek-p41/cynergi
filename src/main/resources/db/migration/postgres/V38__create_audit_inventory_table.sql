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

