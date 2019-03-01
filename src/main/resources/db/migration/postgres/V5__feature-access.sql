CREATE TABLE employee (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   username     TEXT                                   NOT NULL,
   password     TEXT                                   NOT NULL,
   level        NUMERIC(2)                             NOT NULL
);
CREATE TRIGGER update_employee_trg
   BEFORE UPDATE
   ON employee
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX employee_username_idx ON employee(username);

INSERT INTO employee(username, password, level) VALUES ('user1', 'user1', 20);
INSERT INTO employee(username, password, level) VALUES ('user2', 'user2', 30);

