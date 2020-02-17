CREATE TABLE company
(
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    name               VARCHAR(150) CHECK ( char_length(trim(name)) > 1 )       NOT NULL,
    doing_business_as  VARCHAR(100) CHECK ( char_length(trim(doing_business_as)) > 1 ),
    client_code        VARCHAR(6) CHECK ( char_length(trim(client_code)) > 1 )  NOT NULL,
    client_id          INTEGER    CHECK ( client_id > 0 )                       NOT NULL,
    dataset_code       VARCHAR(6) CHECK ( char_length(trim(dataset_code)) = 6 ) NOT NULL,
    federal_tax_number VARCHAR(12),
    UNIQUE (client_id)
);
CREATE TRIGGER update_company_trg
    BEFORE UPDATE
    ON company
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
