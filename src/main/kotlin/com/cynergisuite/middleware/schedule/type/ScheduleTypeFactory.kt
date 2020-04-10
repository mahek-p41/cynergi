package com.cynergisuite.middleware.schedule.type

import org.apache.commons.lang3.RandomUtils

object ScheduleTypeFactory {
   private val types = listOf(
      WEEKLY
   )

   @JvmStatic
   fun weekly(): ScheduleType = WEEKLY

   @JvmStatic
   fun random(): ScheduleType = types[RandomUtils.nextInt(0, types.size)]
}
