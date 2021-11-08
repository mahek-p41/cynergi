package com.cynergisuite.middleware.schedule

import java.time.Month

interface BeginningOfMonthJob : Job<Month> {

   @Throws(ScheduleProcessingException::class)
   override fun process(schedule: ScheduleEntity, time: Month): JobResult
}
