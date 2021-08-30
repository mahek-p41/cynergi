package com.cynergisuite.middleware.accounting.account.payable.aging.infrastructure

import com.cynergisuite.domain.AgingReportFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.aging.AccountPayableAgingReportDTO
import com.cynergisuite.middleware.accounting.account.payable.aging.AccountPayableAgingReportService
import com.cynergisuite.middleware.accounting.account.payable.aging.AgingReportVendorDetailDTO
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/account-payable/aging")
class AccountPayableAgingReportController @Inject constructor(
   private val accountPayableAgingReportService: AccountPayableAgingReportService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableAgingReportController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableAgingReportEndpoints"], summary = "Fetch a single Vendor Detail", description = "Fetch a single Vendor Detail for the Account Payable Aging Report by its system generated primary key", operationId = "accountPayableAgingReport-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AgingReportVendorDetailDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Vendor Detail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: AgingReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AgingReportVendorDetailDTO {
      logger.info("Fetching Account Payable Aging Report Vendor Detail by {}", filterRequest)

      val user = userService.fetchUser(authentication)
      return accountPayableAgingReportService.fetchOne(user.myCompany(), filterRequest.vendors!!, filterRequest.agingDate!!)!!
   }

   @Get(uri = "{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableAgingReportEndpoints"], summary = "Fetch an Account Payable Aging Report", description = "Fetch an Account Payable Aging Report", operationId = "accountPayableAgingReport-fetchReport")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableAgingReportDTO::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Aging Report was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: AgingReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableAgingReportDTO {
      logger.info("Fetching all Account Payable Aging Report Vendor Details {}")

      val user = userService.fetchUser(authentication)
      return accountPayableAgingReportService.fetchReport(user.myCompany(), filterRequest)
   }
}
