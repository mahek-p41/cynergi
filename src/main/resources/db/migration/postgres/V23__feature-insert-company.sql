CREATE TABLE company (
    id                BIGSERIAL                                          NOT NULL PRIMARY KEY,
    UUID              UUID        DEFAULT uuid_generate_v1()             NOT NULL,
    date_created      TIMESTAMPTZ DEFAULT clock_timestamp()              NOT NULL,
    last_updated      TIMESTAMPTZ DEFAULT clock_timestamp()              NOT NULL,
    name              VARCHAR(150) CHECK ( char_length(name) > 1 )       NOT NULL,
    doing_business_as VARCHAR(100) CHECK ( char_length(doing_business_as) > 1 ),
    client_code       VARCHAR(6) CHECK ( char_length(client_code) > 1 )  NOT NULL,
    client_id         INTEGER CHECK ( client_id > 0 )                    NOT NULL,
    dataset_code      VARCHAR(6) CHECK ( char_length(dataset_code) = 6 ) NOT NULL,
    fin               VARCHAR(12)
);

CREATE UNIQUE INDEX company_client_id_index ON company (client_id);

CREATE TRIGGER update_company_trg
    BEFORE UPDATE
    ON company
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
