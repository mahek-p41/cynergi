-- Begin temp audit table setup
CREATE TEMPORARY TABLE temp_audit(
   id BIGINT NOT NULL,
   store_number INTEGER NOT NULL,
   number BIGINT NOT NULL,

   UNIQUE (store_number, number)
);

CREATE OR REPLACE FUNCTION audit_store_number_increment()
   RETURNS TRIGGER AS
$$
DECLARE
   storeNum constant int := new.store_number;
   maxAuditNumber int;
BEGIN
   PERFORM pg_advisory_xact_lock(storeNum);

   maxAuditNumber := (SELECT COALESCE(MAX(number), 0) + 1 FROM temp_audit WHERE store_number = storeNum);

   new.number := maxAuditNumber;

   RETURN new;
END;
$$
   LANGUAGE plpgsql STRICT;

CREATE TRIGGER audit_store_number_auto
   BEFORE INSERT
   ON temp_audit
   FOR EACH ROW
EXECUTE PROCEDURE audit_store_number_increment();
-- End temp audit table setup

-- Begin Insert data into temp table to generate number
INSERT INTO temp_audit(id, store_number)
SELECT id, store_number from audit ORDER BY id, store_number;
-- End Insert data into temp table to generate number

-- Begin setup of audit table
ALTER TABLE audit
   ADD COLUMN number BIGINT; -- TODO not null
ALTER TABLE audit
   ADD CONSTRAINT audit_number_store_number_uq UNIQUE (store_number, number);
-- End setup of audit table

-- Begin audit table number update
UPDATE audit a
SET number = ta.number
FROM temp_audit ta
WHERE a.id = ta.id;

ALTER TABLE audit
   ALTER COLUMN number SET NOT NULL;
-- End audit table number update

-- Drop temp_audit
DROP TABLE temp_audit;

-- Add trigger to audit
CREATE OR REPLACE FUNCTION audit_store_number_increment()
   RETURNS TRIGGER AS
$$
DECLARE
   storeNum constant int := new.store_number;
   maxAuditNumber int;
BEGIN
   PERFORM pg_advisory_xact_lock(storeNum);

   maxAuditNumber := (SELECT COALESCE(MAX(number), 0) + 1 FROM audit WHERE store_number = storeNum);

   new.number := maxAuditNumber;

   RETURN new;
END;
$$
   LANGUAGE plpgsql STRICT;

CREATE TRIGGER audit_store_number_auto
   BEFORE INSERT
   ON audit
   FOR EACH ROW
EXECUTE PROCEDURE audit_store_number_increment();
