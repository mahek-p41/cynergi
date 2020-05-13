CREATE TABLE ship_via
(
    id           BIGSERIAL                                                       NOT NULL PRIMARY KEY,
    uu_row_id    UUID                         DEFAULT uuid_generate_v1()         NOT NULL,
    time_created TIMESTAMPTZ                  DEFAULT clock_timestamp()          NOT NULL,
    time_updated TIMESTAMPTZ                  DEFAULT clock_timestamp()          NOT NULL,
    number       BIGINT CHECK ( number > 0 ) DEFAULT currval('ship_via_id_seq')  NOT NULL,
    description  VARCHAR(30) CHECK ( char_length(trim(description)) > 1)         NOT NULL,
    company_id   BIGINT REFERENCES company (id)                                  NOT NULL
);
CREATE TRIGGER update_shipvia_trg
    BEFORE UPDATE
    ON ship_via
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE UNIQUE INDEX idx_ship_via_desc_id ON ship_via USING btree (company_id, (UPPER(description)));
