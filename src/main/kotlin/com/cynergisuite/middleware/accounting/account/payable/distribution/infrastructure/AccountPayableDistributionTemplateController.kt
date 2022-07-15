package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateService
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateDTO
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
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
@Controller("/api/accounting/account-payable/distribution/template")
class AccountPayableDistributionTemplateController @Inject constructor(
   private val accountPayableDistributionTemplateService: AccountPayableDistributionTemplateService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionTemplateController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableDistributionTemplateEndpoints"], summary = "Fetch a single AccountPayableDistributionTemplateDTO", description = "Fetch a single AccountPayableDistributionTemplateDTO that is associated with the logged-in user's company", operationId = "accountPayableDistributionTemplate-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionTemplateDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableDistributionTemplate was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableDistributionTemplateDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()

      logger.info("Fetching AccountPayableDistributionTemplate by id {}", id)

      val response = accountPayableDistributionTemplateService.fetchOne(id, userCompany) ?: throw NotFoundException(id)

      logger.debug("Fetching AccountPayableDistributionTemplate by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["AccountPayableDistributionTemplateEndpoints"], summary = "Fetch a list of account payable distribution templates", description = "Fetch a listing of account payable distribution templates", operationId = "accountPayableDistributionTemplate-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))])
      ]
   )
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   fun fetchAll(
      @Valid @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayableDistributionTemplateDTO> {
      val user = userService.fetchUser(authentication)
      val apDistributions = accountPayableDistributionTemplateService.fetchAll(user.myCompany(), pageRequest)

      if (apDistributions.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of AccountPayableDistributionTemplates resulted in {}", apDistributions)

      return apDistributions
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableDistributionTemplateEndpoints"], summary = "Create an AccountPayableDistributionTemplateEntity", description = "Create an AccountPayableDistributionTemplateEntity", operationId = "accountPayableDistributionTemplate-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionTemplateDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: AccountPayableDistributionTemplateDTO,
      authentication: Authentication
   ): AccountPayableDistributionTemplateDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create AccountPayableDistributionTemplate {}", dto)

      val response = accountPayableDistributionTemplateService.create(dto, userCompany)

      logger.debug("Requested Create AccountPayableDistributionTemplate {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableDistributionTemplateEndpoints"], summary = "Update an AccountPayableDistributionTemplateEntity", description = "Update an AccountPayableDistributionTemplateEntity from a body of AccountPayableDistributionTemplateDTO", operationId = "accountPayableDistribution-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update AccountPayableDistribution", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionTemplateDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableDistributionTemplate was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      dto: AccountPayableDistributionTemplateDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableDistributionTemplateDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update AccountPayableDistributionTemplate {}", dto)

      val response = accountPayableDistributionTemplateService.update(id, dto, userCompany)

      logger.debug("Requested Update AccountPayableDistributionTemplate {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id:[0-9a-fA-F\\-]+}")
   @AccessControl
   @Throws(NotFoundException::class)
   @Operation(tags = ["AccountPayableDistributionTemplateEndpoints"], summary = "Delete a single AccountPayableDistribution", description = "Delete a single AccountPayableDistribution", operationId = "accountPayableDistribution-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If AccountPayableDistributionTemplate was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableDistributionTemplate was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete AccountPayableDistribution", authentication)

      val user = userService.fetchUser(authentication)

      return accountPayableDistributionTemplateService.delete(id, user.myCompany())
   }
}
