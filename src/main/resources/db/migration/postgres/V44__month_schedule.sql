ALTER TABLE schedule_type_domain ALTER COLUMN value TYPE VARCHAR (70);
INSERT INTO schedule_type_domain VALUES (2, 'BEGINNING_OF_MONTH', 'Beginning of the month', 'schedule.beginning.of.month');
INSERT INTO schedule_type_domain VALUES (3, 'END_OF_MONTH', 'End of the month', 'schedule.end.of.month');
