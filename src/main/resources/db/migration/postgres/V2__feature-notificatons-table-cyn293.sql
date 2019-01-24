CREATE TABLE notification (
   id                BIGSERIAL                              NOT NULL PRIMARY KEY,
   uu_row_id         UUID        DEFAULT uuid_generate_v1() NOT NULL,
   time_created      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   time_updated      TIMESTAMPTZ DEFAULT clock_timestamp()  NOT NULL,
   company_id        VARCHAR(6)                             NOT NULL,
   expiration_date   DATE                                   NOT NULL,
   message           VARCHAR(500)                           NOT NULL,
   notification_type VARCHAR(255)                           NOT NULL,
   sending_employee  VARCHAR(255)                           NOT NULL,
   start_date        DATE                                   NOT NULL
);
CREATE TRIGGER update_notification_trg
   BEFORE UPDATE
   ON notification
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

