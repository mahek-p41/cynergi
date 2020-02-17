ALTER TABLE audit
    ADD COLUMN inventory_count INTEGER CHECK ( inventory_count > -1 ) NOT NULL;
