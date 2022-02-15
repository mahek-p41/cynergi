package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceService
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import jakarta.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/account-payable/invoice")
class AccountPayableInvoiceController @Inject constructor(
   private val accountPayableInvoiceService: AccountPayableInvoiceService,
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
      @Valid @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableInvoiceDTO {
      logger.info("Fetching Account Payable Invoice by {}", id)

      val user = userService.fetchUser(authentication)
      val response = accountPayableInvoiceService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Account Payable Invoice by {} resulted in", id, response)

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
}
