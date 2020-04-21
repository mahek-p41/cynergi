package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.AccountService
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.Valid

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounting/account")
class AccountController @Inject constructor(
   private val userService: UserService,
   private val accountService: AccountService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["AccountEndpoints"], summary = "Fetch a single Account", description = "Fetch a single Account by ID", operationId = "account-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountDTO::class))]),
      ApiResponse(responseCode = "404", description = "The requested Account was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @QueryValue("id") id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountDTO {
      logger.info("Fetching Account by ID {}", id)

      val user = userService.findUser(authentication)
      val response = accountService.fetchById(id, user.myCompany(), httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)

      logger.debug("Fetching AuditDetail by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["AccountEndpoints"], summary = "Fetch a list of accounts", description = "Fetch a listing of accounts", operationId = "account-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))])
   ])
   @Get(uri = "{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @Valid @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountDTO> {
      val user = userService.findUser(authentication)
      val accounts = accountService.fetchAll(user.myCompany(), pageRequest, httpRequest.findLocaleWithDefault())

      if (accounts.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of Account Currency Codes resulted in {}", accounts)

      return accounts
   }

   @Post(processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountEndpoints"], summary = "Create a single account", description = "Create a single account.", operationId = "account-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to save Account", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountDTO::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun save(
      @Body accountDTO: AccountDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountDTO {
      logger.info("Requested Save Account {}", accountDTO)

      val user = userService.findUser(authentication)
      val response = accountService.create(accountDTO, user.myCompany(), httpRequest.findLocaleWithDefault())

      logger.debug("Requested Save Account {} resulted in {}", accountDTO, response)

      return response
   }

   @Put(uri = "/{id}" ,processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountEndpoints"], summary = "Create a single account", description = "Create a single account.", operationId = "account-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update Account", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountDTO::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested Account was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @QueryValue("id") id: Long,
      @Body accountDTO: AccountDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountDTO {
      logger.info("Requested Update Account {}", accountDTO)

      val user = userService.findUser(authentication)
      val response = accountService.update(id, accountDTO, user.myCompany(), httpRequest.findLocaleWithDefault())

      logger.debug("Requested Update Account {} resulted in {}", accountDTO, response)

      return response
   }
}
