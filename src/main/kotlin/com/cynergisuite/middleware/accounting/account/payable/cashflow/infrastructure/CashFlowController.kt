package com.cynergisuite.middleware.accounting.account.payable.cashflow.infrastructure

import com.cynergisuite.domain.CashFlowFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.cashflow.AccountPayableCashFlowDTO
import com.cynergisuite.middleware.accounting.account.payable.cashflow.AccountPayableCashFlowService
import com.cynergisuite.middleware.accounting.account.payable.cashout.infrastructure.AccountPayableCashRequirementController
import com.cynergisuite.middleware.authentication.user.UserService
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


@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounting/account-payable/cashflow")
class CashFlowController @Inject constructor(
   private val accountPayableCashFlowService: AccountPayableCashFlowService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableCashRequirementController::class.java)

   @Get(uri = "{?filterRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["AccountPayableCashFlowReportEndpoints"], summary = "Fetch an Account Payable Cash Flow Report", description = "Fetch an Account Payable Cash Flow Report", operationId = "accountPayableCashFlowReport-fetchReport")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountPayableCashFlowDTO::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Cash Flow Report was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "filterRequest", `in` = ParameterIn.QUERY, required = false)
      @QueryValue("filterRequest")
      filterRequest: CashFlowFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableCashFlowDTO {
      logger.info("Fetching all Account Payable Cash Flow Report Vendor Details {}")

      val user = userService.fetchUser(authentication)
      return accountPayableCashFlowService.fetchReport(user.myCompany(), filterRequest)
   }
}
