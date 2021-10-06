package com.cynergisuite.middleware.schedule

import java.time.Month

interface EndOfMonthJob : Job<Month> {
   override fun process(schedule: ScheduleEntity, time: Month): JobResult
}
