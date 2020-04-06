package com.cynergisuite.middleware.schedule.command

object ScheduleCommandTypeFactory {
   private val commands = listOf(
      ScheduleCommandTypeEntity(1, "AuditSchedule", "Scheduling audits for stores", "schedule.command.audit"),
      ScheduleCommandTypeEntity(2, "PastDueAuditReminder", "Reminder for past due audits of stores", "schedule.command.reminder.past.due.audits")
   )

   fun auditSchedule(): ScheduleCommandTypeEntity = commands.first { it.value == "AuditSchedule" }
   fun pastDueAuditReminder(): ScheduleCommandTypeEntity = commands.first { it.value == "PastDueAuditReminder" }

   fun random(): ScheduleCommandTypeEntity = commands.random()
}
