CREATE TABLE address
(
    id           BIGSERIAL                                           NOT NULL PRIMARY KEY,
    uu_row_id    UUID        DEFAULT uuid_generate_v1()              NOT NULL,
    time_created TIMESTAMPTZ DEFAULT clock_timestamp()               NOT NULL,
    time_updated TIMESTAMPTZ DEFAULT clock_timestamp()               NOT NULL,
    company_id   BIGINT REFERENCES company (id)                      NOT NULL,
    number       INTEGER CHECK (number > 0)                          NOT NULL,
    name         VARCHAR(30) CHECK (char_length(trim(name)) > 1)     NOT NULL,
    address1     VARCHAR(30) CHECK (char_length(trim(address1)) > 1) NOT NULL,
    address2     VARCHAR(30),
    city         VARCHAR(20) CHECK (char_length(trim(city)) > 1)     NOT NULL,
    state        VARCHAR(2) CHECK (char_length(trim(state)) = 2)     NOT NULL,
    postal_code  VARCHAR(10)                                         NOT NULL,
    latitude     NUMERIC(14, 11),
    longitude    NUMERIC(14, 11),
    country      VARCHAR(50) CHECK (char_length(trim(city)) > 1)     NOT NULL,
    county       VARCHAR(50),
    UNIQUE (company_id, number)
);
CREATE INDEX idx_address_name ON address (name);
CREATE INDEX idx_address_postal_code ON address (postal_code);
CREATE INDEX idx_address_company_id ON address (company_id);
CREATE TRIGGER update_address_trg
    BEFORE UPDATE
    ON address
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE vendor_term
(
    id                 BIGSERIAL                                               NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                  NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                   NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                   NOT NULL,
    company_id         BIGINT REFERENCES company (id)                          NOT NULL,
    description        VARCHAR(30) CHECK ( char_length(trim(description)) > 1) NOT NULL,
    number             INTEGER CHECK (number > 0)                              NOT NULL,
    number_of_payments INTEGER     DEFAULT 1                                   NOT NULL,
    due_month_1        INTEGER,
    due_month_2        INTEGER,
    due_month_3        INTEGER,
    due_month_4        INTEGER,
    due_month_5        INTEGER,
    due_month_6        INTEGER,
    due_days_1         INTEGER,
    due_days_2         INTEGER,
    due_days_3         INTEGER,
    due_days_4         INTEGER,
    due_days_5         INTEGER,
    due_days_6         INTEGER,
    due_percent_1      NUMERIC(8, 4),
    due_percent_2      NUMERIC(8, 4),
    due_percent_3      NUMERIC(8, 4),
    due_percent_4      NUMERIC(8, 4),
    due_percent_5      NUMERIC(8, 4),
    due_percent_6      NUMERIC(8, 4),
    discount_month     INTEGER,
    discount_days      INTEGER,
    discount_percent   NUMERIC(6, 2),
    UNIQUE (company_id, number)
);
CREATE INDEX idx_vendor_term_company_id ON vendor_term(company_id);
CREATE TRIGGER update_vendor_term_trg
    BEFORE UPDATE
    ON vendor_term
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE vendor
(
    id                             BIGSERIAL                                                     NOT NULL PRIMARY KEY,
    uu_row_id                      UUID                               DEFAULT uuid_generate_v1() NOT NULL,
    time_created                   TIMESTAMPTZ                        DEFAULT clock_timestamp()  NOT NULL,
    time_updated                   TIMESTAMPTZ                        DEFAULT clock_timestamp()  NOT NULL,
    company_id                     BIGINT REFERENCES company (id)                                NOT NULL,
    number                         INTEGER CHECK ( number > 0 )                                  NOT NULL,
    name_key                       VARCHAR(30) CHECK (char_length(trim(name_key)) > 1)           NOT NULL,
    address_id                     BIGINT REFERENCES address (id)                                NOT NULL,
    our_account_number             INTEGER                            DEFAULT 0                  NOT NULL,
    pay_to                         INTEGER                            DEFAULT 0                  NOT NULL,
    buyer                          VARCHAR(8),
    freight_on_board               VARCHAR(1),
    payment_terms                  BIGINT REFERENCES vendor_term (id) DEFAULT 1                  NOT NULL,
    float_days                     INTEGER                            DEFAULT 0                  NOT NULL,
    normal_days                    INTEGER                            DEFAULT 0                  NOT NULL,
    return_policy                  VARCHAR(1),
    next_account_payable           INTEGER                            DEFAULT 0                  NOT NULL,
    ship_via                       INTEGER REFERENCES ship_via (id)   DEFAULT 1                  NOT NULL,
    vend_group                     VARCHAR(8),
    shutdown_from                  date,
    shutdown_thru                  date,
    minimum_quantity               INTEGER,
    minimum_amount                 NUMERIC(11, 2),
    free_ship_quantity             INTEGER,
    free_ship_amount               NUMERIC(11, 2),
    vendor_1099                    BOOLEAN                            DEFAULT FALSE              NOT NULL,
    federal_id_number              VARCHAR(12),
    year_to_date_purchases         NUMERIC(14, 2),
    last_year_purchases            NUMERIC(14, 2),
    balance                        NUMERIC(14, 2),
    year_to_date_discounts         NUMERIC(14, 2),
    last_payment                   date,
    sales_rep_name                 VARCHAR(20),
    sales_rep_fax                  VARCHAR,
    separate_check                 BOOLEAN                            DEFAULT FALSE              NOT NULL,
    bump_percent                   NUMERIC(9, 4),
    freight_calc_method            VARCHAR(1),
    freight_percent                NUMERIC(8, 3),
    freight_amount                 NUMERIC(8, 2),
    rebate_code_1                  INTEGER,
    rebate_code_2                  INTEGER,
    rebate_code_3                  INTEGER,
    rebate_code_4                  INTEGER,
    rebate_code_5                  INTEGER,
    charge_inv_tax_1               BOOLEAN                            DEFAULT FALSE              NOT NULL,
    charge_inv_tax_2               BOOLEAN                            DEFAULT FALSE              NOT NULL,
    charge_inv_tax_3               BOOLEAN                            DEFAULT FALSE              NOT NULL,
    charge_inv_tax_4               BOOLEAN                            DEFAULT FALSE              NOT NULL,
    federal_id_number_verification BOOLEAN                            DEFAULT FALSE              NOT NULL,
    UNIQUE (company_id, number)
);
CREATE INDEX idx_vendor_name_key ON vendor (name_key);
CREATE INDEX idx_vendor_our_account_number ON vendor (our_account_number);
CREATE INDEX idx_vendor_company_id ON vendor (company_id);
CREATE TRIGGER update_vendor_trg
    BEFORE UPDATE
    ON vendor
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

