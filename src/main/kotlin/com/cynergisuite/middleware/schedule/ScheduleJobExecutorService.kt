package com.cynergisuite.middleware.schedule

import com.cynergisuite.extensions.toDayOfWeek
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.BeginningOfMonth
import com.cynergisuite.middleware.schedule.type.Daily
import com.cynergisuite.middleware.schedule.type.EndOfMonth
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.schedule.type.Weekly
import io.micronaut.context.ApplicationContext
import io.micronaut.kotlin.context.getBean
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import javax.transaction.Transactional

@Singleton
class ScheduleJobExecutorService @Inject constructor(
   private val applicationContext: ApplicationContext,
   private val companyRepository: CompanyRepository,
   private val scheduleRepository: ScheduleRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleJobExecutorService::class.java)

   @Transactional
   fun runDaily(time: OffsetDateTime = OffsetDateTime.now(), forceRun: Boolean = false) =
      runJob(time, Daily, forceRun)

   fun runWeekly(time: OffsetDateTime = OffsetDateTime.now(), forceRun: Boolean = false) =
      runJob(time, Weekly, forceRun)

   @Transactional
   fun runBeginningOfMonth(time: OffsetDateTime = OffsetDateTime.now(), forceRun: Boolean = false) =
      runJob(time, BeginningOfMonth, forceRun)

   @Transactional
   fun runEndOfMonth(time: OffsetDateTime = OffsetDateTime.now(), forceRun: Boolean = false) =
      runJob(time, EndOfMonth, forceRun)

   private fun runJob(time: OffsetDateTime, scheduleType: ScheduleType, forceRun: Boolean = false): Int {
      return companyRepository.all()
         .flatMap { scheduleRepository.allEnabled(scheduleType, it) }
         .onEach { logger.info("Loaded scheduled enabled task {}", it.title) }
         .map { it to applicationContext.getBean<Job>(it.command.value) }
         .filter { (schedule, task) ->

            if (forceRun) {
               true
            } else if (scheduleType == Weekly) {
               val dayOfWeek = schedule.schedule.toDayOfWeek()

               time.dayOfWeek == dayOfWeek && task.shouldProcess(schedule, time)
            } else {
               task.shouldProcess(schedule, time)
            }
         } // TODO scheduler replace this with some logic that handles if a job should run using a when statement and the objects listed in ScheduleType on what checking should be done
         .onEach { (schedule, _) -> logger.info("Executing {}", schedule.title) }
         .map { (schedule, task) ->
            val result = try {
               task.process(schedule, time)
            } catch (e: Throwable) {
               ErrorJobResult(e, schedule.title)
            }

            schedule to result
         }
         .onEach { (schedule, result) -> if (!(result.failureReason().isNullOrBlank())) logger.error("Job {} failed {}", schedule.title, result.failureReason()) }
         .filter { (_, result) -> result.failureReason().isNullOrBlank() }
         .onEach { (schedule, _) -> logger.info("Successfully executed {}", schedule.title) }
         .count()
   }

   // TODO scheduler can we get rid of this, and just use a single job that runs once per minute (or some other larger interval) and checks if there are jobs to be ran
   @Scheduled(cron = "0 30 5 * * *")
   internal fun runDailyScheduled() { // this is what is called by the scheduler
      val jobsRan = runDaily()

      logger.info("Successfully ran {} daily jobs", jobsRan)
   } // TODO scheduler if we go with a runs once per minute we'd need to implement some sort of job queue that can't have duplicates in it, since it is possible that a long running job could get queued up multiple times, and we should avoid that.
   // TODO scheduler if we go the coroutines route we'd need some way to remove jobs once the coroutine completes.  Using coroutines is the best way to handle scheduled jobs, because we can't overload the system with jobs.  We would need to convert database access to be suspend functions to get it all to work correctly.  This would be a good improvement over what we have now.

   @Scheduled(cron = "0 45 5 * * *")
   internal fun runWeeklyScheduled() {
      val jobsRan = runWeekly()

      logger.info("Successfully ran {} weekly jobs", jobsRan)
   }

   // TODO scheduler can we get rid of this, and just use a single job that runs once per minute and checks if there are jobs to be ran.  If we go with a SystemJob and a UserJob setup we could leave this, and only SystemJobs would be trigged by this  (Does doing this make it too complex?)
   @Scheduled(cron = "0 0 6 1 JAN-DEC *") // run on the first day of the month
   internal fun runBeginningOfMonthScheduled() {
      val jobsRan = runBeginningOfMonth()

      logger.info("Successfully ran {} beginning of month jobs", jobsRan)
   }

   // TODO scheduler can we get rid of this, and just use a single job that runs once per minute and checks if there are jobs to be ran.  If we go with a SystemJob and a UserJob setup we could leave this, and only SystemJobs would be trigged by this (Does doing this make it too complex?)
   @Scheduled(cron = "0 45 5 L * ?") // run on the last day of the month
   internal fun runEndOfMonthScheduled() {
      val jobsRan = runEndOfMonth()

      logger.info("Successfully ran {} end of month jobs", jobsRan)
   }
}
