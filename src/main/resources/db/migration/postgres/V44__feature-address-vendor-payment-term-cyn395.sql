CREATE TABLE address
(
    id           UUID        DEFAULT uuid_generate_v1()              NOT NULL PRIMARY KEY,
    time_created TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()               NOT NULL,
    time_updated TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()               NOT NULL,
    number       BIGINT,
    name         VARCHAR(30) CHECK (CHAR_LENGTH(TRIM(name)) > 1)     NOT NULL,
    address1     VARCHAR(30) CHECK (CHAR_LENGTH(TRIM(address1)) > 1) NOT NULL,
    address2     VARCHAR(30),
    city         VARCHAR(30) CHECK (CHAR_LENGTH(TRIM(city)) > 1)     NOT NULL,
    state        VARCHAR(2) CHECK (CHAR_LENGTH(TRIM(state)) = 2)     NOT NULL,
    postal_code  VARCHAR(10)                                         NOT NULL,
    latitude     numeric(14, 11),
    longitude    numeric(14, 11),
    country      VARCHAR(50) CHECK (CHAR_LENGTH(TRIM(city)) > 1)     NOT NULL,
    county       VARCHAR(50),
    phone        VARCHAR(21),
    fax          VARCHAR(21)
);

CREATE INDEX address_name_idx ON address(name);
CREATE INDEX address_postal_code_idx ON address(postal_code);

CREATE TRIGGER update_address_trg
   BEFORE UPDATE
   ON address
   FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE TABLE vendor_payment_term
(
    id                 UUID        DEFAULT uuid_generate_v1()                  NOT NULL PRIMARY KEY,
    time_created       TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()                   NOT NULL,
    company_id         UUID REFERENCES company (id)                            NOT NULL,
    description        VARCHAR(30) CHECK ( CHAR_LENGTH(TRIM(description)) > 1) NOT NULL,
    number             BIGINT,
    number_of_payments INTEGER     DEFAULT 1                                   NOT NULL,
    discount_month     INTEGER,
    discount_days      INTEGER,
    discount_percent   NUMERIC(8, 7),
    UNIQUE (company_id, number)
);
CREATE INDEX vendor_payment_term_company_id_idx ON vendor_payment_term(company_id);
CREATE TRIGGER update_vendor_payment_term_trg
   BEFORE UPDATE
   ON vendor_payment_term
   FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


CREATE TABLE vendor_payment_term_schedule
(
    id                     UUID        DEFAULT uuid_generate_v1()      NOT NULL PRIMARY KEY,
    time_created           TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()       NOT NULL,
    time_updated           TIMESTAMPTZ DEFAULT CLOCK_TIMESTAMP()       NOT NULL,
    vendor_payment_term_id UUID REFERENCES vendor_payment_term (id)    NOT NULL,
    due_month              INTEGER CHECK ( due_month > 0 ),
    due_days               INTEGER CHECK ( due_days > 0 )              NOT NULL,
    due_percent            NUMERIC(8, 7) CHECK ( due_percent > 0 )     NOT NULL,
    schedule_order_number  INTEGER CHECK ( schedule_order_number > 0 ) NOT NULL,
    UNIQUE (vendor_payment_term_id, schedule_order_number)
);

CREATE INDEX vendor_payment_schedule_payment_term_id_idx ON vendor_payment_term_schedule(vendor_payment_term_id);

CREATE TRIGGER update_vendor_payment_term_schedule_trg
   BEFORE UPDATE
   ON vendor_payment_term_schedule
   FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();


