package com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure

import com.cynergisuite.domain.AccountPayableInvoiceListByVendorFilterRequest
import com.cynergisuite.domain.InvoiceReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.recurring.AccountPayableRecurringInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.recurring.AccountPayableRecurringInvoiceReportTemplate
import com.cynergisuite.middleware.accounting.account.payable.recurring.AccountPayableRecurringInvoiceService
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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

@Secured(IS_AUTHENTICATED, "APRECUR")
@AreaControl("AP")
@Controller("/api/accounting/account-payable/recurring")
class AccountPayableRecurringInvoiceController @Inject constructor(
   private val accountPayableRecurringInvoiceService: AccountPayableRecurringInvoiceService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableRecurringInvoiceController::class.java)

   @Secured("APRECURLST")
   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableRecurringInvoiceEndpoints"], summary = "Fetch a single Account Payable Recurring Invoice", description = "Fetch a single Account Payable Recurring Invoice by it's system generated primary key", operationId = "accountPayableRecurringInvoice-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableRecurringInvoiceDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Recurring Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableRecurringInvoiceDTO {
      logger.info("Fetching Account Payable Recurring Invoice by {}", id)

      val user = userService.fetchUser(authentication)
      val response = accountPayableRecurringInvoiceService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Account Payable Recurring Invoice by {} resulted in", id, response)

      return response
   }

   @Secured("APRECURLST")
   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableRecurringInvoiceEndpoints"], summary = "Fetch a listing of Account Payable Recurring Invoices", description = "Fetch a paginated listing of Account Payable Recurring Invoice", operationId = "accountPayableRecurringInvoice-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Recurring Invoice was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      filterRequest: AccountPayableInvoiceListByVendorFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayableRecurringInvoiceDTO> {
      logger.info("Fetching all Account Payable Recurring Invoices {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val page = accountPayableRecurringInvoiceService.fetchAll(user.myCompany(), filterRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = filterRequest)
      }

      return page
   }

   @Secured("APRECURLST")
   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/report{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableRecurringInvoiceEndpoints"], summary = "Fetch a listing of Account Payable Recurring Invoices", description = "Fetch a paginated listing of Account Payable Recurring Invoice", operationId = "accountPayableRecurringInvoice-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Recurring Invoice was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: InvoiceReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableRecurringInvoiceReportTemplate {
      logger.info("Fetching all Account Payable Recurring Invoices {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val page = accountPayableRecurringInvoiceService.fetchReport(user.myCompany(), filterRequest)

      return page
   }

   @Secured("APRECURADD")
   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableRecurringInvoiceEndpoints"], summary = "Create a single Account Payable Recurring Invoice", description = "Create a single AccountPayableRecurringInvoice", operationId = "accountPayableRecurringInvoice-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableRecurringInvoiceDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The Account Payable Recurring Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: AccountPayableRecurringInvoiceDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableRecurringInvoiceDTO {
      logger.debug("Requested Create Account Payable Recurring Invoice {}", dto)

      val user = userService.fetchUser(authentication)
      val response = accountPayableRecurringInvoiceService.create(dto, user.myCompany())

      logger.debug("Requested Create Account Payable Recurring Invoice {} resulted in {}", dto, response)

      return response
   }

   @Secured("APRECURCHG")
   @Put(value = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableRecurringInvoiceEndpoints"], summary = "Update a single Account Payable Recurring Invoice", description = "Update a single Account Payable Recurring Invoice", operationId = "accountPayableRecurringInvoice-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableRecurringInvoiceDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Recurring Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the Account Payable Recurring Invoice being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: AccountPayableRecurringInvoiceDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableRecurringInvoiceDTO {
      logger.info("Requested Update Account Payable Recurring Invoice {}", dto)

      val user = userService.fetchUser(authentication)
      val response = accountPayableRecurringInvoiceService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Account Payable Recurring Invoice {} resulted in {}", dto, response)

      return response
   }

   @Secured("APRECURCHG")
   @Post(value = "/transfer", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableRecurringInvoiceEndpoints"], summary = "Update a single Account Payable Recurring Invoice", description = "Update a single Account Payable Recurring Invoice", operationId = "accountPayableRecurringInvoice-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableRecurringInvoiceDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Recurring Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun transfer(
      @Body @Valid
      dto: AccountPayableRecurringInvoiceDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableInvoiceDTO {
      logger.info("Requested Update Account Payable Recurring Invoice {}", dto)

      val user = userService.fetchUser(authentication)
      val response = accountPayableRecurringInvoiceService.transfer(dto, user)

      logger.debug("Requested Update Account Payable Recurring Invoice {} resulted in {}", dto, response)

      return response
   }
}
