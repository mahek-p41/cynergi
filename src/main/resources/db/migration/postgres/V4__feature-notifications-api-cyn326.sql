CREATE TABLE notification_domain_type (
   id           BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id    UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   value        CHAR(1)                                NOT NULL,
   description  VARCHAR(15)                            NOT NULL
);
CREATE TRIGGER update_notification_type_trg
   BEFORE UPDATE
   ON notification_type
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
ALTER TABLE notification_domain_type
   ADD CONSTRAINT notification_domain_type_value_uq UNIQUE (value);

INSERT INTO notification_domain_type(value, description)
VALUES ('S', 'Store');
INSERT INTO notification_domain_type(value, description)
VALUES ('E', 'Employee');
INSERT INTO notification_domain_type(value, description)
VALUES ('D', 'Department');
INSERT INTO notification_domain_type(value, description)
VALUES ('A', 'All');

ALTER TABLE notification
   ADD COLUMN notification_domain_type_id BIGINT REFERENCES notification_domain_type(id);

UPDATE notification n
SET notification_domain_type_id = ndt.id
FROM notification_domain_type ndt
WHERE n.notification_type = ndt.value;

ALTER TABLE notification
   DROP COLUMN notification_type;

ALTER TABLE notification_domain_type
   ADD CONSTRAINT notification_notification_domain_type_uq


