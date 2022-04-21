package com.cynergisuite.middleware.schedule

import java.time.OffsetDateTime

/**
 * Describes the contract that the ScheduleJobExecutorService expects to run a job.  All scheduled work needs to implement this interface or one of its child interfaces
 * in order for the ScheduleJobExecutorService to pick it up and run it.
 */
interface Job {

   @Throws(ScheduleProcessingException::class)
   fun shouldProcess(schedule: ScheduleEntity, time: OffsetDateTime) = true

   @Throws(ScheduleProcessingException::class)
   fun process(schedule: ScheduleEntity, time: OffsetDateTime): JobResult
}
