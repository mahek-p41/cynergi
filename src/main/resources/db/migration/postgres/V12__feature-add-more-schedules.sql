DROP INDEX schedule_type_domain_value;
CREATE UNIQUE INDEX schedule_type_domain_value ON schedule_type_domain(UPPER(value));

INSERT INTO schedule_type_domain(id, value, description, localization_code) VALUES (2, 'DAILY', 'Daily', 'schedule.daily');
INSERT INTO schedule_type_domain(id, value, description, localization_code) VALUES (3, 'WEEKLY', 'Weekly', 'schedule.weekly');
INSERT INTO schedule_type_domain(id, value, description, localization_code) VALUES (4, 'MONTHLY', 'Monthly', 'schedule.monthly');

CREATE TABLE audit_schedule(
    id                BIGSERIAL                                        NOT NULL PRIMARY KEY,
    uu_row_id         UUID        DEFAULT uuid_generate_v1()           NOT NULL,
    time_created      TIMESTAMPTZ DEFAULT clock_timestamp()            NOT NULL,
    time_updated      TIMESTAMPTZ DEFAULT clock_timestamp()            NOT NULL,
    store_number      INTEGER     CHECK( store_number > 0 )            NOT NULL,
    department_access VARCHAR(2)  CHECK( char_length(department) = 2 ) NOT NULL,
    schedule_id       BIGINT      REFERENCES schedule(id)              NOT NULL
);
CREATE TRIGGER audit_schedule_trg
    BEFORE UPDATE
    ON audit_schedule
    FOR EACH ROW
EXECUTE PROCEDURE last_updated_column_fn();
CREATE INDEX audit_schedule_schedule_id ON audit_schedule (schedule_id);
