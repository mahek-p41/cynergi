ALTER TABLE company
ADD COLUMN address_id UUID REFERENCES address(id),
ADD COLUMN deleted    BOOLEAN DEFAULT FALSE   NOT NULL,
DROP CONSTRAINT company_client_id_key;

CREATE INDEX idx_company_address__id
ON company(address_id);

CREATE UNIQUE INDEX company_client_id_idx ON company USING btree (client_id, deleted)
WHERE deleted = false;
