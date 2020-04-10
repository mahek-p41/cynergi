package com.cynergisuite.middleware.schedule.command

object ScheduleCommandTypeFactory {
   private val commands = listOf(
      ScheduleCommandTypeEntity(1, "AuditSchedule", "Scheduling audits for stores", "schedule.command.audit")
   )

   fun auditSchedule(): ScheduleCommandTypeEntity = commands.first { it.value == "AuditSchedule" }

   fun random(): ScheduleCommandTypeEntity = commands.random()
}
