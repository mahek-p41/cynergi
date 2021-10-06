package com.cynergisuite.middleware.schedule.command

class ScheduleCommandTypeTestDataLoader {
   private static final List<ScheduleCommandTypeEntity> commands = [
      new ScheduleCommandTypeEntity(1, "AuditSchedule", "Scheduling audits for stores", "schedule.command.audit")
   ]

   static ScheduleCommandTypeEntity auditSchedule() { commands.find { it.value == "AuditSchedule" } }

   static ScheduleCommandTypeEntity random() { commands.random() }
}
