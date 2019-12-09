package com.cynergisuite.middleware.schedule

interface Scheduler {

   @Throws(ScheduleProcessingException::class)
   fun processSchedule(schedule: ScheduleEntity) : ScheduleResult
}
