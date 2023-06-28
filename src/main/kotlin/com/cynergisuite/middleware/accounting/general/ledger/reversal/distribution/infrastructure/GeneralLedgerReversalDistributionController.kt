package com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionService
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
@AreaControl("GL")
@Controller("/api/accounting/general-ledger/reversal/distribution")
class GeneralLedgerReversalDistributionController @Inject constructor(
   private val generalLedgerReversalDistributionService: GeneralLedgerReversalDistributionService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalDistributionController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerReversalDistributionEndpoints"], summary = "Fetch a single General Ledger Reversal Distribution", description = "Fetch a single General Ledger Reversal Distribution by its system generated primary key", operationId = "generalLedgerReversalDistribution-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalDistributionDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Reversal Distribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReversalDistributionDTO {
      logger.info("Fetching General Ledger Reversal Distribution by {}", id)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerReversalDistributionService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching General Ledger Reversal Distribution by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerReversalDistributionEndpoints"], summary = "Fetch a listing of General Ledger Reversal Distributions", description = "Fetch a paginated listing of General Ledger Reversal Distributions", operationId = "generalLedgerReversalDistribution-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Reversal Distribution was unable to be found, or the result is empty"),
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
   ): Page<GeneralLedgerReversalDistributionDTO> {
      logger.info("Fetching all General Ledger Reversal Distributions {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = generalLedgerReversalDistributionService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/reversal-id/{glReversalId:[0-9a-fA-F\\-]+}{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerReversalDistributionEndpoints"], summary = "Fetch a listing of General Ledger Reversal Distributions by General Ledger Reversal ID", description = "Fetch a paginated listing of General Ledger Reversal Distributions by General Ledger Reversal ID", operationId = "generalLedgerReversalDistribution-fetchAllByReversalId")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Reversal Distribution was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByReversalId(
      @Parameter(name = "glReversalId", `in` = PATH, description = "The General Ledger Reversal ID for which the list of General Ledger Reversal Distributions is to be loaded")
      @QueryValue("glReversalId")
      glReversalId: UUID,
      @Parameter(name = "pageRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<GeneralLedgerReversalDistributionDTO> {
      logger.info("Fetching all General Ledger Reversal Distributions {} by General Ledger Reversal ID {}", pageRequest, glReversalId)

      val user = userService.fetchUser(authentication)
      val page = generalLedgerReversalDistributionService.fetchAllByReversalId(glReversalId, user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerReversalDistributionEndpoints"], summary = "Create a single General Ledger Reversal Distribution", description = "Create a single GeneralLedgerReversalDistribution", operationId = "generalLedgerReversalDistribution-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalDistributionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The General Ledger Reversal Distribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerReversalDistributionDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReversalDistributionDTO {
      logger.debug("Requested Create General Ledger Reversal Distribution {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerReversalDistributionService.create(dto, user.myCompany())

      logger.debug("Requested Create General Ledger Reversal Distribution {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerReversalDistributionEndpoints"], summary = "Update a single General Ledger Reversal Distribution", description = "Update a single General Ledger Reversal Distribution", operationId = "generalLedgerReversalDistribution-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalDistributionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Reversal Distribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the General Ledger Reversal Distribution being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: GeneralLedgerReversalDistributionDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReversalDistributionDTO {
      logger.info("Requested Update General Ledger Reversal Distribution {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerReversalDistributionService.update(id, dto, user.myCompany())

      logger.debug("Requested Update General Ledger Reversal Distribution {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id:[0-9a-fA-F\\-]+}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["GeneralLedgerReversalDistributionEndpoints"], summary = "Delete a single GeneralLedgerReversalDistribution", description = "Delete a single GeneralLedgerReversalDistribution", operationId = "generalLedgerReversalDistribution-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If GeneralLedgerReversalDistribution was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerReversalDistribution was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete GeneralLedgerReversalDistribution", authentication)

      val user = userService.fetchUser(authentication)

      return generalLedgerReversalDistributionService.delete(id, user.myCompany())
   }
}
