package com.cynergisuite.middleware.schedule

import java.time.DayOfWeek

interface DailySchedule {

   @Throws(ScheduleProcessingException::class)
   fun shouldProcess(schedule: ScheduleEntity, dayOfWeek: DayOfWeek) : Boolean

   @Throws(ScheduleProcessingException::class)
   fun processDaily(schedule: ScheduleEntity, dayOfWeek: DayOfWeek) : ScheduleResult
}
