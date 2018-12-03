CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE address(
  id                     BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID                   UUID DEFAULT uuid_generate_v1()       NOT NULL,
  date_created           TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_updated           TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  address1               VARCHAR (150)                         NOT NULL,
  address2               VARCHAR (150)                         NOT NULL,
  city                   VARCHAR (150)                         NOT NULL,
  state                  VARCHAR (2)                           NOT NULL,
  postal_code            VARCHAR (20)                          NOT NULL,
  phone                  VARCHAR (20)                          NOT NULL,
  fax                    VARCHAR (20)                          NOT NULL,
  work_phone             VARCHAR (20)                          NOT NULL,
  work_phone_ext         VARCHAR (10)                          NOT NULL,
  map_cd                 VARCHAR (20)                          NOT NULL
);
CREATE TABLE business(
  id                     BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID                   UUID      DEFAULT uuid_generate_v1()  NOT NULL,
  date_created           TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_updated           TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  name                   VARCHAR(150)                          NOT NULL
);
CREATE TABLE store(
  id                     BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID                   UUID DEFAULT uuid_generate_v1()       NOT NULL,
  date_created           TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_updated           TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  name                   VARCHAR(150)                          NOT NULL,
  address_id             BIGINT REFERENCES address (id)        NOT NULL,
  business_id            BIGINT REFERENCES business (id)       NOT NULL
);

CREATE TABLE customer(
  id                     BIGSERIAL                             NOT NULL PRIMARY KEY,
  UUID                   UUID      DEFAULT uuid_generate_v1()  NOT NULL,
  date_created           TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_updated           TIMESTAMP DEFAULT current_timestamp   NOT NULL,
  last_name              VARCHAR(150) NOT NULL,
  first_name             VARCHAR(150) NOT NULL,
  address                BIGINT REFERENCES address (id)        NOT NULL
)
