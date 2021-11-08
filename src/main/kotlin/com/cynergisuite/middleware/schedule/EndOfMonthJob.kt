package com.cynergisuite.middleware.schedule

import java.time.Month

interface EndOfMonthJob : Job<Month> {

   @Throws(ScheduleProcessingException::class)
   override fun process(schedule: ScheduleEntity, time: Month): JobResult
}
