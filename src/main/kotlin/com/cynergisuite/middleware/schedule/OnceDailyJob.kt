package com.cynergisuite.middleware.schedule

import java.time.DayOfWeek

// TODO scheduler can we get rid of this and use the database to determine if something should be ran, combined with some business logic using the current time?
interface OnceDailyJob : Job<DayOfWeek> {

   @Throws(ScheduleProcessingException::class)
   override fun process(schedule: ScheduleEntity, time: DayOfWeek): JobResult
}
