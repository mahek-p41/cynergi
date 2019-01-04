CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE FUNCTION last_updated_column_fn()
  RETURNS TRIGGER AS $$
BEGIN
  new.last_updated := current_timestamp;

  return new;
END;
$$
LANGUAGE plpgsql;

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
ALTER TABLE company ADD CONSTRAINT company_name_uq UNIQUE (name);
