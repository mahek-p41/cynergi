package com.cynergisuite.middleware.schedule.command

object ScheduleCommandTypeFactory {
   private val commands = listOf(
      ScheduleCommandTypeEntity(1, "ScheduleAudit", "Scheduling audits for stores", "schedule.command.schedule.audit")
   )

   fun scheduleAudit(): ScheduleCommandTypeEntity = commands.first { it.value == "ScheduleAudit" }

   fun random(): ScheduleCommandTypeEntity = commands.random()
}
