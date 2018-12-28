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
  phone1             VARCHAR(20)                           NOT NULL,
  phone1_description VARCHAR(20)                           NOT NULL,
  phone2             VARCHAR(20),
  phone2_description VARCHAR(20),
  fax                VARCHAR(20),
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

CREATE TABLE store (
  id           BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID         UUID DEFAULT uuid_generate_v1()       NOT NULL,
  date_created TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_updated TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  name         VARCHAR(150)                          NOT NULL,
  num          VARCHAR(150)                          NOT NULL,
  address_id   BIGINT REFERENCES address (id)        NOT NULL,
  company_id   BIGINT REFERENCES company (id)        NOT NULL
);
CREATE TRIGGER update_store_trg
  BEFORE UPDATE
  ON store
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

CREATE TABLE customer (-- customers
  id                  BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID                UUID DEFAULT uuid_generate_v1()       NOT NULL,
  date_created        TIMESTAMP DEFAULT current_timestamp   NOT NULL, -- created_at
  last_updated        TIMESTAMP DEFAULT current_timestamp   NOT NULL, -- updated_at
  account             VARCHAR(150)                          NOT NULL, -- custacct
  last_name           VARCHAR(150)                          NOT NULL, -- name1_first
  first_name          VARCHAR(150)                          NOT NULL, -- name1_last
  contact_name        VARCHAR(150)                          NOT NULL, -- contact_name
  --address_id          BIGINT REFERENCES address (id)        NOT NULL, -- addr1, addr2, etc
  date_of_birth       DATE                                  NOT NULL, -- dob
  --taxable             BOOLEAN DEFAULT TRUE                  NOT NULL, -- tax_nbr
  tax_number          VARCHAR(150),
  --status_flag         VARCHAR(20)                           NOT NULL, -- status_flag
  allow_olp           BOOLEAN DEFAULT FALSE                 NOT NULL, -- allow_olp
  allow_recur         BOOLEAN DEFAULT FALSE                 NOT NULL, -- allow_recur
  --bttc                VARCHAR(150)                          NOT NULL, -- bttc
  --customer_receivable VARCHAR(150)                          NOT NULL, -- custreceivable
  --store_id            BIGINT REFERENCES store (id)          NOT NULL, -- store_num
  cell_opt_in         BOOLEAN DEFAULT FALSE                 NOT NULL, -- cell_optin
  cell_pin            VARCHAR(10),
  customer_vectors    TSVECTOR                              NOT NULL
  -- ssan                binary
  -- ssan_salt           binary
  -- driver_lic_nbr      binary
  -- driver_lic_nbr_salt binary
);

CREATE INDEX customer_search_idx
  ON customer
  USING gin (customer_vectors);

CREATE FUNCTION generate_customer_search_fn()
  RETURNS TRIGGER AS $$
BEGIN
  new.customer_vectors := to_tsvector(new.last_name) || to_tsvector(new.first_name) || to_tsvector(contact_name);
  new.last_updated := current_timestamp;

  return new;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER update_customer_search_trg
  BEFORE INSERT OR UPDATE
  ON customer
  FOR EACH ROW EXECUTE PROCEDURE generate_customer_search_fn();
