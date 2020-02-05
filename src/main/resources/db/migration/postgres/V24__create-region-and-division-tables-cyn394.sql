CREATE TABLE division
 (
    id                 BIGSERIAL   NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()               NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    company_id         BIGINT REFERENCES company(id) NOT NULL,
    number             INTEGER DEFAULT 1 NOT NULL,
    name               varchar(50) DEFAULT 'Division' NOT NULL,
    employee_number    INTEGER CHECK( employee_number > 0 ) NOT NULL,
    description        varchar(50),
    CONSTRAINT uq_division UNIQUE
    ( id,
      company_id,
      number,
      employee_number
    )
 );
CREATE TRIGGER update_division_trg
   BEFORE UPDATE
   ON division
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE region
 (
    id                 BIGSERIAL   NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()               NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    division_id        BIGINT REFERENCES division(id)                       NOT NULL,
    number             INTEGER DEFAULT 1 NOT NULL,
    name               varchar(50) DEFAULT 'Region' NOT NULL,
    employee_number    INTEGER CHECK( employee_number > 0 ) NOT NULL,
    description        varchar(50),
    CONSTRAINT uq_region UNIQUE
    ( id,
      division_id,
      number,
      employee_number
    )
 );
CREATE TRIGGER update_region_trg
   BEFORE UPDATE
   ON region
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE TABLE region_to_store
 (
    region_id BIGINT REFERENCES region(id) NOT NULL,
    company_id BIGINT REFERENCES company(id) NOT NULL,
    store_number INTEGER  CHECK( store_number > 0 ) NOT NULL,
    CONSTRAINT uq_region_to_store UNIQUE
    ( region_id,
      company_id,
      store_number
    )
 );





