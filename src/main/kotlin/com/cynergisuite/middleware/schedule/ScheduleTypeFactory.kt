package com.cynergisuite.middleware.schedule

import org.apache.commons.lang3.RandomUtils

object ScheduleTypeFactory {
   private val types = listOf(
      ScheduleType(1, "HOURLY", "Hourly", "schedule.hourly")
   )

   @JvmStatic
   fun hourly(): ScheduleType = types.first { it.value == "HOURLY" }

   @JvmStatic
   fun random(): ScheduleType = types[RandomUtils.nextInt(0, types.size)]
}
