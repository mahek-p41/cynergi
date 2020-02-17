DELETE FROM schedule_arg arg
USING schedule sched
WHERE sched.id = arg.schedule_id
      AND sched.command_id = (SELECT id FROM schedule_command_type_domain WHERE value = 'AuditSchedule')
      AND arg.description = 'department'
