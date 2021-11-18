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
    fax          VARCHAR(21),
    deleted      BOOLEAN      DEFAULT FALSE                          NOT NULL
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
    deleted            BOOLEAN     DEFAULT FALSE                               NOT NULL,
    UNIQUE (company_id, number)
);
CREATE INDEX vendor_payment_term_company_id_idx ON vendor_payment_term(company_id) WHERE deleted is FALSE;
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
    deleted                BOOLEAN     DEFAULT FALSE                   NOT NULL,
    UNIQUE (vendor_payment_term_id, schedule_order_number, deleted)
);

CREATE INDEX vendor_payment_schedule_payment_term_id_idx ON vendor_payment_term_schedule(vendor_payment_term_id);

CREATE TRIGGER update_vendor_payment_term_schedule_trg
   BEFORE UPDATE
   ON vendor_payment_term_schedule
   FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

-- Queries that have complicated where clause need to create a trigger to call this check for the table.
-- Queries that not delete row by primary key (id) or foreign key considered complicated queries.
-- This trigger invoked before the soft delete of each row.

CREATE OR REPLACE FUNCTION soft_delete_check_fn()
    RETURNS TRIGGER AS
$$
DECLARE
   referencedRows RECORD;
   fkColumn VARCHAR;
   pkColumn VARCHAR;
   fkTable VARCHAR;
   pkTable VARCHAR;
   hasReferences BOOLEAN;
   hasReferences2 BOOLEAN;
BEGIN
   RAISE NOTICE 'TG_TABLE_NAME %', TG_TABLE_NAME;
    IF new.id <> old.id THEN
        RAISE EXCEPTION 'cannot update id once it has been created';
    ELSEIF new.deleted = TRUE THEN
         FOR referencedRows IN
              SELECT conrelid::regclass AS "FK_Table"
                 ,CASE WHEN pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY %' THEN substring(pg_get_constraintdef(c.oid), 14, position(')' in pg_get_constraintdef(c.oid))-14) END AS "FK_Column"
                 ,CASE WHEN pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY %' THEN substring(pg_get_constraintdef(c.oid), position(' REFERENCES ' in pg_get_constraintdef(c.oid))+12, position('(' in substring(pg_get_constraintdef(c.oid), 14))-position(' REFERENCES ' in pg_get_constraintdef(c.oid))+1) END AS "PK_Table"
                 ,CASE WHEN pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY %' THEN substring(pg_get_constraintdef(c.oid), position('(' in substring(pg_get_constraintdef(c.oid), 14))+14, position(')' in substring(pg_get_constraintdef(c.oid), position('(' in substring(pg_get_constraintdef(c.oid), 14))+14))-1) END AS "PK_Column"
              FROM   pg_constraint c
              JOIN   pg_namespace n ON n.oid = c.connamespace
              WHERE  contype IN ('f', 'p ')
              AND pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY %'
              AND substring(pg_get_constraintdef(c.oid), position(' REFERENCES ' in pg_get_constraintdef(c.oid))+12, position('(' in substring(pg_get_constraintdef(c.oid), 14))-position(' REFERENCES ' in pg_get_constraintdef(c.oid))+1) = TG_TABLE_NAME
              ORDER  BY pg_get_constraintdef(c.oid), conrelid::regclass::text, contype DESC
         LOOP
            fkColumn := referencedRows."FK_Column";
            pkColumn := referencedRows."PK_Column";
            fkTable := referencedRows."FK_Table";
            pkTable := referencedRows."PK_Table";
            RAISE NOTICE 'Checking % in referenced column: %.%', new.id, fkTable, fkColumn;
            RAISE NOTICE 'Checking pkColumn=% pkTable=%', pkColumn, pkTable;

            IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                       WHERE TABLE_NAME = fkTable AND COLUMN_NAME = 'deleted') THEN
               EXECUTE 'SELECT (EXISTS(SELECT * FROM ' || fkTable || ' WHERE ' || fkColumn || ' = ' || quote_literal(new.id) || ' AND deleted = false))::boolean'
                 INTO hasReferences;
               IF (hasReferences) THEN
                  RAISE EXCEPTION 'Still referenced';
               END IF;
            ELSE
               EXECUTE 'SELECT (EXISTS(SELECT * FROM ' || fkTable || ' WHERE ' || fkColumn || ' = ' || quote_literal(new.id) || '))::boolean'
                  INTO hasReferences2;
               IF (hasReferences2) THEN
                  RAISE EXCEPTION 'Still referenced';
               END IF;
            END IF;

         END LOOP;
         RAISE NOTICE 'References tables:%', referencedRows;
    ELSE
    		RAISE NOTICE 'Test my prod % %', new.id, new.deleted;-- Remove this else after finishing testing
    END IF;

    RETURN new;
END;
$$
    LANGUAGE plpgsql;


CREATE TRIGGER account_payable_payment_term_schedule_delete_trg
    BEFORE UPDATE
    ON vendor_payment_term_schedule
    FOR EACH ROW
EXECUTE PROCEDURE soft_delete_check_fn('vendor_payment_term_schedule');
