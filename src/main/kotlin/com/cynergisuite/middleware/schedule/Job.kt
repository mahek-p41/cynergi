package com.cynergisuite.middleware.schedule

import java.time.temporal.TemporalAccessor

sealed interface Job<in T : TemporalAccessor> {

   @Throws(ScheduleProcessingException::class)
   fun shouldProcess(schedule: ScheduleEntity, time: T): Boolean // TODO scheduler can this be removed and rely on the data in the database, or provide a default implementation here, that can be overridden if needed

   @Throws(ScheduleProcessingException::class)
   fun process(schedule: ScheduleEntity, time: T): JobResult
}
