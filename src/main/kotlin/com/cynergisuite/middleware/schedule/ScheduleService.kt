package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.WEEKLY
import com.cynergisuite.middleware.schedule.type.DAILY
import io.micronaut.context.ApplicationContext
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleService @Inject constructor(
   private val applicationContext: ApplicationContext,
   private val scheduleRepository: ScheduleRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleService::class.java)

   @Scheduled(cron = "0 30 5 * * *")
   internal fun runDailyScheduled() { // this is what is called by the scheduler
      val jobsRan = runDaily()

      logger.info("Submitted {} jobs", jobsRan)
   }

   @JvmOverloads
   fun runDaily(dayOfWeek: DayOfWeek = OffsetDateTime.now().dayOfWeek): Int { // useful for calling on-demand
      var jobsRan = 0

      scheduleRepository.forEach(DAILY) { schedule ->
         val beanQualifier = DailyScheduleNameBeanQualifier(schedule.title)

         if ((schedule.schedule == dayOfWeek.name || schedule.schedule == DAILY.value)
            && applicationContext.containsBean(DailySchedule::class.java, beanQualifier)) { // TODO look at using javax.inject.Named annotation rather than all this complicated logic to load the scheduler
            val dailyTask = applicationContext.getBean(DailySchedule::class.java, beanQualifier)

            logger.info("Executing daily task for schedule {} using {}", schedule, dailyTask.javaClass.canonicalName)

            val taskResult = dailyTask.processDaily(schedule)

            logger.debug("Task result for schedule {} was {}", schedule, taskResult)

            jobsRan++
         } else {
            logger.error("Unable to find daily task for schedule {}", schedule)
         }
      }

      scheduleRepository.forEach(WEEKLY) { schedule ->
         val beanQualifier = DailyScheduleNameBeanQualifier(schedule.title)

         if (schedule.schedule == dayOfWeek.name && applicationContext.containsBean(DailySchedule::class.java, beanQualifier)) { // TODO look at using javax.inject.Named annotation rather than all this complicated logic to load the scheduler
            val dailyTask = applicationContext.getBean(DailySchedule::class.java, beanQualifier)
            logger.info("Executing daily task for schedule {} using {}", schedule, dailyTask.javaClass.canonicalName)

            val taskResult = dailyTask.processDaily(schedule)

            logger.debug("Task result for schedule {} was {}", schedule, taskResult)

            jobsRan++
         } else {
            logger.error("Unable to find daily task for schedule {}", schedule)
         }
      }
      return jobsRan
   }
}
