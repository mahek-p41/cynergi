CREATE TABLE division
 (
    id                 BIGSERIAL   NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()               NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    company_id         INTEGER REFERENCES company(id),
    number             INTEGER DEFAULT 1 NOT NULL,
    name               varchar(50),
    description        varchar(50)
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
    number             INTEGER DEFAULT 1 NOT NULL,
    name               varchar(50),
    description        varchar(50)
 );
CREATE TRIGGER update_region_trg
   BEFORE UPDATE
   ON region
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE division_to_region
 (
    region_number      INTEGER NOT NULL,
    division_number    INTEGER NOT NULL,
    CONSTRAINT uq_division_to_region UNIQUE
    ( region_number,
      division_number
    )
 );

CREATE TRIGGER update_division_to_region_trg
   BEFORE UPDATE
   ON division_to_region
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE region_to_store
 (
    region_number         INTEGER NOT NULL,
    store_number          INTEGER NOT NULL
 );


CREATE TRIGGER update_division_to_region_trg
   BEFORE UPDATE
   ON region_to_store
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


CREATE TABLE division_manager
 (
    id                 BIGSERIAL   NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()               NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    division_number    INTEGER,
    emp_number         INTEGER,
    name               varchar(50),
    active             BOOLEAN DEFAULT 'No',
    inactive_date      Date
 );
CREATE TRIGGER update_division_manager_trg
   BEFORE UPDATE
   ON division_manager
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE region_manager
 (
    id                 BIGSERIAL   NOT NULL PRIMARY KEY,
    uu_row_id          UUID        DEFAULT uuid_generate_v1()               NOT NULL,
    time_created       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    time_updated       TIMESTAMPTZ DEFAULT clock_timestamp()                NOT NULL,
    region_number      INTEGER,
    emp_number         INTEGER,
    name               varchar(50),
    active             BOOLEAN DEFAULT 'No',
    inactive_date      Date
 );
CREATE TRIGGER update_region_manager_trg
   BEFORE UPDATE
   ON region_manager
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();


