package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.area.AreaType
import java.time.OffsetDateTime

/**
 * Implement this interface on Job's that depend on a specific Area being enabled in the system.
 */
abstract class AreaEnabledJob(
   private val areaService: AreaService,
   private val areaType: AreaType,
) : Job {

   override fun shouldProcess(schedule: ScheduleEntity, time: OffsetDateTime): Boolean {
      return areaService.isEnabledFor(schedule.company, areaType) && super.shouldProcess(schedule, time)
   }
}
