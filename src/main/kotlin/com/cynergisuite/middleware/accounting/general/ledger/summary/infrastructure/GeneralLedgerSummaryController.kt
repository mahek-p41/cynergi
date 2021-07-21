package com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryService
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
import java.util.UUID
import javax.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/general-ledger/summary")
class GeneralLedgerSummaryController @Inject constructor(
   private val generalLedgerSummaryService: GeneralLedgerSummaryService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSummaryController::class.java)

   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerSummaryEndpoints"], summary = "Fetch a single GeneralLedgerSummaryDTO", description = "Fetch a single GeneralLedgerSummaryDTO that is associated with the logged-in user's company", operationId = "generalLedgerSummary-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerSummaryDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerSummary was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Valid @QueryValue("id")
      id: UUID,
      authentication: Authentication
   ): GeneralLedgerSummaryDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Fetching GeneralLedgerSummary by ID {}", id)

      val response = generalLedgerSummaryService.fetchOne(id, userCompany) ?: throw NotFoundException(id)

      logger.debug("Fetching GeneralLedgerSummary by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerSummaryEndpoints"], summary = "Fetch a listing of GeneralLedgerSummaries", description = "Fetch a paginated listing of GeneralLedgerSummaries", operationId = "generalLedgerSummary-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested GeneralLedgerSummary was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Valid @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<GeneralLedgerSummaryDTO> {
      logger.info("Fetching all GeneralLedgerSummaries {}", pageRequest)

      val user = userService.findUser(authentication)
      val page = generalLedgerSummaryService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerSummaryEndpoints"], summary = "Create a GeneralLedgerSummaryEntity", description = "Create an GeneralLedgerSummaryEntity", operationId = "generalLedgerSummary-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerSummaryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerSummaryDTO,
      authentication: Authentication
   ): GeneralLedgerSummaryDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create GeneralLedgerSummary {}", dto)

      val response = generalLedgerSummaryService.create(dto, userCompany)

      logger.debug("Requested Create GeneralLedgerSummary {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerSummaryEndpoints"], summary = "Update a GeneralLedgerSummaryEntity", description = "Update an GeneralLedgerSummaryEntity from a body of GeneralLedgerSummaryDTO", operationId = "generalLedgerSummary-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update GeneralLedgerSummary", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerSummaryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerSummary was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      dto: GeneralLedgerSummaryDTO,
      authentication: Authentication
   ): GeneralLedgerSummaryDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update GeneralLedgerSummary {}", dto)

      val response = generalLedgerSummaryService.update(id, dto, userCompany)

      logger.debug("Requested Update GeneralLedgerSummary {} resulted in {}", dto, response)

      return response
   }
}
