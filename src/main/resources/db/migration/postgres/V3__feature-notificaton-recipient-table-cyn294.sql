CREATE TABLE notification_recipient (
   id              BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id       UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated    TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   description     VARCHAR(255),
   recipient       VARCHAR(255)                           NOT NULL,
   notification_id BIGINT REFERENCES notification(id)     NOT NULL
);
CREATE TRIGGER update_notification_recipient_trg
   BEFORE UPDATE
   ON notification_recipient
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX notification_recipient_id_idx
   ON notification_recipient (notification_id);
ALTER TABLE notification_recipient
   ADD CONSTRAINT recipient_notification_id_uq UNIQUE (recipient, notification_id);
