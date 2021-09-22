package com.cynergisuite.middleware.schedule

import java.time.temporal.TemporalAccessor

sealed interface Job<in T : TemporalAccessor> {

   @Throws(ScheduleProcessingException::class)
   fun shouldProcess(schedule: ScheduleEntity, time: T): Boolean
}
