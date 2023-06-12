package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.*
import com.cynergisuite.middleware.accounting.account.VendorBalanceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceListByVendorDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceReportTemplate
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.server.types.files.StreamedFile
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
import java.io.ByteArrayInputStream
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/account-payable/invoice")
class AccountPayableInvoiceController @Inject constructor(
   private val accountPayableInvoiceService: AccountPayableInvoiceService,
   private val accountPayableCheckPreviewService: AccountPayableCheckPreviewService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Fetch a single Account Payable Invoice", description = "Fetch a single Account Payable Invoice by its system generated primary key", operationId = "accountPayableInvoice-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableInvoiceDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableInvoiceDTO {
      logger.info("Fetching Account Payable Invoice by {}", id)

      val user = userService.fetchUser(authentication)
      val response = accountPayableInvoiceService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Account Payable Invoice by {} resulted in {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Fetch a listing of Account Payable Invoices", description = "Fetch a paginated listing of Account Payable Invoice", operationId = "accountPayableInvoice-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Invoice was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayableInvoiceDTO> {
      logger.info("Fetching all Account Payable Invoices {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = accountPayableInvoiceService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/list-by-vendor{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(
      tags = ["AccountPayableInvoiceEndpoints"],
      summary = "Fetch all Account Payable Invoices by vendor",
      description = "Fetch all Account Payable Invoices by vendor",
      operationId = "accountPayableInvoice-listByVendor"
   )
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Invoice was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByVendor(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: AccountPayableInvoiceListByVendorFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayableInvoiceListByVendorDTO> {
      logger.info("Fetching Account Payable Invoices {}", filterRequest)

      val user = userService.fetchUser(authentication)

      return accountPayableInvoiceService.fetchAllByVendor(user.myCompany(), filterRequest)
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/report{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(
      tags = ["AccountPayableInvoiceEndpoints"],
      summary = "Fetch an Account Payable Invoices Report",
      description = "Fetch an Account Payable Invoices Report",
      operationId = "accountPayableInvoice-fetchReport"
   )
   @ApiResponses(
      value = [
         ApiResponse(
            responseCode = "200",
            content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]
         ),
         ApiResponse(
            responseCode = "204",
            description = "The requested Account Payable Invoice Report was unable to be found, or the result is empty"
         ),
         ApiResponse(
            responseCode = "401",
            description = "If the user calling this endpoint does not have permission to operate it"
         ),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: InvoiceReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableInvoiceReportTemplate {
      logger.info("Fetching all Account Payable Invoices {}", filterRequest)

      val user = userService.fetchUser(authentication)
      return accountPayableInvoiceService.fetchReport(user.myCompany(), filterRequest)
   }

   @Throws(NotFoundException::class)
   @Get(uri = "/export{?filterRequest*}")
   @Operation(
      tags = ["AccountPayableInvoiceEndpoints"],
      summary = "Export a listing of AccountPayableInvoices",
      description = "Export a listing of AccountPayableInvoices to a file",
      operationId = "accountPayableInvoice-export"
   )
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200"),
         ApiResponse(responseCode = "204", description = "The requested accountPayableInvoice was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun export(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @QueryValue("filterRequest")
      filterRequest: InvoiceReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): StreamedFile {
      logger.info("Exporting all accountPayableInvoices {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val byteArray = accountPayableInvoiceService.export(filterRequest, user.myCompany())
      return StreamedFile(ByteArrayInputStream(byteArray), MediaType.ALL_TYPE).attach("AP Invoice Report Export.csv")
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/inquiry{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(
      tags = ["AccountPayableInvoiceEndpoints"],
      summary = "Fetch an Account Payable Invoice Inquiry",
      description = "Fetch an Account Payable Invoice Inquiry",
      operationId = "accountPayableInvoice-inquiry"
   )
   @ApiResponses(
      value = [
         ApiResponse(
            responseCode = "200",
            content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]
         ),
         ApiResponse(
            responseCode = "204",
            description = "The requested Account Payable Invoice Inquiry was unable to be found, or the result is empty"
         ),
         ApiResponse(
            responseCode = "401",
            description = "If the user calling this endpoint does not have permission to operate it"
         ),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun inquiry(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: AccountPayableInvoiceInquiryFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayableInvoiceInquiryDTO> {
      logger.info("Fetching Account Payable Invoice Inquiry {}", filterRequest)

      val user = userService.fetchUser(authentication)

      return accountPayableInvoiceService.inquiry(user.myCompany(), filterRequest)
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Create a single Account Payable Invoice", description = "Create a single AccountPayableInvoice", operationId = "accountPayableInvoice-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableInvoiceDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The Account Payable Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: AccountPayableInvoiceDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableInvoiceDTO {
      logger.debug("Requested Create Account Payable Invoice {}", dto)

      val user = userService.fetchUser(authentication)
      val response = accountPayableInvoiceService.create(dto, user.myCompany())

      logger.debug("Requested Create Account Payable Invoice {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Update a single Account Payable Invoice", description = "Update a single Account Payable Invoice", operationId = "accountPayableInvoice-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableInvoiceDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the Account Payable Invoice being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: AccountPayableInvoiceDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableInvoiceDTO {
      logger.info("Requested Update Account Payable Invoice {}", dto)

      val user = userService.fetchUser(authentication)
      val response = accountPayableInvoiceService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Account Payable Invoice {} resulted in {}", dto, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/check-preview{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Fetch an Account Payable Check Preview Report", description = "Fetch an Account Payable Check Preview Report", operationId = "accountPayableInvoice-checkPreview")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Check Preview Report was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun checkPreview(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @QueryValue("filterRequest")
      filterRequest: AccountPayableCheckPreviewFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableCheckPreviewDTO {
      logger.info("Fetching Account Payable Check Preview Report {}", filterRequest)

      val user = userService.fetchUser(authentication)

      return accountPayableCheckPreviewService.checkPreview(user.myCompany(), filterRequest)
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/vendor-balance{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Fetch an Account Payable Check Preview Report", description = "Fetch an Account Payable Check Preview Report", operationId = "accountPayableInvoice-checkPreview")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Check Preview Report was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun vendorBalance(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @QueryValue("filterRequest")
      filterRequest: AccountPayableVendorBalanceReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorBalanceDTO {
      logger.info("Fetching Account Payable Check Preview Report {}", filterRequest)

      val user = userService.fetchUser(authentication)

      return accountPayableInvoiceService.vendorBalance(user.myCompany(), filterRequest)
   }
}
