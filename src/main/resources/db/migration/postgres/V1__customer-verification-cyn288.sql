CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE FUNCTION last_updated_column_fn()
   RETURNS TRIGGER AS
$$
BEGIN
   new.time_updated := clock_timestamp();

   RETURN new;
END;
$$
   LANGUAGE plpgsql;

CREATE TABLE verification_auto (
   id                BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id         UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   address           BOOLEAN,
   comment           VARCHAR(100),
   dealer_phone      VARCHAR(18),
   diff_address      VARCHAR(50),
   diff_employee     VARCHAR(50),
   diff_phone        VARCHAR(18),
   dmv_verify        BOOLEAN,
   employer          BOOLEAN,
   last_payment      DATE,
   name              VARCHAR(50),
   next_payment      DATE,
   note              VARCHAR(50),
   payment_frequency VARCHAR(10),
   payment           NUMERIC(19, 2),
   pending_action    VARCHAR(50),
   phone             BOOLEAN,
   previous_loan     BOOLEAN,
   purchase_date     DATE,
   related           VARCHAR(50)
);
CREATE TRIGGER update_verification_auto_trg
   BEFORE UPDATE
   ON verification_auto
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE verification_employment (
   id            BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id     UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created  TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated  TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   department    VARCHAR(50),
   hire_date     DATE,
   leave_message BOOLEAN,
   name          VARCHAR(50),
   reliable      BOOLEAN,
   title         VARCHAR(50)
);
CREATE TRIGGER update_verification_employment_trg
   BEFORE UPDATE
   ON verification_employment
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE verification_landlord (
   id            BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id     UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created  TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated  TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   address       BOOLEAN,
   alt_phone     VARCHAR(18),
   lease_type    VARCHAR(25),
   leave_message BOOLEAN,
   length        INTEGER,
   name          VARCHAR(50),
   paid_rent     VARCHAR(15),
   phone         BOOLEAN,
   reliable      BOOLEAN,
   rent          NUMERIC(19, 2)
);
CREATE TRIGGER update_verification_landlord_trg
   BEFORE UPDATE
   ON verification_landlord
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE verification (
   id                BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id         UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   customer_account  VARCHAR(10)                            NOT NULL,
   customer_comments VARCHAR(255),
   verified_by       VARCHAR(50)                            NOT NULL, -- is a soft reference to an employee
   verified_time     TIMESTAMP   DEFAULT clock_timestamp()  NOT NULL,
   company           VARCHAR(6),                                      -- this is the pointer for the company but as the current implementation for most of cynergi is divided up into company's having their own dataset
   auto_id           BIGINT REFERENCES verification_auto(id),
   employment_id     BIGINT REFERENCES verification_employment(id),
   landlord_id       BIGINT REFERENCES verification_landlord(id)
);
CREATE TRIGGER update_verification_trg
   BEFORE UPDATE
   ON verification
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
ALTER TABLE verification
   ADD CONSTRAINT verification_customer_account_uq UNIQUE (customer_account);

CREATE TABLE verification_references (
   id             BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id      UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created   TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated   TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   address        BOOLEAN,
   has_home_phone BOOLEAN,
   known          INTEGER, -- years known?
   leave_message  BOOLEAN,
   rating         VARCHAR(3),
   relationship   BOOLEAN,
   reliable       BOOLEAN,
   time_frame     INTEGER,
   verify_phone   BOOLEAN,
   verification_id   BIGINT REFERENCES verification(id)        NOT NULL
);
CREATE TRIGGER update_verification_references_trg
   BEFORE UPDATE
   ON verification_references
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
