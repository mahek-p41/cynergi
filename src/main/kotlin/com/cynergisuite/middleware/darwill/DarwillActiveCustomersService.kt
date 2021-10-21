package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.schedule.JobResult
import com.cynergisuite.middleware.schedule.OnceDailyJob
import com.cynergisuite.middleware.schedule.ScheduleEntity
import java.time.DayOfWeek
import javax.inject.Singleton

@Singleton
class DarwillActiveCustomersService: OnceDailyJob {

   override fun shouldProcess(schedule: ScheduleEntity, time: DayOfWeek): Boolean = time == DayOfWeek.SUNDAY

   override fun process(schedule: ScheduleEntity, time: DayOfWeek): JobResult {
      TODO("Not implemented")
   }
}
