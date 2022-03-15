package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.BEGINNING_OF_MONTH
import com.cynergisuite.middleware.schedule.type.END_OF_MONTH
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.schedule.type.WEEKLY
import io.micronaut.context.ApplicationContext
import io.micronaut.kotlin.context.getBean
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.ClassUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.Month
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor
import javax.transaction.Transactional
import kotlin.reflect.KClass

@Singleton
class ScheduleJobExecutorService @Inject constructor(
   private val applicationContext: ApplicationContext,
   private val companyRepository: CompanyRepository,
   private val scheduleRepository: ScheduleRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleJobExecutorService::class.java)

   @Transactional
   fun runDaily(dayOfWeek: DayOfWeek = OffsetDateTime.now().dayOfWeek, forceRun: Boolean = false) =
      runJob(dayOfWeek, WEEKLY, OnceDailyJob::class, forceRun)

   @Transactional
   fun runBeginningOfMonth(month: Month = OffsetDateTime.now().month, forceRun: Boolean = false) =
      runJob(month, BEGINNING_OF_MONTH, BeginningOfMonthJob::class, forceRun)

   @Transactional
   fun runEndOfMonth(month: Month = OffsetDateTime.now().month, forceRun: Boolean = false) =
      runJob(month, END_OF_MONTH, EndOfMonthJob::class, forceRun)

   private fun <T : TemporalAccessor, J : Job<T>> runJob(temporalAccessor: T, scheduleType: ScheduleType, jobClazz: KClass<J>, forceRun: Boolean = false): Int {
      return companyRepository.all()
         .flatMap { scheduleRepository.all(scheduleType, it) } // TODO schedule should change to just loading all schedules for a company that are enabled
         .onEach { logger.info("Loaded scheduled task {}, enabled: {}", it.title, it.enabled) }
         .filter { it.enabled } // FIXME why didn't I filter this in the query?
         .filter { // TODO scheduler can this be removed?  Just rely completely on the data in the database, see bellow for how to handle that in a different filter step, alternatively we can leave this logic and create a SystemJob and a UserJob set of interfaces, so something like Audit scheduling would be a UserJob and Darwill would be a SystemJob
            val job: Job<T> = applicationContext.getBean(it.command.value)

            ClassUtils.isAssignable(job::class.java, jobClazz.java)
         }
         .map { it to applicationContext.getBean<Job<T>>(it.command.value) }
         .onEach { (schedule, task) -> logger.info("Will {} execute: {} -> force run: {}", schedule.title, forceRun || task.shouldProcess(schedule, temporalAccessor), forceRun) }
         .filter { (schedule, task) -> forceRun || task.shouldProcess(schedule, temporalAccessor) } // TODO scheduler replace this with some logic that handles if a job should run using a when statement and the objects listed in ScheduleType on what checking should be done
         .onEach { (schedule, _) -> logger.info("Executing {}", schedule.title) }
         .map { (schedule, task) ->
            val result = try {
               task.process(schedule, temporalAccessor)
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

      logger.info("Submitted {} daily jobs", jobsRan)
   } // TODO scheduler if we go with a runs once per minute we'd need to implement some sort of job queue that can't have duplicates in it, since it is possible that a long running job could get queued up multiple times, and we should avoid that.
   // TODO scheduler if we go the coroutines route we'd need some way to remove jobs once the coroutine completes.  Using coroutines is the best way to handle scheduled jobs, because we can't overload the system with jobs.  We would need to convert database access to be suspend functions to get it all to work correctly.  This would be a good improvement over what we have now.

   // TODO scheduler can we get rid of this, and just use a single job that runs once per minute and checks if there are jobs to be ran.  If we go with a SystemJob and a UserJob setup we could leave this, and only SystemJobs would be trigged by this  (Does doing this make it too complex?)
   @Scheduled(cron = "0 0 6 1 JAN-DEC *") // run on the first day of the month
   internal fun runBeginningOfMonthScheduled() {
      val jobsRan = runBeginningOfMonth()

      logger.info("Submitted {} beginning of month jobs", jobsRan)
   }

   // TODO scheduler can we get rid of this, and just use a single job that runs once per minute and checks if there are jobs to be ran.  If we go with a SystemJob and a UserJob setup we could leave this, and only SystemJobs would be trigged by this (Does doing this make it too complex?)
   @Scheduled(cron = "0 45 5 L * ?") // run on the last day of the month
   internal fun runEndOfMonthScheduled() {
      val jobsRan = runEndOfMonth()

      logger.info("Submitted {} end of month jobs", jobsRan)
   }
}
