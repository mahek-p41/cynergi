CREATE TABLE legacy_importation (
   id           BIGSERIAL                             NOT NULL PRIMARY KEY,
   time_created TIMESTAMPTZ DEFAULT clock_timestamp() NOT NULL,
   filename     VARCHAR(1000) UNIQUE                  NOT NULL,
   hash         VARCHAR(64) UNIQUE                    NOT NULL
);

CREATE TABLE employee (
   id            BIGSERIAL                                       NOT NULL PRIMARY KEY,
   uu_row_id     UUID        DEFAULT uuid_generate_v1()          NOT NULL,
   time_created  TIMESTAMPTZ DEFAULT clock_timestamp()           NOT NULL,
   time_updated  TIMESTAMPTZ DEFAULT clock_timestamp()           NOT NULL,
   number        INTEGER     CHECK( number > 0 )                 NOT NULL,
   last_name     VARCHAR(15) CHECK( char_length(last_name) > 1 ) NOT NULL,
   first_name_mi VARCHAR(15),
   pass_code     VARCHAR(6)  CHECK( char_length(pass_code) > 2 ) NOT NULL,
   store_number  INTEGER     CHECK( store_number > 0 )           NOT NULL,
   active        BOOLEAN     DEFAULT TRUE                        NOT NULL,
   UNIQUE (number, pass_code)
);
CREATE TRIGGER update_employee_trg
   BEFORE UPDATE
   ON employee
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

-- begin notification_type_domain alterations

DROP TRIGGER update_notification_domain_type_trg ON notification_type_domain;
ALTER TABLE notification_type_domain
   DROP COLUMN uu_row_id;
ALTER TABLE notification_type_domain
   DROP COLUMN time_created;
ALTER TABLE notification_type_domain
   DROP COLUMN time_updated;
ALTER TABLE notification_type_domain
   ADD COLUMN localization_code VARCHAR(50);

UPDATE notification_type_domain SET localization_code = 'notification.store' WHERE upper(value) = 'S';
UPDATE notification_type_domain SET localization_code = 'notification.employee' WHERE upper(value) = 'E';
UPDATE notification_type_domain SET localization_code = 'notification.department' WHERE upper(value) = 'D';
UPDATE notification_type_domain SET localization_code = 'notification.all' WHERE upper(value) = 'A';

CREATE UNIQUE INDEX notification_type_domain_value ON notification_type_domain(upper(value));

ALTER TABLE notification_type_domain
   ALTER COLUMN localization_code SET NOT NULL;


-- end notification_type_domain alterations
