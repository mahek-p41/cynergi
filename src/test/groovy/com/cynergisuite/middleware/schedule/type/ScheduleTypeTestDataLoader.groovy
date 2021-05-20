package com.cynergisuite.middleware.schedule.type

class ScheduleTypeTestDataLoader {
   private static final List<ScheduleType> types = [
      Weekly.INSTANCE as ScheduleType
   ]

   static ScheduleType weekly() { Weekly.INSTANCE }

   static ScheduleType random() { types.random() }
}
