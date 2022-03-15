INSERT INTO schedule_type_domain (id, value, description, localization_code)
VALUES (4, 'DAILY', 'Daily', 'schedule.daily');

INSERT INTO schedule_arg(value, description, schedule_id)
SELECT
  sched.schedule as value,
  'dayOfWeek' as description,
   sched.id AS schdule_id
FROM schedule sched
WHERE sched.command_id = (SELECT id FROM schedule_command_type_domain WHERE value = 'AuditSchedule');

UPDATE schedule sched
SET schedule = 'DAILY'
WHERE sched.command_id = (SELECT id FROM schedule_command_type_domain WHERE value = 'AuditSchedule');
