package com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure

import com.cynergisuite.domain.AccountPayableListPaymentsFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PaymentReportFilterRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentReportTemplate
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentService
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@AreaControl("AP")
@Controller("/api/accounting/account-payable/payment")
class AccountPayablePaymentController @Inject constructor(
   private val accountPayablePaymentService: AccountPayablePaymentService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayablePaymentController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayablePaymentEndpoints"], summary = "Fetch a single Account Payable Payment", description = "Fetch a single Account Payable Payment by its system generated primary key", operationId = "accountPayablePayment-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayablePaymentDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Payment was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayablePaymentDTO {
      logger.info("Fetching Account Payable Payment by {}", id)

      val user = userService.fetchUser(authentication)
      val response = accountPayablePaymentService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Account Payable Payment by {} resulted in", id, response)

      return response
   }

   @Get(uri = "{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayablePaymentEndpoints"], summary = "Fetch a Account Payable Payments Report", description = "Fetch a Account Payable Payments Report", operationId = "accountPayablePayment-fetchReport")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayablePaymentReportTemplate::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Payment was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: PaymentReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayablePaymentReportTemplate {
      logger.info("Fetching all Account Payable Payments {}")

      val user = userService.fetchUser(authentication)
      return accountPayablePaymentService.fetchReport(user.myCompany(), filterRequest)
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["AccountPayablePaymentEndpoints"], summary = "Fetch a Listing of Account Payable Payments", description = "Fetch a listing of account payable payments", operationId = "accountPayablePayment-fetchPaymentsListing")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))])
      ]
   )
   @Get(uri = "/pmtlist{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   fun fetchPaymentsListing(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: AccountPayableListPaymentsFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayablePaymentDTO> {
      val user = userService.fetchUser(authentication)
      val paymentListing = accountPayablePaymentService.fetchPaymentsListing(user.myCompany(), pageRequest, httpRequest.findLocaleWithDefault())

      if (paymentListing.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of Account Payable Payments resulted in {}", paymentListing)

      return paymentListing
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayablePaymentEndpoints"], summary = "Create a single Account Payable Payment", description = "Create a single AccountPayablePayment", operationId = "accountPayablePayment-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayablePaymentDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The Account Payable Payment was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: AccountPayablePaymentDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayablePaymentDTO {
      logger.debug("Requested Create Account Payable Payment {}", dto)

      val user = userService.fetchUser(authentication)
      val response = accountPayablePaymentService.create(dto, user.myCompany())

      logger.debug("Requested Create Account Payable Payment {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayablePaymentEndpoints"], summary = "Update a single Account Payable Payment", description = "Update a single Account Payable Payment", operationId = "accountPayablePayment-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayablePaymentDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Payment was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the Account Payable Payment being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: AccountPayablePaymentDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayablePaymentDTO {
      logger.info("Requested Update Account Payable Payment {}", dto)

      val user = userService.fetchUser(authentication)
      val response = accountPayablePaymentService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Account Payable Payment {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["AccountPayablePaymentEndpoints"], summary = "Delete a single AccountPayablePayment", description = "Delete a single Account Payable Payment", operationId = "accountPayablePayment-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If SourceCode was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested AccountPayablePayment was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete Account Payable Payment Detail", authentication)

      val user = userService.fetchUser(authentication)

      return accountPayablePaymentService.delete(id, user.myCompany())
   }
}
