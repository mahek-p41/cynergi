DROP INDEX schedule_type_domain_value;
CREATE UNIQUE INDEX schedule_type_domain_value ON schedule_type_domain(UPPER(value));

DELETE FROM schedule_type_domain WHERE id = 1;
INSERT INTO schedule_type_domain(id, value, description, localization_code) VALUES (1, 'WEEKLY', 'Weekly', 'schedule.weekly');

CREATE TABLE schedule_command_type_domain (
    id                INTEGER                                            NOT NULL PRIMARY KEY,
    value             VARCHAR(25) CHECK ( CHAR_LENGTH(TRIM(VALUE)) > 0 ) NOT NULL,
    description       VARCHAR(50)                                        NOT NULL,
    localization_code VARCHAR(50)                                        NOT NULL
);
CREATE UNIQUE INDEX schedule_command_type_domain_value ON schedule_command_type_domain(UPPER(value));
INSERT INTO schedule_command_type_domain(id, value, description, localization_code) VALUES (1, 'AuditSchedule', 'Scheduling audits for stores', 'schedule.command.audit');

ALTER TABLE schedule
    ALTER COLUMN command SET DATA TYPE INTEGER USING (command::INTEGER);
ALTER TABLE schedule
    RENAME command TO command_id;
ALTER TABLE schedule
    ADD CONSTRAINT schedule_schedule_command_type_domain_fk FOREIGN KEY (command_id) REFERENCES schedule_command_type_domain (id);

ALTER TABLE schedule
    ADD COLUMN enabled BOOLEAN DEFAULT TRUE NOT NULL;
