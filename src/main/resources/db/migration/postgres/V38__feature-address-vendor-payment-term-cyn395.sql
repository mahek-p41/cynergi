CREATE TABLE address
(
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    number BIGINT DEFAULT currval('address_id_seq')                             NOT NULL,
    name character varying(30) CHECK (char_length(trim(name)) > 1)              NOT NULL,
    address1 character varying(30)CHECK (char_length(trim(address1)) > 1)       NOT NULL ,
    address2 character varying(30),
    city character varying(30)CHECK (char_length(trim(city)) > 1)               NOT NULL ,
    state character varying(2)CHECK (char_length(trim(state)) = 2)              NOT NULL,
    postal_code character varying(10)                                           NOT NULL,
    latitude numeric (14,11),
    longitude numeric (14,11),
    country varchar(50)CHECK (char_length(trim(city)) > 1)                      NOT NULL,
    county varchar(50),
    phone  varchar(21),
    fax  varchar(21)
   );

CREATE INDEX address_name_idx
ON address(name);

CREATE INDEX address_postal_code_idx
ON address(postal_code);

CREATE TRIGGER update_address_trg
   BEFORE UPDATE
   ON address
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE vendor_payment_term (
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    company_id BIGINT REFERENCES company(id)                                    NOT NULL,
    description varchar(30) CHECK ( char_length(trim(description)) > 1)         NOT NULL,
    number BIGINT DEFAULT currval('vendor_payment_term_id_seq')                 NOT NULL,
    number_of_payments integer DEFAULT 1                                        NOT NULL,
    discount_month  integer,
    discount_days  integer,
    discount_percent numeric(8,7),
    UNIQUE (company_id, number)
);

CREATE INDEX vendor_payment_term_company_id_idx
ON vendor_payment_term(company_id);

CREATE TRIGGER update_vendor_payment_term_trg
   BEFORE UPDATE
   ON vendor_payment_term
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE TABLE vendor_payment_term_schedule (
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    payment_term_id BIGINT REFERENCES vendor_payment_term (id)                  NOT NULL,
    due_month  integer CHECK ( due_month > 0 ),
    due_days  integer CHECK ( due_days > 0 )                                    NOT NULL,
    due_percent numeric(8,7) CHECK ( due_percent > 0 )                          NOT NULL,
    schedule_order_number integer  CHECK ( schedule_order_number > 0 )          NOT NULL,
    UNIQUE (payment_term_id, schedule_order_number)
 );

CREATE INDEX vendor_payment_schedule_payment_term_id_idx
ON vendor_payment_term_schedule(payment_term_id);


CREATE TRIGGER update_vendor_payment_term_schedule_trg
   BEFORE UPDATE
   ON vendor_payment_term_schedule
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


