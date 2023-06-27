package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDetailDTO
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionDetailService
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
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
@Controller("/api/accounting/account-payable/distribution/detail")
class AccountPayableDistributionDetailController @Inject constructor(
   private val accountPayableDistributionService: AccountPayableDistributionDetailService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionDetailController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AccountPayableDistributionDetailEndpoints"], summary = "Fetch a single AccountPayableDistributionDetailDTO", description = "Fetch a single AccountPayableDistributionDetailDTO that is associated with the logged-in user's company", operationId = "accountPayableDistributionDetail-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionDetailDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableDistributionDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableDistributionDetailDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()

      logger.info("Fetching AccountPayableDistributionDetail by id {}", id)

      val response = accountPayableDistributionService.fetchOne(id, userCompany) ?: throw NotFoundException(id)

      logger.debug("Fetching AccountPayableDistributionDetail by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["AccountPayableDistributionDetailEndpoints"], summary = "Fetch a list of account payable distributions", description = "Fetch a listing of account payable distributions", operationId = "accountPayableDistribution-fetchAll")
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
   ): Page<AccountPayableDistributionDetailDTO> {
      val user = userService.fetchUser(authentication)
      val apDistributions = accountPayableDistributionService.fetchAll(user.myCompany(), pageRequest)

      if (apDistributions.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of AccountPayableDistributions resulted in {}", apDistributions)

      return apDistributions
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["AccountPayableDistributionDetailEndpoints"], summary = "Fetch a list of account payable distribution details by template id", description = "Fetch a listing of account payable distribution details by template id", operationId = "accountPayableDistribution-fetchAllRecordsByTemplateId")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))])
      ]
   )
   @Get(uri = "/template/{id:[0-9a-fA-F\\-]+}{?pageRequest*}", produces = [APPLICATION_JSON])
   fun fetchAllRecordsByTemplateId(
      @Parameter(name = "id", `in` = PATH, description = "ID of the template") @QueryValue("id") id: UUID,
      @Valid @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayableDistributionDetailDTO> {
      val user = userService.fetchUser(authentication)
      val apDistributions = accountPayableDistributionService.fetchAllRecordsByTemplateId(user.myCompany(), id, pageRequest)

      if (apDistributions.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of AccountPayableDistributions for group name={} resulted in {}", id, apDistributions)

      return apDistributions
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableDistributionDetailEndpoints"], summary = "Create an AccountPayableDistributionTemplateEntity", description = "Create an AccountPayableDistributionTemplateEntity", operationId = "accountPayableDistribution-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: AccountPayableDistributionDetailDTO,
      authentication: Authentication
   ): AccountPayableDistributionDetailDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create AccountPayableDistributionDetail {}", dto)

      val response = accountPayableDistributionService.create(dto, userCompany)

      logger.debug("Requested Create AccountPayableDistributionDetail {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableDistributionDetailEndpoints"], summary = "Update an AccountPayableDistributionTemplateEntity", description = "Update an AccountPayableDistributionTemplateEntity from a body of AccountPayableDistributionDTO", operationId = "accountPayableDistribution-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update AccountPayableDistribution", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableDistributionDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      dto: AccountPayableDistributionDetailDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountPayableDistributionDetailDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update AccountPayableDistributionDetail {}", dto)

      val response = accountPayableDistributionService.update(id, dto, userCompany)

      logger.debug("Requested Update AccountPayableDistributionDetail {} resulted in {}", dto, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AccountPayableDistributionDetailEndpoints"], summary = "Update multiple AccountPayableDistributionTemplateEntity", description = "Update multiple AccountPayableDistributionTemplateEntity from a body of AccountPayableDistributionDTO", operationId = "accountPayableDistribution-bulkUpdate")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update AccountPayableDistribution", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayableDistributionDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableDistributionDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Body @Valid
      dto: List<AccountPayableDistributionDetailDTO>,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<AccountPayableDistributionDetailDTO> {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update AccountPayableDistributionDetail {}", dto)

      val response = accountPayableDistributionService.update(dto, userCompany)

      logger.debug("Requested Update AccountPayableDistributionDetail {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id:[0-9a-fA-F\\-]+}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["AccountPayableDistributionDetailEndpoints"], summary = "Delete a single AccountPayableDistribution", description = "Delete a single AccountPayableDistribution", operationId = "accountPayableDistribution-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If AccountPayableDistributionDetail was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested AccountPayableDistributionDetail was unable to be found"),
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

      return accountPayableDistributionService.delete(id, user.myCompany())
   }
}
