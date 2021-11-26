ALTER TABLE company
ADD COLUMN address_id UUID REFERENCES address(id),
ADD COLUMN deleted    BOOLEAN DEFAULT FALSE   NOT NULL;

CREATE INDEX idx_company_address__id
ON company(address_id);

