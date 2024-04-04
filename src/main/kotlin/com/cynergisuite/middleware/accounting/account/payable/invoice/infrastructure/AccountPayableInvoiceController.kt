package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.*
import com.cynergisuite.middleware.accounting.account.VendorBalanceDTO
import com.cynergisuite.middleware.accounting.account.payable.expense.AccountPayableExpenseReportTemplate
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewService
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableDistDetailReportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDistributionDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryPaymentDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceListByVendorDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceMaintenanceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceReportTemplate
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceScheduleDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceScheduleEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceService
import com.cynergisuite.middleware.accounting.account.payable.invoice.InvoiceScheduleDTO
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.employee.EmployeeValueObject
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
@AreaControl("AP")
@Controller("/api/accounting/account-payable/invoice")
class AccountPayableInvoiceController @Inject constructor(
   private val accountPayableInvoiceService: AccountPayableInvoiceService,
   private val accountPayableCheckPreviewService: AccountPayableCheckPreviewService,
   private val userService: UserService,
   private val employeeService: EmployeeService
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
      filterRequest: AccountPayableInvoiceFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayableInvoiceDTO> {
      logger.info("Fetching all Account Payable Invoices {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val page = accountPayableInvoiceService.fetchAll(user.myCompany(), filterRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = filterRequest)
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
   @Get(uri = "/open-by-vendor{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(
      tags = ["AccountPayableInvoiceEndpoints"],
      summary = "Fetch Open Account Payable Invoices by vendor",
      description = "Fetch Open Account Payable Invoices by vendor",
      operationId = "accountPayableInvoice-openByVendor"
   )
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Invoices were unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOpenByVendor(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: AccountPayableInvoiceListByVendorFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayableInvoiceListByVendorDTO> {
      logger.info("Fetching Open Account Payable Invoices By Vendor {}", filterRequest)

      val user = userService.fetchUser(authentication)

      return accountPayableInvoiceService.fetchOpenByVendor(user.myCompany(), filterRequest)
   }

   @Secured("APRPT")
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

   @Secured("APEXPENS")
   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/expense/report{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(
      tags = ["AccountPayableInvoiceEndpoints"],
      summary = "Fetch an Account Payable Expense Report",
      description = "Fetch an Account Payable Expense Report",
      operationId = "accountPayableInvoice-fetchExpenseReport"
   )
   @ApiResponses(
      value = [
         ApiResponse(
            responseCode = "200",
            content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]
         ),
         ApiResponse(
            responseCode = "204",
            description = "The requested Account Payable Expense Report was unable to be found, or the result is empty"
         ),
         ApiResponse(
            responseCode = "401",
            description = "If the user calling this endpoint does not have permission to operate it"
         ),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchExpenseReport(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: ExpenseReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableExpenseReportTemplate {
      logger.info("Fetching AP Expense Report  {}", filterRequest)

      val user = userService.fetchUser(authentication)
      return accountPayableInvoiceService.fetchExpenseReport(user.myCompany(), filterRequest)
   }

   @Secured("APRPT")
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

   @Secured("APEXPENS")
   @Throws(NotFoundException::class)
   @Get(uri = "expense/export{?filterRequest*}")
   @Operation(
      tags = ["AccountPayableInvoiceEndpoints"],
      summary = "Export a listing of Account Payable Expense Invoices",
      description = "Export a listing of Account Payable Expense Invoices to a file",
      operationId = "accountPayableInvoice-exportExpenseInvoices"
   )
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun exportExpenseInvoices(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @QueryValue("filterRequest")
      filterRequest: ExpenseReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): StreamedFile {
      logger.info("Exporting a listing of Account Payable Expense Invoices {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val byteArray = accountPayableInvoiceService.exportExpenseInvoices(filterRequest, user.myCompany())
      return StreamedFile(ByteArrayInputStream(byteArray), MediaType.ALL_TYPE).attach("AP Invoice Report Export.csv")
   }

   @Secured("APSHO")
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

   @Secured("APADD")
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
      if (dto.employee == null) {
         dto.employee = EmployeeValueObject(employeeService.fetchOne(user.myId(), user.myCompany()))
      }
      val response = accountPayableInvoiceService.create(dto, user.myCompany())

      logger.debug("Requested Create Account Payable Invoice {} resulted in {}", dto, response)

      return response
   }

   @Secured("APCHG")
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

   @Secured("APPREVUE")
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

   @Secured("APTRLBAL")
   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/vendor-balance{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Fetch an Account Payable Vendor Balance Report", description = "Fetch an Account Payable Vendor Balance Report", operationId = "accountPayableInvoice-vendorBalance")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Vendor Balance Report was unable to be found, or the result is empty"),
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
   ): List<VendorBalanceDTO> {
      logger.info("Fetching Account Payable Vendor Balance Report {}", filterRequest)

      val user = userService.fetchUser(authentication)

      return accountPayableInvoiceService.vendorBalance(user.myCompany(), filterRequest)
   }

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}/payment", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Fetch an AP Invoice Payments", description = "Fetch an AP Invoice Payments", operationId = "accountPayableInvoice-fetchInvoicePayments")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableInvoiceInquiryPaymentDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchInvoicePayments(
      @QueryValue("id")
      invoiceId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<AccountPayableInvoiceInquiryPaymentDTO> {
      logger.info("Fetching Account Payable Invoice Payments by {}", invoiceId)

      val user = userService.fetchUser(authentication)
      val response = accountPayableInvoiceService.fetchInvoicePayments(invoiceId, user.myCompany())

      logger.debug("Fetching Account Payable Invoice Payments by {} resulted in {}", invoiceId, response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}/gl-distribution", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Fetch an AP Invoice GL Distributions", description = "Fetch an AP Invoice GL Distributions", operationId = "accountPayableInvoice-fetchGLDistributions")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistDetailReportDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchGLDistributions(
      @QueryValue("id")
      invoiceId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<AccountPayableInvoiceDistributionDTO> {
      logger.info("Fetching GL Distributions by {}", invoiceId)

      val user = userService.fetchUser(authentication)
      val response = accountPayableInvoiceService.fetchGLDistributions(invoiceId, user.myCompany())

      logger.debug("Fetching GL Distributions by {} resulted in {}", invoiceId, response)

      return response
   }

   @Secured("APADD")
   @Post(value = "/maintenance", processes = [APPLICATION_JSON])
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
   fun maintenance(
      @Body @Valid
      dto: AccountPayableInvoiceMaintenanceDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableInvoiceMaintenanceDTO {
      logger.debug("Requested Create Account Payable Invoice {}", dto)

      val user = userService.fetchUser(authentication)
      dto.apInvoice?.employee = EmployeeValueObject(employeeService.fetchOne(user.myId(), user.myCompany()))
      val response = accountPayableInvoiceService.maintenance(dto, user.myCompany())

      logger.debug("Requested Create Account Payable Invoice {} resulted in {}", dto, response)

      return response
   }

   @Secured("APDEL")
   @Delete(uri = "/{id:[0-9a-fA-F\\-]+}")
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Delete a single account payable invoice", description = "Deletes an account payable invoice based on passed id", operationId = "accountPayableInvoice-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the account payable invoice record was deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling the endpoint does not have permission"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete account payable invoice", authentication)

      val user = userService.fetchUser(authentication)

      return accountPayableInvoiceService.delete(id, user.myCompany())
   }

   @Secured("APADD")
   @Post(value = "/maintenance/schedule", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Create an Account Payable Invoice Schedule", description = "Create an AccountPayableInvoiceSchedule", operationId = "AccountPayableInvoiceSchedule-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableInvoiceScheduleDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The Account Payable Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun schedule(
      @Body @Valid
      dto: InvoiceScheduleDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<AccountPayableInvoiceScheduleDTO> {
      logger.debug("Requested Create Account Payable Invoice Schedule {}", dto)

      val user = userService.fetchUser(authentication)
      val response = accountPayableInvoiceService.createSchedule(dto, user.myCompany())

      logger.debug("Requested Create Account Payable Invoice Schedule {} resulted in {}", dto, response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}/schedules", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableInvoiceEndpoints"], summary = "Fetch a list of AP Invoice Schedules", description = "Fetch a list of AP Invoice Schedules", operationId = "accountPayableInvoice-fetchSchedules")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistDetailReportDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchSchedules(
      @QueryValue("id")
      invoiceId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<AccountPayableInvoiceScheduleDTO> {
      logger.info("Fetching GL Distributions by {}", invoiceId)

      val user = userService.fetchUser(authentication)
      val response = accountPayableInvoiceService.fetchSchedules(invoiceId, user.myCompany())

      logger.debug("Fetching GL Distributions by {} resulted in {}", invoiceId, response)

      return response
   }
}
