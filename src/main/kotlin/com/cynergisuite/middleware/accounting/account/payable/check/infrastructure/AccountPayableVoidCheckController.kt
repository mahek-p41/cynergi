package com.cynergisuite.middleware.accounting.account.payable.check.infrastructure

import com.cynergisuite.middleware.accounting.account.payable.check.AccountPayableVoidCheckDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceService
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceController
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
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
@AreaControl("AP")
@Controller("/api/accounting/account-payable/check")
class AccountPayableVoidCheckController @Inject constructor(
   private val accountPayableInvoiceService: AccountPayableInvoiceService,
   private val accountPayableCheckPreviewService: AccountPayableCheckPreviewService,
   private val userService: UserService,
   private val employeeService: EmployeeService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceController::class.java)

   @Secured("APPREVUE")
   @Get(uri = "/void{?filterRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableVoidCheckEndpoints"], summary = "Fetch an Account Payable Check", description = "Fetch an Account Payable Check to void", operationId = "accountPayableCheck-fetchVoid")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountPayableInvoiceDTO::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Check was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchVoidCheck(
      @Parameter(name = "filterRequest", `in` = ParameterIn.QUERY, required = false)
      @QueryValue("filterRequest")
      filterRequest: AccountPayableVoidCheckFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableVoidCheckDTO {
      logger.info("Fetching Account Payable Check to void {}", filterRequest)

      val user = userService.fetchUser(authentication)

     return accountPayableCheckPreviewService.fetchVoidCheck(user.myCompany(), filterRequest)
   }

   @Secured("APPREVUE")
   @Post(uri = "/void", produces = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableVoidCheckEndpoints"], summary = "Void an Account Payable Check", description = "Void an Account Payable Check", operationId = "accountPayableCheck-void")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountPayableInvoiceDTO::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Check was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun voidCheck(
      @Body @Valid
      dto: AccountPayableVoidCheckDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Voiding Account Payable Check {}", dto)

      val user = userService.fetchUser(authentication)

      accountPayableCheckPreviewService.voidCheck(user.myCompany(), dto)
   }
}
