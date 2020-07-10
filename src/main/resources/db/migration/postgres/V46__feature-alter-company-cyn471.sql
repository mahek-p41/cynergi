ALTER TABLE company
ADD COLUMN address_id BIGINT REFERENCES address(id);

CREATE INDEX idx_company_address__id
ON company(address_id);

