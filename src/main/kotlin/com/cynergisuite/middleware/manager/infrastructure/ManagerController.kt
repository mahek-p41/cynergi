package com.cynergisuite.middleware.manager.infrastructure

import com.cynergisuite.middleware.company.CompanyService
import com.cynergisuite.middleware.manager.SftpClientCredentialsDto
import com.cynergisuite.middleware.darwill.DarwillService
import com.cynergisuite.middleware.wow.WowService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.schedule.ScheduleJobExecutorService
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import io.micronaut.context.annotation.Parameter
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Secured(IS_ANONYMOUS)
@Controller("/manage")
class ManagerController @Inject constructor(
   private val companyService: CompanyService,
   private val darwillService: DarwillService,
   private val wowService: WowService,
   private val scheduleJobExecutorService: ScheduleJobExecutorService,
) {
   private val logger: Logger = LoggerFactory.getLogger(ManagerController::class.java)

   @Post("/darwill", consumes = [APPLICATION_JSON])
   fun enableDarwill(
      @Valid @Body darwillManagement: SftpClientCredentialsDto
   ) {
      val company = companyService.fetchOne(darwillManagement.companyId!!) ?: throw NotFoundException(darwillManagement.companyId)

      logger.info("Enabling darwill for {}", company.datasetCode)

      darwillService.enableFor(company, SftpClientCredentials(darwillManagement))
   }

   @Delete("/darwill/{companyId}")
   fun disableDarwill(
      @Parameter("companyId") companyId: UUID
   ) {
      val company = companyService.fetchOne(companyId) ?: throw NotFoundException(companyId)

      logger.info("Disabling darwill for {}", company.datasetCode)

      darwillService.disableFor(company)
   }

   @Post("/wow", consumes = [APPLICATION_JSON])
   fun enableWow(
      @Valid @Body wowManagement: SftpClientCredentialsDto
   ) {
      val company = companyService.fetchOne(wowManagement.companyId!!) ?: throw NotFoundException(wowManagement.companyId)

      logger.info("Enabling wow for {}", company.datasetCode)

      wowService.enableFor(company, SftpClientCredentials(wowManagement))
   }

   @Delete("/wow/{companyId}")
   fun disableWow(
      @Parameter("companyId") companyId: UUID
   ) {
      val company = companyService.fetchOne(companyId) ?: throw NotFoundException(companyId)

      logger.info("Disabling wow for {}", company.datasetCode)

      wowService.disableFor(company)
   }

   @Post("/schedule/run/daily")
   fun runDaily() {
      logger.info("Daily scheduled jobs run interactively: {}", scheduleJobExecutorService.runDaily(forceRun = true))
   }

   @Post("/schedule/run/beginning/of/month")
   fun runBeginningOfMonth() {
      logger.info("Beginning of the month scheduled jobs run interactively {}", scheduleJobExecutorService.runBeginningOfMonth(forceRun = true))
   }

   @Post("/schedule/run/end/of/month")
   fun runEndOfMonth() {
      logger.info("End of month scheduled jobs run interactively {}", scheduleJobExecutorService.runEndOfMonth(forceRun = true))
   }
}
