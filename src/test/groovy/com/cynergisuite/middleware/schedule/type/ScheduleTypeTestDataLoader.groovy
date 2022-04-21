package com.cynergisuite.middleware.schedule.type

class ScheduleTypeTestDataLoader {
   private static final List<ScheduleType> types = [
      Weekly.INSTANCE as ScheduleType,
      BeginningOfMonth.INSTANCE as ScheduleType,
      EndOfMonth.INSTANCE as ScheduleType,
      Daily.INSTANCE as ScheduleType
   ]

   static ScheduleTypeEntity weekly() { ScheduleTypeKt.toEntity(Weekly.INSTANCE) }

   static ScheduleTypeEntity daily() { ScheduleTypeKt.toEntity(Daily.INSTANCE) }

   static ScheduleTypeEntity random() { ScheduleTypeKt.toEntity(types.random()) }

   static List<ScheduleTypeEntity> all() { types.collect { ScheduleTypeKt.toEntity(it) } }
}
