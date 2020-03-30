CREATE TABLE address
(
    id                 BIGSERIAL                                                NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()                   NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                    NOT NULL,
    company_id BIGINT REFERENCES company(id)                                    NOT NULL,
    number integer CHECK (number > 0)                                           NOT NULL,
    name character varying(30) CHECK (char_length(trim(name)) > 1)              NOT NULL,
    address1 character varying(30)CHECK (char_length(trim(address1)) > 1)       NOT NULL ,
    address2 character varying(30),
    city character varying(20)CHECK (char_length(trim(city)) > 1)               NOT NULL ,
    state character varying(2)CHECK (char_length(trim(state)) = 2)              NOT NULL,
    postal_code character varying(10)                                           NOT NULL,
    latitude numeric (14,11),
    longitude numeric (14,11),
    country varchar(50)CHECK (char_length(trim(city)) > 1)                      NOT NULL,
    county varchar(50),
    UNIQUE (company_id, number)
);

CREATE INDEX idx_name
ON address(name);

CREATE INDEX idx_postal_code
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
    number integer CHECK (number > 0)                                           NOT NULL,
    number_of_payments integer DEFAULT 1                                        NOT NULL,
    due_month_1  integer,
    due_month_2  integer,
    due_month_3  integer,
    due_month_4  integer,
    due_month_5  integer,
    due_month_6  integer,
    due_days_1  integer,
    due_days_2  integer,
    due_days_3  integer,
    due_days_4  integer,
    due_days_5  integer,
    due_days_6  integer,
    due_percent_1 numeric(8,4),
    due_percent_2 numeric(8,4),
    due_percent_3 numeric(8,4),
    due_percent_4 numeric(8,4),
    due_percent_5 numeric(8,4),
    due_percent_6 numeric(8,4),
    discount_month  integer,
    discount_days  integer,
    discount_percent numeric(6,2),
    UNIQUE (company_id, number)
);
CREATE TRIGGER update_vendor_payment_term_trg
   BEFORE UPDATE
   ON vendor_payment_term
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
