package com.cynergisuite.middleware.accounting.general.ledger.trial.balance.infrastructure

import com.cynergisuite.domain.GeneralLedgerTrialBalanceReportFilterRequest
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerTrialBalanceReportTemplate
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerTrialBalanceService
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@AreaControl("GL")
@Controller("/api/general-ledger/trial-balance")
class GeneralLedgerTrialBalanceController @Inject constructor(
   private val generalLedgerTrialBalanceService: GeneralLedgerTrialBalanceService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerTrialBalanceController::class.java)

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/report{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(
      tags = ["GeneralLedgerTrialBalanceEndpoints"],
      summary = "Fetch a General Ledger Trial Balance Report",
      description = "Fetch a General Ledger Trial Balance Report",
      operationId = "generalLedgerTrialBalance-fetchReport"
   )
   @ApiResponses(
      value = [
         ApiResponse(
            responseCode = "200",
            content = [Content(
               mediaType = APPLICATION_JSON,
               schema = Schema(implementation = GeneralLedgerTrialBalanceReportTemplate::class)
            )]
         ),
         ApiResponse(
            responseCode = "404",
            description = "The requested GeneralLedgerTrialBalance was unable to be found"
         ),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "filterRequest", `in` = ParameterIn.QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: GeneralLedgerTrialBalanceReportFilterRequest,
      authentication: Authentication
   ): GeneralLedgerTrialBalanceReportTemplate {
      val userCompany = userService.fetchUser(authentication).myCompany()

      logger.info("Fetching General Ledger Trial Balance Report")

      return generalLedgerTrialBalanceService.fetchReport(userCompany, filterRequest)
   }

}
