--- begin schedule type domain table
CREATE TABLE schedule_type_domain (
   id                INTEGER                                NOT NULL PRIMARY KEY,
   value             VARCHAR(15)                            NOT NULL,
   description       VARCHAR(50)                            NOT NULL,
   localization_code VARCHAR(50)                            NOT NULL
);
CREATE UNIQUE INDEX schedule_type_domain_value ON schedule_type_domain (value);

INSERT INTO schedule_type_domain(id, value, description, localization_code) VALUES (1, 'CRON', 'Cron style schedule', 'schedule.cron');

CREATE TABLE schedule (
   id                BIGSERIAL                                             NOT NULL PRIMARY KEY,
   uu_row_id         UUID           DEFAULT uuid_generate_v1()             NOT NULL,
   time_created      TIMESTAMPTZ    DEFAULT clock_timestamp()              NOT NULL,
   time_updated      TIMESTAMPTZ    DEFAULT clock_timestamp()              NOT NULL,
   title             VARCHAR(64)                                           NOT NULL,
   description       VARCHAR(256),
   schedule          VARCHAR(592)                                          NOT NULL,
   command           VARCHAR(1024)                                         NOT NULL,
   type_id           INTEGER        REFERENCES schedule_type_domain (id)   NOT NULL
);
CREATE TRIGGER update_schedule_trg
   BEFORE UPDATE
   ON schedule
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();

CREATE TABLE schedule_arg (
   id                BIGSERIAL                                             NOT NULL PRIMARY KEY,
   uu_row_id         UUID           DEFAULT uuid_generate_v1()             NOT NULL,
   time_created      TIMESTAMPTZ    DEFAULT clock_timestamp()              NOT NULL,
   time_updated      TIMESTAMPTZ    DEFAULT clock_timestamp()              NOT NULL,
   value             VARCHAR(256)                                          NOT NULL,
   description       VARCHAR(256),
   schedule_id       BIGINT         REFERENCES schedule (id)               NOT NULL
);
CREATE TRIGGER update_schedule_arg_trg
   BEFORE UPDATE
   ON schedule_arg
   FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
