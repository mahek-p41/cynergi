package com.cynergisuite.middleware.schedule.type

import org.apache.commons.lang3.RandomUtils

object ScheduleTypeFactory {
   private val types = listOf(
      ScheduleTypeEntity(1, "WEEKLY", "Weekly", "schedule.weekly")
   )

   @JvmStatic
   fun weekly(): ScheduleTypeEntity = types.first { it.value == "WEEKLY" }

   @JvmStatic
   fun random(): ScheduleTypeEntity = types[RandomUtils.nextInt(0, types.size)]
}
