package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import io.micronaut.context.ApplicationContext
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleService @Inject constructor(
   private val applicationContext: ApplicationContext,
   private val scheduleRepository: ScheduleRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleService::class.java)

   @Scheduled(cron = "0 0 5 ? * *")
   internal fun runDaily() {

   }
}
