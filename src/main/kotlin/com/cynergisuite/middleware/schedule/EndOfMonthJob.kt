package com.cynergisuite.middleware.schedule

import java.time.Month

// TODO scheduler can we get rid of this abstraction, just rely on the type in the database, and some logic for beginning of the month and end of the month using the time.
interface EndOfMonthJob : Job<Month> {

   @Throws(ScheduleProcessingException::class)
   override fun process(schedule: ScheduleEntity, time: Month): JobResult
}
