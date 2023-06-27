package com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalService
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
@Controller("/api/general-ledger/reversal")
class GeneralLedgerReversalController @Inject constructor(
   private val generalLedgerReversalService: GeneralLedgerReversalService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerReversalEndpoints"], summary = "Fetch a single GeneralLedgerReversal", description = "Fetch a single GeneralLedgerReversal by its system generated primary key", operationId = "generalLedgerReversal-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerReversal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReversalDTO {
      logger.info("Fetching GeneralLedgerReversal by {}", id)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerReversalService.fetchOne(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching GeneralLedgerReversal by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerReversalEndpoints"], summary = "Fetch a listing of GeneralLedgerReversals", description = "Fetch a paginated listing of GeneralLedgerReversals", operationId = "generalLedgerReversal-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested GeneralLedgerReversal was unable to be found, or the result is empty"),
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
   ): Page<GeneralLedgerReversalDTO> {
      logger.info("Fetching all GeneralLedgerReversals {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = generalLedgerReversalService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerReversalEndpoints"], summary = "Create a single GeneralLedgerReversal", description = "Create a single GeneralLedgerReversal", operationId = "generalLedgerReversal-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The GeneralLedgerReversal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerReversalDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReversalDTO {
      logger.debug("Requested Create GeneralLedgerReversal {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerReversalService.create(dto, user.myCompany())

      logger.debug("Requested Create GeneralLedgerReversal {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerReversalEndpoints"], summary = "Update a single GeneralLedgerReversal", description = "Update a single GeneralLedgerReversal", operationId = "generalLedgerReversal-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerReversal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the GeneralLedgerReversal being updated") @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: GeneralLedgerReversalDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReversalDTO {
      logger.info("Requested Update GeneralLedgerReversal {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerReversalService.update(id, dto, user.myCompany())

      logger.debug("Requested Update GeneralLedgerReversal {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["GeneralLedgerReversalEndpoints"], summary = "Delete a single GeneralLedgerReversal", description = "Delete a single GeneralLedgerReversal", operationId = "generalLedgerReversal-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If GeneralLedgerReversal was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerReversal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete GeneralLedgerReversal", authentication)

      val user = userService.fetchUser(authentication)

      return generalLedgerReversalService.delete(id, user.myCompany())
   }
}
