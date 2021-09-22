package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.WEEKLY
import io.micronaut.context.ApplicationContext
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ScheduleJobExecutorService @Inject constructor(
   private val applicationContext: ApplicationContext,
   private val companyRepository: CompanyRepository,
   private val scheduleRepository: ScheduleRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleJobExecutorService::class.java)

   @Scheduled(cron = "0 30 5 * * *")
   internal fun runDailyScheduled() { // this is what is called by the scheduler
      val jobsRan = runDaily()

      logger.info("Submitted {} jobs", jobsRan)
   }

   @Transactional
   fun runDaily(dayOfWeek: DayOfWeek = OffsetDateTime.now().dayOfWeek): Int { // useful for calling on-demand
      var jobsRan = 0

      companyRepository.forEach { company ->
         scheduleRepository.forEach(WEEKLY, company) { schedule ->
            val beanQualifier = DailyScheduleNameBeanQualifier(schedule.command.value)

            if (schedule.enabled && applicationContext.containsBean(OnceDailyJob::class.java, beanQualifier)) {
               val dailyTask = applicationContext.getBean(OnceDailyJob::class.java, beanQualifier)

               if (dailyTask.shouldProcess(schedule, dayOfWeek)) { // check if this job has anything to do for today
                  logger.info("Executing daily task for schedule {} using {}", schedule, dailyTask.javaClass.canonicalName)

                  val taskResult = dailyTask.processDaily(schedule, dayOfWeek) // process the job

                  logger.debug("Task result for schedule {} was {}", schedule, taskResult)

                  jobsRan++ // increment the result
               }
            } else {
               logger.error("Unable to find daily task for schedule {}", schedule)
            }
         }
      }

      return jobsRan
   }

   @Scheduled(cron = "0 0 6 1 JAN-DEC *") // run on the first day of the month
   internal fun runBeginningOfMonthScheduled() {
      companyRepository.forEach { company ->

      }
   }

   @Scheduled(cron = "0 45 5 L * ?") // run on the last day of the month
   internal fun runEndOfMonthScheduled() {
      companyRepository.forEach { company ->

      }
   }
}
