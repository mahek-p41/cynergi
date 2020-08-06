CREATE TABLE audit_scan_area (
   id                      BIGSERIAL                                          NOT NULL PRIMARY KEY,
   uu_row_id               UUID        DEFAULT uuid_generate_v1()             NOT NULL,
   time_created            TIMESTAMPTZ DEFAULT clock_timestamp()              NOT NULL,
   time_updated            TIMESTAMPTZ DEFAULT clock_timestamp()              NOT NULL,
   name                    VARCHAR(50)                                        NOT NULL,
   store_number_sfk        BIGINT                                             NOT NULL,
   company_id              BIGINT REFERENCES company(id)                      NOT NULL
);

CREATE TRIGGER update_audit_scan_area_trg BEFORE
UPDATE ON audit_scan_area
FOR EACH ROW EXECUTE PROCEDURE last_updated_column_fn();

INSERT INTO audit_scan_area
   (name, store_number_sfk, company_id)
   SELECT type.description AS NAME,
          store.number     AS store_number_sfk,
          comp.id          AS company_id
   FROM   audit_scan_area_type_domain type
      CROSS JOIN company comp
      JOIN fastinfo_prod_import.store_vw store
         ON store.dataset = comp.dataset_code
   WHERE  store.number <= 999;

-- Migrate audit_detail table

ALTER TABLE audit_detail
ADD COLUMN scan_area_id_2 BIGINT REFERENCES audit_scan_area(id);

UPDATE audit_detail d
SET scan_area_id_2 = area.id
FROM
audit_scan_area area
   JOIN audit a ON area.company_id = a.company_id AND area.store_number_sfk = a.store_number
   JOIN audit_scan_area_type_domain t ON	area.name = t.description
WHERE d.audit_id = a.id AND t.id = d.scan_area_id;

--ALTER TABLE audit_detail
--DROP COLUMN scan_area_id;
--
--ALTER TABLE audit_detail
--RENAME COLUMN scan_area_id_2 TO scan_area_id;

-- Migrate audit_exception table

ALTER TABLE audit_exception
ADD COLUMN scan_area_id_2 BIGINT REFERENCES audit_scan_area(id);

UPDATE audit_exception d
SET scan_area_id_2 = area.id
FROM
audit_scan_area area
   JOIN audit a ON area.company_id = a.company_id AND area.store_number_sfk = a.store_number
   JOIN audit_scan_area_type_domain t ON	area.name = t.description
WHERE d.audit_id = a.id AND t.id = d.scan_area_id;

--ALTER TABLE audit_exception
--DROP COLUMN scan_area_id;
--
--ALTER TABLE audit_exception
--RENAME COLUMN scan_area_id_2 TO scan_area_id;
