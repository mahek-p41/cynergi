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
CREATE TABLE vendor
(
    id                                  UUID        DEFAULT uuid_generate_v1()                  NOT NULL PRIMARY KEY,
    time_created                        TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    time_updated                        TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    company_id                          UUID REFERENCES company (id)                            NOT NULL,
    number                              BIGINT DEFAULT nextval('vendor_number_seq'),
    name                                VARCHAR(30) CHECK (CHAR_LENGTH(TRIM(name)) > 1)         NOT NULL,
    address_id                          UUID REFERENCES address (id),
    account_number                      VARCHAR(20),
    pay_to_id                           UUID REFERENCES vendor (id),
    freight_on_board_type_id            INTEGER REFERENCES freight_on_board_type_domain (id)    NOT NULL,
    vendor_payment_term_id              UUID REFERENCES vendor_payment_term (id)                NOT NULL,
    normal_days                         INTEGER,
    return_policy                       BOOLEAN     DEFAULT FALSE                               NOT NULL,
    ship_via_id                         UUID REFERENCES ship_via (id)                           NOT NULL,
    vendor_group_id                     UUID REFERENCES vendor_group (id),
    minimum_quantity                    INTEGER,
    minimum_amount                      NUMERIC(11, 2),
    free_ship_quantity                  INTEGER,
    free_ship_amount                    NUMERIC(11, 2),
    vendor_1099                         BOOLEAN     DEFAULT FALSE                               NOT NULL,
    federal_id_number                   VARCHAR(12),
    sales_representative_name           VARCHAR(20),
    sales_representative_fax            VARCHAR(20),
    separate_check                      BOOLEAN     DEFAULT FALSE                               NOT NULL,
    bump_percent                        NUMERIC(8, 7),
    freight_calc_method_type_id         INTEGER REFERENCES freight_calc_method_type_domain (id) NOT NULL,
    freight_percent                     NUMERIC(8, 7),
    freight_amount                      NUMERIC(8, 2),
    charge_inventory_tax_1              BOOLEAN     DEFAULT FALSE                               NOT NULL,
    charge_inventory_tax_2              BOOLEAN     DEFAULT FALSE                               NOT NULL,
    charge_inventory_tax_3              BOOLEAN     DEFAULT FALSE                               NOT NULL,
    charge_inventory_tax_4              BOOLEAN     DEFAULT FALSE                               NOT NULL,
    federal_id_number_verification      BOOLEAN     DEFAULT FALSE                               NOT NULL,
    email_address                       VARCHAR(320),
    purchase_order_submit_email_address VARCHAR(320),
    allow_drop_ship_to_customer         BOOLEAN     DEFAULT FALSE                               NOT NULL,
    auto_submit_purchase_order          BOOLEAN     DEFAULT FALSE                               NOT NULL,
    search_vector                       TSVECTOR                                                NOT NULL,
    note                                TEXT,
    phone_number                        VARCHAR CHECK (phone_number NOT LIKE '%[^0-9+-.]%'),
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
