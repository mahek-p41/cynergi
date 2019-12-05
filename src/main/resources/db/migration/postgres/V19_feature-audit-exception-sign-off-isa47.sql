ALTER TABLE audit_exception
    ADD COLUMN signed_off_by INTEGER CHECK ( signed_off_by > -1 );
