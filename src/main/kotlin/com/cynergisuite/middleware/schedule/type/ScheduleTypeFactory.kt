package com.cynergisuite.middleware.schedule.type

import org.apache.commons.lang3.RandomUtils

object ScheduleTypeFactory {
   private val types = listOf(
      WEEKLY, DAILY
   )

   @JvmStatic
   fun weekly(): ScheduleType = WEEKLY
   @JvmStatic
   fun daily(): ScheduleType = DAILY

   @JvmStatic
   fun random(): ScheduleType = types[RandomUtils.nextInt(0, types.size)]
}
