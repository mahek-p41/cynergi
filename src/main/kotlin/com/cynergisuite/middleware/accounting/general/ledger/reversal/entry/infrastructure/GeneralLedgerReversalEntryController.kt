package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.GeneralLedgerReversalEntryDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.GeneralLedgerReversalEntryService
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
@Controller("/api/accounting/general-ledger/reversal/entry")
class GeneralLedgerReversalEntryController @Inject constructor(
   private val generalLedgerReversalEntryService: GeneralLedgerReversalEntryService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalEntryController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerReversalEntryEndpoints"], summary = "Fetch a single General Ledger Reversal Entry", description = "Fetch a single General Ledger Reversal Entry by its system generated primary key", operationId = "generalLedgerReversalEntry-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalEntryDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Reversal Entry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReversalEntryDTO {
      logger.info("Fetching General Ledger Reversal Entry by General Ledger Reversal {}", id)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerReversalEntryService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching General Ledger Reversal Entry by General Ledger Reversal {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerReversalEntryEndpoints"], summary = "Fetch a listing of General Ledger Reversal Entries", description = "Fetch a paginated listing of General Ledger Reversal Entries", operationId = "generalLedgerReversalEntry-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Reversal Entry was unable to be found, or the result is empty"),
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
   ): Page<GeneralLedgerReversalEntryDTO> {
      logger.info("Fetching all General Ledger Reversal Entries {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = generalLedgerReversalEntryService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerReversalEntryEndpoints"], summary = "Create a single General Ledger Reversal Entry", description = "Create a single GeneralLedgerReversalEntry", operationId = "generalLedgerReversalEntry-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalEntryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The General Ledger Reversal Entry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerReversalEntryDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReversalEntryDTO {
      logger.debug("Requested Create General Ledger Reversal Entry {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerReversalEntryService.create(dto, user.myCompany())

      logger.debug("Requested Create General Ledger Reversal Entry {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerReversalEntryEndpoints"], summary = "Update a single General Ledger Reversal Entry", description = "Update a single General Ledger Reversal Entry", operationId = "generalLedgerReversalEntry-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalEntryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Reversal Entry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The General Ledger Reversal id for the General Ledger Reversal Entry being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: GeneralLedgerReversalEntryDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerReversalEntryDTO {
      logger.info("Requested Update General Ledger Reversal Entry {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerReversalEntryService.update(id, dto, user.myCompany())

      logger.debug("Requested Update General Ledger Reversal Entry {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id:[0-9a-fA-F\\-]+}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["GeneralLedgerReversalEntryEndpoints"], summary = "Delete a single GeneralLedgerReversalEntry", description = "Delete a single GeneralLedgerReversalEntry", operationId = "generalLedgerReversalEntry-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If GeneralLedgerReversalEntry was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerReversalEntry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete GeneralLedgerReversalEntry", authentication)

      val user = userService.fetchUser(authentication)

      return generalLedgerReversalEntryService.delete(id, user.myCompany())
   }
}
