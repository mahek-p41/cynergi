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
    id           BIGSERIAL                                              NOT NULL PRIMARY KEY,
    uu_row_id    UUID        DEFAULT uuid_generate_v1()                 NOT NULL,
    time_created TIMESTAMPTZ DEFAULT clock_timestamp()                  NOT NULL,
    time_updated TIMESTAMPTZ DEFAULT clock_timestamp()                  NOT NULL,
    company_id   BIGINT REFERENCES company (id)                         NOT NULL,
    value        VARCHAR(10) CHECK (char_length(trim(value)) > 1)       NOT NULL,
    description  VARCHAR(50) CHECK (char_length(trim(description)) > 1) NOT NULL,
    UNIQUE (company_id, value)
);
CREATE TRIGGER update_vendor_group_trg
    BEFORE UPDATE
    ON vendor_group
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE vendor
(
    id                                  BIGSERIAL                                                    NOT NULL PRIMARY KEY,
    uu_row_id                           UUID                        DEFAULT uuid_generate_v1()       NOT NULL,
    time_created                        TIMESTAMPTZ                 DEFAULT clock_timestamp()        NOT NULL,
    time_updated                        TIMESTAMPTZ                 DEFAULT clock_timestamp()        NOT NULL,
    company_id                          BIGINT REFERENCES company (id)                               NOT NULL,
    number                              BIGINT CHECK ( number > 0 ) DEFAULT currval('vendor_id_seq') NOT NULL,
    name                                VARCHAR(30) CHECK (char_length(trim(name)) > 1)              NOT NULL,
    address_id                          BIGINT REFERENCES address (id)                               NOT NULL,
    our_account_number                  INTEGER                                                      NOT NULL,
    pay_to_id                           BIGINT REFERENCES vendor (id),
    freight_on_board_type_id            INTEGER REFERENCES freight_on_board_type_domain (id)         NOT NULL,
    payment_terms_id                    BIGINT REFERENCES vendor_payment_term (id)                   NOT NULL,
    float_days                          INTEGER,
    normal_days                         INTEGER,
    return_policy                       BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    ship_via_id                         BIGINT REFERENCES ship_via (id)                              NOT NULL,
    group_id                            BIGINT REFERENCES vendor_group (id),
    minimum_quantity                    INTEGER,
    minimum_amount                      NUMERIC(11, 2),
    free_ship_quantity                  INTEGER,
    free_ship_amount                    NUMERIC(11, 2),
    vendor_1099                         BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    federal_id_number                   VARCHAR(12),
    sales_representative_name           VARCHAR(20),
    sales_representative_fax            VARCHAR(20),
    separate_check                      BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    bump_percent                        NUMERIC(8, 7),
    freight_calc_method_type_id         INTEGER REFERENCES freight_calc_method_type_domain (id)      NOT NULL,
    freight_percent                     NUMERIC(8, 7),
    freight_amount                      NUMERIC(8, 2),
    charge_inventory_tax_1              BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    charge_inventory_tax_2              BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    charge_inventory_tax_3              BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    charge_inventory_tax_4              BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    federal_id_number_verification      BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    email_address                       VARCHAR(320),
    purchase_order_submit_email_address VARCHAR(320),
    allow_drop_ship_to_customer         BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    auto_submit_purchase_order          BOOLEAN                     DEFAULT FALSE                    NOT NULL,
    search_vector                       TSVECTOR                                                     NOT NULL,
    UNIQUE (company_id, number)
);
CREATE TRIGGER update_vendor_trg
    BEFORE UPDATE
    ON vendor
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

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
    ON vendor (group_id);

CREATE INDEX vendor_freight_calc_method_type_id_idx
    ON vendor (freight_calc_method_type_id);

CREATE INDEX vendor_name_trgm_idx
    ON vendor USING gist (name gist_trgm_ops);

CREATE INDEX vendor_vector_idx
    ON vendor USING gin(search_vector);
