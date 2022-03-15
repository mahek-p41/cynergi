package com.cynergisuite.middleware.schedule.command

class ScheduleCommandTypeTestDataLoader {
   private static final List<ScheduleCommandType> commands = [
      AuditSchedule.INSTANCE as ScheduleCommandType,
      DarwillInactiveCustomer.INSTANCE as ScheduleCommandType,
      DarwillActiveCustomer.INSTANCE as ScheduleCommandType,
      DarwillBirthday.INSTANCE as ScheduleCommandType,
      DarwillCollection.INSTANCE as ScheduleCommandType,
      DarwillLastWeeksDelivery.INSTANCE as ScheduleCommandType,
      DarwillLastWeeksPayout.INSTANCE as ScheduleCommandType,
   ]

   static ScheduleCommandTypeEntity auditSchedule() { return ScheduleCommandTypeKt.toEntity(commands.find { it.value == "AuditSchedule" }) }

   static ScheduleCommandTypeEntity random() { ScheduleCommandTypeKt.toEntity(commands.random()) }
}
