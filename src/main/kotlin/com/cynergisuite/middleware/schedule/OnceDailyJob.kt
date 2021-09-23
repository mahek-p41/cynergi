package com.cynergisuite.middleware.schedule

import java.time.DayOfWeek

interface OnceDailyJob : Job<DayOfWeek> {

   @Throws(ScheduleProcessingException::class)
   override fun process(schedule: ScheduleEntity, time: DayOfWeek): JobResult
}
