package com.cynergisuite.middleware.schedule

import java.time.Month

interface BeginningOfMonthJob : Job<Month> {
   override fun process(schedule: ScheduleEntity, time: Month): JobResult
}
