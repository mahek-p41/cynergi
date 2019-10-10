ALTER TABLE audit_status_type_domain
    ADD COLUMN color VARCHAR(6) CHECK ( char_length(color) = 6 ) UNIQUE;

CREATE FUNCTION audit_status_type_domain_color_upper_fn()
    RETURNS TRIGGER AS
$$
BEGIN
    new.color := UPPER(new.color);

    RETURN new;
END;
$$
    LANGUAGE plpgsql;
CREATE TRIGGER audit_status_type_domain_color_upper_trg
    BEFORE INSERT OR UPDATE
    ON audit_status_type_domain
    FOR EACH ROW
EXECUTE PROCEDURE audit_status_type_domain_color_upper_fn();

UPDATE audit_status_type_domain SET color = 'FF0000' WHERE id = 1;
UPDATE audit_status_type_domain SET color = 'FF6600' WHERE id = 2;
UPDATE audit_status_type_domain SET color = 'FFCC00' WHERE id = 3;
UPDATE audit_status_type_domain SET color = 'CCFF00' WHERE id = 4;
UPDATE audit_status_type_domain SET color = '66FF00' WHERE id = 5;

ALTER TABLE audit_status_type_domain
    ALTER COLUMN color SET NOT NULL;
