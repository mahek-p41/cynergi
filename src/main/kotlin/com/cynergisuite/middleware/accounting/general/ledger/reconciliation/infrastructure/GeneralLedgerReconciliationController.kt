package com.cynergisuite.middleware.accounting.general.ledger.reconciliation.infrastructure

import com.cynergisuite.domain.GeneralLedgerReconciliationReportFilterRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationDTO
import com.cynergisuite.middleware.accounting.general.ledger.reconciliation.GeneralLedgerReconciliationReportEntity
import com.cynergisuite.middleware.accounting.general.ledger.reconciliation.GeneralLedgerReconciliationService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
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

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/general-ledger/reconciliation")
class GeneralLedgerReconciliationController @Inject constructor(
   private val userService: UserService,
   private val glReconciliationService: GeneralLedgerReconciliationService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReconciliationController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerReconciliationEndpoints"], summary = "Fetch a GeneralLedgerReconciliationReport", description = "Fetch a  GeneralLedgerReconciliationReport", operationId = "generalLedgerReconciliationReport-fetchReport")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReconciliationReportEntity::class))]),
         ApiResponse(responseCode = "404", description = "The requested BankReconciliation was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      filterRequest: GeneralLedgerReconciliationReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReconciliationReportEntity? {
      logger.info("Fetching BankReconciliation by date {}", filterRequest.date)

      val user = userService.fetchUser(authentication)
      val response = glReconciliationService.fetchReport( user.myCompany(), filterRequest, httpRequest.findLocaleWithDefault())

      logger.debug("Fetching BankReconciliation by {} resulted in", filterRequest.date, response)

      return response
   }
}
