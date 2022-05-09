ALTER TABLE vendor
ADD COLUMN deleted    BOOLEAN DEFAULT FALSE   NOT NULL,
DROP CONSTRAINT vendor_company_id_number_key;

CREATE UNIQUE INDEX vendor_unique_idx ON vendor USING btree (company_id, number, deleted)
WHERE deleted = false;
