package com.cynergisuite.middleware.schedule

import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.schedule.type.BEGINNING_OF_MONTH
import com.cynergisuite.middleware.schedule.type.END_OF_MONTH
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.schedule.type.WEEKLY
import io.micronaut.context.ApplicationContext
import io.micronaut.context.Qualifier
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.Month
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor
import javax.inject.Inject
import javax.inject.Singleton
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
   fun runDaily(dayOfWeek: DayOfWeek = OffsetDateTime.now().dayOfWeek) = // useful for calling on-demand
      runJob(dayOfWeek, WEEKLY, OnceDailyJob::class) { OnceDailyJobQualifier(it) }

   @Transactional
   fun runBeginningOfMonth(month: Month = OffsetDateTime.now().month) =
      runJob(month, BEGINNING_OF_MONTH, BeginningOfMonthJob::class) { BeginningOfMonthJobQualifier(it) }

   @Transactional
   fun runEndOfMonth(month: Month = OffsetDateTime.now().month) =
      runJob(month, END_OF_MONTH, EndOfMonthJob::class) { EndOfMonthJobQualifier(it) }

   private fun <T : TemporalAccessor, J : Job<T>> runJob(temporalAccessor: T, scheduleType: ScheduleType, jobClazz: KClass<J>, qualifierSupplier: (commandValue: String) -> Qualifier<J>): Int {
      return companyRepository.all()
         .flatMap { scheduleRepository.all(scheduleType, it) }
         .filter { it.enabled }
         .map { it to qualifierSupplier(it.command.value) }
         .filter { (_, qualifier) -> applicationContext.containsBean(jobClazz.java, qualifier) }
         .map { (schedule, qualifier) -> schedule to applicationContext.getBean(jobClazz.java, qualifier) }
         .map { (schedule, task) -> task.process(schedule, temporalAccessor) }
         .count()
   }

   @Scheduled(cron = "0 30 5 * * *")
   internal fun runDailyScheduled() { // this is what is called by the scheduler
      val jobsRan = runDaily()

      logger.info("Submitted {} daily jobs", jobsRan)
   }

   @Scheduled(cron = "0 0 6 1 JAN-DEC *") // run on the first day of the month
   internal fun runBeginningOfMonthScheduled() {
      val jobsRan = runBeginningOfMonth()

      logger.info("Submitted {} beginning of month jobs", jobsRan)
   }

   @Scheduled(cron = "0 45 5 L * ?") // run on the last day of the month
   internal fun runEndOfMonthScheduled() {
      val jobsRan = runEndOfMonth()

      logger.info("Submitted {} end of month jobs", jobsRan)
   }
}
