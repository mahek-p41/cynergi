package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
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
import javax.inject.Inject
import javax.validation.Valid

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounting/general-ledger/recurring/distribution")
class GeneralLedgerRecurringDistributionController @Inject constructor(
   private val generalLedgerRecurringDistributionService: GeneralLedgerRecurringDistributionService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringDistributionController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringDistributionEndpoints"], summary = "Fetch a single General Ledger Recurring Distribution", description = "Fetch a single General Ledger Recurring Distribution by its system generated primary key", operationId = "generalLedgerRecurringDistribution-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringDistributionDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Recurring Distribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Valid @QueryValue("id")
      id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerRecurringDistributionDTO {
      logger.info("Fetching General Ledger Recurring Distribution by {}", id)

      val user = userService.findUser(authentication)
      val response = generalLedgerRecurringDistributionService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching General Ledger Recurring Distribution by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringDistributionEndpoints"], summary = "Fetch a listing of General Ledger Recurring Distributions", description = "Fetch a paginated listing of General Ledger Recurring Distributions", operationId = "generalLedgerRecurringDistribution-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Recurring Distribution was unable to be found, or the result is empty"),
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
   ): Page<GeneralLedgerRecurringDistributionDTO> {
      logger.info("Fetching all General Ledger Recurring Distributions {}", pageRequest)

      val user = userService.findUser(authentication)
      val page = generalLedgerRecurringDistributionService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/recurring-id-{glRecurringId:[0-9]+}{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringDistributionEndpoints"], summary = "Fetch a listing of General Ledger Recurring Distributions by General Ledger Recurring ID", description = "Fetch a paginated listing of General Ledger Recurring Distributions by General Ledger Recurring ID", operationId = "generalLedgerRecurringDistribution-fetchAllByRecurringId")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Recurring Distribution was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByRecurringId(
      @Parameter(name = "glRecurringId", `in` = PATH, description = "The General Ledger Recurring ID for which the list of General Ledger Recurring Distributions is to be loaded")
      @Valid @QueryValue("glRecurringId")
      glRecurringId: Long,
      @Parameter(name = "pageRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<GeneralLedgerRecurringDistributionDTO> {
      logger.info("Fetching all General Ledger Recurring Distributions {} by General Ledger Recurring ID {}", pageRequest, glRecurringId)

      val user = userService.findUser(authentication)
      val page = generalLedgerRecurringDistributionService.fetchAllByRecurringId(glRecurringId, user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerRecurringDistributionEndpoints"], summary = "Create a single General Ledger Recurring Distribution", description = "Create a single GeneralLedgerRecurringDistribution", operationId = "generalLedgerRecurringDistribution-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringDistributionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The General Ledger Recurring Distribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerRecurringDistributionDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerRecurringDistributionDTO {
      logger.debug("Requested Create General Ledger Recurring Distribution {}", dto)

      val user = userService.findUser(authentication)
      val response = generalLedgerRecurringDistributionService.create(dto, user.myCompany())

      logger.debug("Requested Create General Ledger Recurring Distribution {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerRecurringDistributionEndpoints"], summary = "Update a single General Ledger Recurring Distribution", description = "Update a single General Ledger Recurring Distribution", operationId = "generalLedgerRecurringDistribution-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringDistributionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Recurring Distribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the General Ledger Recurring Distribution being updated")
      @QueryValue("id")
      id: Long,
      @Body @Valid
      dto: GeneralLedgerRecurringDistributionDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerRecurringDistributionDTO {
      logger.info("Requested Update General Ledger Recurring Distribution {}", dto)

      val user = userService.findUser(authentication)
      val response = generalLedgerRecurringDistributionService.update(id, dto, user.myCompany())

      logger.debug("Requested Update General Ledger Recurring Distribution {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["GeneralLedgerRecurringDistributionEndpoints"], summary = "Delete a single GeneralLedgerRecurringDistribution", description = "Delete a single GeneralLedgerRecurringDistribution", operationId = "generalLedgerRecurringDistribution-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description="If GeneralLedgerRecurringDistribution was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerRecurringDistribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: Long,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ){
      logger.debug("User {} requested delete GeneralLedgerRecurringDistribution", authentication)

      val user = userService.findUser(authentication)

      return generalLedgerRecurringDistributionService.delete(id, user.myCompany())
   }
}
