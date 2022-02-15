package com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringService
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
@Controller("/api/accounting/general-ledger/recurring")
class GeneralLedgerRecurringController @Inject constructor(
   private val generalLedgerRecurringService: GeneralLedgerRecurringService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringEndpoints"], summary = "Fetch a single General Ledger Recurring", description = "Fetch a single General Ledger Recurring by it's system generated primary key", operationId = "generalLedgerRecurring-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Recurring was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerRecurringDTO {
      logger.info("Fetching General Ledger Recurring by {}", id)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerRecurringService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching General Ledger Recurring by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringEndpoints"], summary = "Fetch a listing of General Ledger Recurrings", description = "Fetch a paginated listing of General Ledger Recurring", operationId = "generalLedgerRecurring-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Recurring was unable to be found, or the result is empty"),
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
   ): Page<GeneralLedgerRecurringDTO> {
      logger.info("Fetching all General Ledger Recurrings {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = generalLedgerRecurringService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerRecurringEndpoints"], summary = "Create a single General Ledger Recurring", description = "Create a single GeneralLedgerRecurring", operationId = "generalLedgerRecurring-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The General Ledger Recurring was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerRecurringDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerRecurringDTO {
      logger.debug("Requested Create General Ledger Recurring {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerRecurringService.create(dto, user.myCompany())

      logger.debug("Requested Create General Ledger Recurring {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerRecurringEndpoints"], summary = "Update a single General Ledger Recurring", description = "Update a single General Ledger Recurring", operationId = "generalLedgerRecurring-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Recurring was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the General Ledger Recurring being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: GeneralLedgerRecurringDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerRecurringDTO {
      logger.info("Requested Update General Ledger Recurring {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerRecurringService.update(id, dto, user.myCompany())

      logger.debug("Requested Update General Ledger Recurring {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["GeneralLedgerRecurringEndpoints"], summary = "Delete a single GeneralLedgerRecurring", description = "Delete a single GeneralLedgerRecurring", operationId = "generalLedgerRecurring-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If GeneralLedgerRecurring was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerRecurring was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete GeneralLedgerRecurring", authentication)

      val user = userService.fetchUser(authentication)

      return generalLedgerRecurringService.delete(id, user.myCompany())
   }
}
