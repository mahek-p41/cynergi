package com.cynergisuite.middleware.schedule.type

import org.apache.commons.lang3.RandomUtils

object ScheduleTypeFactory {
   private val types = listOf(
      ScheduleType(3, "WEEKLY", "Weekly", "schedule.weekly")
   )

   @JvmStatic
   fun weekly(): ScheduleType = types.first { it.value == "WEEKLY" }

   @JvmStatic
   fun random(): ScheduleType = types[RandomUtils.nextInt(0, types.size)]
}
