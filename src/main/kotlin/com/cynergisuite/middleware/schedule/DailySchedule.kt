package com.cynergisuite.middleware.schedule

interface DailySchedule {

   @Throws(ScheduleProcessingException::class)
   fun processDaily(schedule: ScheduleEntity) : ScheduleResult
}
