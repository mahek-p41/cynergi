CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;

CREATE TABLE freight_on_board_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO freight_on_board_type_domain (id, value, description, localization_code)
VALUES (1, 'D', 'Destination', 'destination'),
       (2, 'S', 'Shipping', 'shipping');

CREATE TABLE freight_calc_method_type_domain
(
    id                INTEGER                                                        NOT NULL PRIMARY KEY,
    value             VARCHAR(10) CHECK ( char_length(trim(value)) > 0)              NOT NULL,
    description       VARCHAR(100) CHECK ( char_length(trim(description)) > 1)       NOT NULL,
    localization_code VARCHAR(100) CHECK ( char_length(trim(localization_code)) > 1) NOT NULL,
    UNIQUE (value)
);

INSERT INTO freight_calc_method_type_domain(id, value, description, localization_code)
VALUES (1, 'I', 'Item', 'item'),
       (2, 'N', 'None', 'none'),
       (3, 'P', 'Percent', 'percent'),
       (4, 'S', 'Size', 'size'),
       (5, 'W', 'Weight', 'weight');

CREATE TABLE vendor_group
(
    id           UUID        DEFAULT uuid_generate_v1()                 NOT NULL PRIMARY KEY,
    time_created TIMESTAMPTZ DEFAULT clock_timestamp()                  NOT NULL,
    time_updated TIMESTAMPTZ DEFAULT clock_timestamp()                  NOT NULL,
    company_id   UUID REFERENCES company (id)                           NOT NULL,
    value        VARCHAR(10) CHECK (char_length(trim(value)) > 1)       NOT NULL,
    description  VARCHAR(50) CHECK (char_length(trim(description)) > 1) NOT NULL,
    deleted      BOOLEAN     DEFAULT FALSE                              NOT NULL
);

CREATE UNIQUE INDEX vendor_group_unique_idx ON vendor_group USING btree (company_id, value, deleted)
WHERE deleted = false;

CREATE INDEX vendor_group_deleted_idx ON vendor_group(deleted)
WHERE deleted = false;

CREATE TRIGGER update_vendor_group_trg
    BEFORE UPDATE
    ON vendor_group
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE SEQUENCE vendor_number_seq START 10000;
CREATE TABLE inventory_end_of_month
(
    id                                  UUID        DEFAULT uuid_generate_v1()                  NOT NULL PRIMARY KEY,
    time_created                        TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    time_updated                        TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    company_id                          UUID REFERENCES company (id)                            NOT NULL,
    store
    year
    month
    serial_nbr
    cost
    book_value
    depreciation
    asset_account
    contra_asset_account
    model
    alt_id
    current_inv_nbr
    macrs_pfy_end_cost
    macrs_pfy_end_depr
    macrs_pfy_end_amt_depr
    macrs_pfy_end_date
    macrs_lfy_end_cost
    macrs_lfy_end_depr
    macrs_lfy_end_amt_depr
    macrs_pfy_bonus
    macrs_lfy_end_bonus
    UNIQUE (company_id, number)
);
CREATE TRIGGER update_vendor_trg
    BEFORE UPDATE
    ON vendor
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE OR REPLACE FUNCTION vendor_search_update_fn()
    RETURNS TRIGGER AS
$$
DECLARE
    vendNum CONSTANT TEXT := CAST(new.number AS TEXT);
    vendName CONSTANT TEXT := new.name;
BEGIN
    new.search_vector :=
        setweight(to_tsvector(vendNum), 'A') ||
        setweight(to_tsvector(vendName), 'B');

    RETURN new;
END;
$$
    LANGUAGE plpgsql STRICT;

CREATE TRIGGER vendor_search_update_trg
    BEFORE INSERT OR UPDATE
    ON vendor FOR EACH ROW EXECUTE PROCEDURE vendor_search_update_fn();

CREATE INDEX vendor_company_id_idx
    ON vendor (company_id);

CREATE INDEX vendor_address_id_idx
    ON vendor (address_id);

CREATE INDEX vendor_freight_on_board_type_id_idx
    ON vendor (freight_on_board_type_id);

CREATE INDEX vendor_ship_via_id_idx
    ON vendor (ship_via_id);

CREATE INDEX vendor_group_id_idx
    ON vendor (vendor_group_id);

CREATE INDEX vendor_freight_calc_method_type_id_idx
    ON vendor (freight_calc_method_type_id);

CREATE INDEX vendor_name_trgm_idx
    ON vendor USING gist (name gist_trgm_ops);

CREATE INDEX vendor_vector_idx
    ON vendor USING gin(search_vector);

