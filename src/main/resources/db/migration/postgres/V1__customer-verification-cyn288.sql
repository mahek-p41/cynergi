CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE FUNCTION last_updated_column_fn()
  RETURNS TRIGGER AS
$$
BEGIN
  new.last_updated := current_timestamp;

  RETURN new;
END;
$$
  LANGUAGE plpgsql;

CREATE TABLE checklist_auto (
  id                  BIGSERIAL                       NOT NULL PRIMARY KEY,
  uuid                UUID DEFAULT uuid_generate_v1() NOT NULL,
  date_created        TIMESTAMP                       NOT NULL,
  last_updated        TIMESTAMP                       NOT NULL,
  auto_address        BOOLEAN,
  auto_comment        VARCHAR(100),
  auto_dealer_phone   VARCHAR(18),
  auto_diff_address   VARCHAR(50),
  auto_diff_emp       VARCHAR(50),
  auto_diff_phone     VARCHAR(18),
  auto_dmv_verify     BOOLEAN,
  auto_employer       BOOLEAN,
  auto_last_payment   TIMESTAMP,
  auto_name           VARCHAR(50),
  auto_next_payment   TIMESTAMP,
  auto_note           VARCHAR(50),
  auto_pay_freq       VARCHAR(10),
  auto_payment        FLOAT,
  auto_pending_action VARCHAR(50),
  auto_phone          BOOLEAN,
  auto_prev_loan      BOOLEAN,
  auto_purchase_date  TIMESTAMP,
  auto_related        VARCHAR(50)
);
CREATE TRIGGER update_checklist_auto_trg
  BEFORE UPDATE
  ON checklist_auto
  FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE checklist_employment (
  id            BIGSERIAL                       NOT NULL PRIMARY KEY,
  uuid          UUID DEFAULT uuid_generate_v1() NOT NULL,
  date_created  TIMESTAMP                       NOT NULL,
  last_updated  TIMESTAMP                       NOT NULL,
  emp_dept      VARCHAR(50),
  emp_hire_date TIMESTAMP,
  emp_leave_msg BOOLEAN,
  emp_name      VARCHAR(50),
  emp_reliable  BOOLEAN,
  emp_title     VARCHAR(50)
);
CREATE TRIGGER update_checklist_employment_trg
  BEFORE UPDATE
  ON checklist_employment
  FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE checklist_landlord (
  id              BIGSERIAL                       NOT NULL PRIMARY KEY,
  uuid            UUID DEFAULT uuid_generate_v1() NOT NULL,
  date_created    TIMESTAMP                       NOT NULL,
  last_updated    TIMESTAMP                       NOT NULL,
  land_address    BOOLEAN,
  land_alt_phone  VARCHAR(18),
  land_lease_type VARCHAR(25),
  land_leave_msg  BOOLEAN,
  land_length     INTEGER,
  land_name       VARCHAR(50),
  land_paid_rent  VARCHAR(15),
  land_phone      BOOLEAN,
  land_reliable   BOOLEAN,
  land_rent       NUMERIC(19, 2)
);
CREATE TRIGGER update_checklist_landlord_trg
  BEFORE UPDATE
  ON checklist_landlord
  FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE checklist (
  id                      BIGSERIAL                            NOT NULL PRIMARY KEY,
  uuid                    UUID      DEFAULT uuid_generate_v1() NOT NULL,
  date_created            TIMESTAMP DEFAULT current_timestamp  NOT NULL,
  last_updated            TIMESTAMP DEFAULT current_timestamp  NOT NULL,
  cust_acct               VARCHAR(10)                          NOT NULL,
  cust_comments           VARCHAR(255),
  cust_verified_by        VARCHAR(50),
  cust_verified_date      TIMESTAMP,
  cust_dataset            VARCHAR(6),
  checklist_auto_id       BIGINT REFERENCES checklist_auto(id),
  checklist_employment_id BIGINT REFERENCES checklist_employment(id),
  checklist_landlord_id   BIGINT REFERENCES checklist_landlord(id)
);
CREATE TRIGGER update_checklist_trg
  BEFORE UPDATE
  ON checklist
  FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE checklist_references (
  id                 BIGSERIAL                            NOT NULL PRIMARY KEY,
  uuid               UUID      DEFAULT uuid_generate_v1() NOT NULL,
  date_created       TIMESTAMP DEFAULT current_timestamp  NOT NULL,
  last_updated       TIMESTAMP DEFAULT current_timestamp  NOT NULL,
  ref_address        BOOLEAN,
  ref_has_home_phone BOOLEAN,
  ref_known          INTEGER,
  ref_leave_msg      BOOLEAN,
  ref_rating         VARCHAR(3),
  ref_relationship   BOOLEAN,
  ref_reliable       BOOLEAN,
  ref_time_frame     INTEGER,
  ref_verify_phone   BOOLEAN,
  checklist_id       BIGINT                               NOT NULL
);
CREATE TRIGGER update_checklist_references_trg
  BEFORE UPDATE
  ON checklist_references
  FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
