package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDTO
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionService
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
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/account-payable/distribution")
class AccountPayableDistributionController @Inject constructor(
   private val accountPayableDistributionService: AccountPayableDistributionService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableDistributionEndpoints"], summary = "Fetch a single AccountPayableDistributionDTO", description = "Fetch a single AccountPayableDistributionDTO that is associated with the logged-in user's company", operationId = "accountPayableDistribution-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableDistribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableDistributionDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()

      logger.info("Fetching AccountPayableDistribution by id {}", id)

      val response = accountPayableDistributionService.fetchOne(id, userCompany) ?: throw NotFoundException("Account payable distribution")

      logger.debug("Fetching AccountPayableDistribution by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["AccountPayableDistributionEndpoints"], summary = "Fetch a list of account payable distributions", description = "Fetch a listing of account payable distributions", operationId = "accountPayableDistribution-fetchAll")
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
   ): Page<AccountPayableDistributionDTO> {
      val user = userService.findUser(authentication)
      val apDistributions = accountPayableDistributionService.fetchAll(user.myCompany(), pageRequest)

      if (apDistributions.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of AccountPayableDistributions resulted in {}", apDistributions)

      return apDistributions
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableDistributionEndpoints"], summary = "Create an AccountPayableDistributionEntity", description = "Create an AccountPayableDistributionEntity", operationId = "accountPayableDistribution-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: AccountPayableDistributionDTO,
      authentication: Authentication
   ): AccountPayableDistributionDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create AccountPayableDistribution {}", dto)

      val response = accountPayableDistributionService.create(dto, userCompany)

      logger.debug("Requested Create AccountPayableDistribution {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableDistributionEndpoints"], summary = "Update an AccountPayableDistributionEntity", description = "Update an AccountPayableDistributionEntity from a body of AccountPayableDistributionDTO", operationId = "accountPayableDistribution-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update AccountPayableDistribution", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableDistribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: Long,
      @Body @Valid
      dto: AccountPayableDistributionDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableDistributionDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update AccountPayableDistribution {}", dto)

      val response = accountPayableDistributionService.update(id, dto, userCompany)

      logger.debug("Requested Update AccountPayableDistribution {} resulted in {}", dto, response)

      return response
   }
}
