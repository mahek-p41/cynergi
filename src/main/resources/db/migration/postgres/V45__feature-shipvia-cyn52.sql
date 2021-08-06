CREATE SEQUENCE division_number_seq START 10000;
ALTER TABLE division
   ALTER COLUMN number SET DATA TYPE BIGINT;
ALTER TABLE division
   ALTER COLUMN number SET DEFAULT nextval('division_number_seq');

CREATE SEQUENCE region_number_seq START 10000;
ALTER TABLE region
   ALTER COLUMN number SET DATA TYPE BIGINT;
ALTER TABLE region
   ALTER COLUMN number SET DEFAULT nextval('region_number_seq');

CREATE SEQUENCE ship_via_number_seq START 10000;
CREATE TABLE ship_via
(
    id           UUID        DEFAULT uuid_generate_v1()                  NOT NULL PRIMARY KEY,
    time_created TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    time_updated TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    number       BIGINT      DEFAULT NEXTVAL('ship_via_number_seq')      NOT NULL,
    description  VARCHAR(30) CHECK ( CHAR_LENGTH(TRIM(description)) > 1) NOT NULL,
    company_id   UUID REFERENCES company (id)                            NOT NULL,
    deleted      BOOLEAN     DEFAULT FALSE                               NOT NULL
);
CREATE TRIGGER update_shipvia_trg
    BEFORE UPDATE
    ON ship_via
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE UNIQUE INDEX ship_via_desc_id_idx ON ship_via USING btree (company_id, (UPPER(description)));
