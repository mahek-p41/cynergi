CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE FUNCTION last_updated_column_fn()
  RETURNS TRIGGER AS $$
BEGIN
  new.last_updated := current_timestamp;

  return new;
END;
$$
LANGUAGE plpgsql;

CREATE TABLE address (
  id                 BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID               UUID DEFAULT uuid_generate_v1()       NOT NULL,
  date_created       TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_updated       TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  address1           VARCHAR(150)                          NOT NULL,
  address2           VARCHAR(150),
  city               VARCHAR(150)                          NOT NULL,
  state              VARCHAR(2)                            NOT NULL,
  postal_code        VARCHAR(20)                           NOT NULL,
  zip_plus_four      VARCHAR(10),
  country            VARCHAR(150)                          NOT NULL,
  phone              VARCHAR(20)                           NOT NULL,
  email_address      VARCHAR(200)                          NOT NULL
);
CREATE TRIGGER update_address_trg
  BEFORE UPDATE
  ON address
  FOR EACH ROW EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE company (
  id           BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID         UUID DEFAULT uuid_generate_v1()       NOT NULL,
  date_created TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_updated TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  name         VARCHAR(150)                          NOT NULL
);
CREATE TRIGGER update_company_trg
  BEFORE UPDATE
  ON company
  FOR EACH ROW EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE employee (
  id           BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID         UUID DEFAULT uuid_generate_v1()       NOT NULL,
  date_created TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_updated TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  first_name   VARCHAR (150)                         NOT NULL,
  last_name    VARCHAR (150)                         NOT NULL,
  address_id   BIGINT REFERENCES address (id)        NOT NULL,
  company_id   BIGINT REFERENCES company (id)        NOT NULL
);
CREATE TRIGGER update_employee_trg
  BEFORE UPDATE
  ON employee
  FOR EACH ROW EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE customer (
  id                  BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID                UUID DEFAULT uuid_generate_v1()       NOT NULL,
  date_created        TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_updated        TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  account             VARCHAR(150)                          NOT NULL,
  last_name           VARCHAR(150)                          NOT NULL,
  first_name          VARCHAR(150)                          NOT NULL,
  contact_name        VARCHAR(150)                          NOT NULL,
  date_of_birth       DATE                                  NOT NULL,
  --address_id          BIGINT REFERENCES address (id)        NOT NULL,
  customer_vectors    TSVECTOR                              NOT NULL
);

CREATE INDEX customer_search_idx
  ON customer
  USING gin (customer_vectors);

CREATE FUNCTION generate_customer_search_fn()
  RETURNS TRIGGER AS $$
BEGIN
  new.customer_vectors := to_tsvector(new.last_name) || to_tsvector(new.first_name) || to_tsvector(new.contact_name);
  new.last_updated := current_timestamp;

  return new;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER update_customer_search_trg
  BEFORE INSERT OR UPDATE
  ON customer
  FOR EACH ROW EXECUTE PROCEDURE generate_customer_search_fn();
