package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.infrastructure

import com.cynergisuite.domain.GeneralLedgerRecurringEntriesFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailService
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.GeneralLedgerRecurringEntriesDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.GeneralLedgerRecurringEntriesService
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

@Secured(IS_AUTHENTICATED, "GL")
@AreaControl("GL")
@Controller("/api/accounting/general-ledger/recurring/entries")
class GeneralLedgerRecurringEntriesController @Inject constructor(
   private val generalLedgerDetailService: GeneralLedgerDetailService,
   private val generalLedgerRecurringEntriesService: GeneralLedgerRecurringEntriesService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringEntriesController::class.java)

   @Secured("GLRECURLST")
   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringEntriesEndpoints"], summary = "Fetch a single General Ledger Recurring Entry", description = "Fetch a single General Ledger Recurring Entry by its system generated primary key", operationId = "generalLedgerRecurringEntries-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringEntriesDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Recurring Entry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerRecurringEntriesDTO {
      logger.info("Fetching General Ledger Recurring Entry by General Ledger Recurring {}", id)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerRecurringEntriesService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching General Ledger Recurring Entry by General Ledger Recurring {} resulted in", id, response)

      return response
   }

   @Secured("GLRECURSHO")
   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringEntriesEndpoints"], summary = "Fetch a listing of General Ledger Recurring Entries", description = "Fetch a paginated listing of General Ledger Recurring Entries", operationId = "generalLedgerRecurringEntries-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Recurring Entry was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: GeneralLedgerRecurringEntriesFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<GeneralLedgerRecurringEntriesDTO> {
      logger.info("Fetching all General Ledger Recurring Entries {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val page = generalLedgerRecurringEntriesService.fetchAll(user.myCompany(), filterRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = filterRequest)
      }

      return page
   }

   @Secured("GLRECURPRT")
   @Get(uri = "report{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringEntriesEndpoints"], summary = "Fetch a General Ledger Recurring Entries report", description = "Fetch a General Ledger Recurring Entries report", operationId = "generalLedgerRecurringEntries-fetchReport")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringEntriesDTO::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Recurring Entry was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: GeneralLedgerRecurringEntriesFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<GeneralLedgerRecurringEntriesDTO> {
      logger.info("Fetching all General Ledger Recurring Entries with report criteria {}", filterRequest)

      val user = userService.fetchUser(authentication)
      return generalLedgerRecurringEntriesService.fetchReport(user.myCompany(), filterRequest)
   }

   @Secured("GLRECURADD")
   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerRecurringEntriesEndpoints"], summary = "Create a single General Ledger Recurring Entry", description = "Create a single GeneralLedgerRecurringEntries", operationId = "generalLedgerRecurringEntries-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringEntriesDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The General Ledger Recurring Entry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerRecurringEntriesDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerRecurringEntriesDTO {
      logger.debug("Requested Create General Ledger Recurring Entry {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerRecurringEntriesService.create(dto, user.myCompany())

      logger.debug("Requested Create General Ledger Recurring Entry {} resulted in {}", dto, response)

      return response
   }

   @Secured("GLRECURCHG")
   @Put(value = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerRecurringEntriesEndpoints"], summary = "Update a single General Ledger Recurring Entry", description = "Update a single General Ledger Recurring Entry", operationId = "generalLedgerRecurringEntries-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringEntriesDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested General Ledger Recurring Entry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The General Ledger Recurring id for the General Ledger Recurring Entry being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: GeneralLedgerRecurringEntriesDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerRecurringEntriesDTO {
      logger.info("Requested Update General Ledger Recurring Entry {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerRecurringEntriesService.update(id, dto, user.myCompany())

      logger.debug("Requested Update General Ledger Recurring Entry {} resulted in {}", dto, response)

      return response
   }

   @Secured("GLRECURDEL")
   @Delete(value = "/{id:[0-9a-fA-F\\-]+}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["GeneralLedgerRecurringEntriesEndpoints"], summary = "Delete a single GeneralLedgerRecurringEntries", description = "Delete a single GeneralLedgerRecurringEntries", operationId = "generalLedgerRecurringEntries-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If GeneralLedgerRecurringEntries was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerRecurringEntries was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete GeneralLedgerRecurringEntries", authentication)

      val user = userService.fetchUser(authentication)

      return generalLedgerRecurringEntriesService.delete(id, user.myCompany())
   }

   @Secured("GLRECURTRN")
   @Post(uri = "/transfer{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringEntriesEndpoints"], summary = "Use GL Recurring to post journal entries", description = "Fetch a list of General Ledger Recurring Entries to create General Ledger Detail records", operationId = "generalLedgerRecurringEntries-transfer")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerDetailDTO::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Recurring Entry was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun transferMultipleEntries(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: GeneralLedgerRecurringEntriesFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Fetching all General Ledger Recurring Entries that meet the criteria {} to create General Ledger Details", filterRequest)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()
      generalLedgerDetailService.transfer(user, filterRequest, locale)
   }

   @Secured("GLRECURTRN")
   @Post(uri = "/transfer/single", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerRecurringEntriesEndpoints"], summary = "Use GL Recurring to post a single journal entry", description = "Create General Ledger Detail records from a requested General Ledger Recurring Entry", operationId = "generalLedgerRecurringEntry-singletransfer")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerDetailDTO::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Recurring Entry was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun transferSingleEntry(
      @Body @Valid
      dto: GeneralLedgerRecurringEntriesDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Transfer a single General Ledger Recurring Entry to create General Ledger Details", dto)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()
      generalLedgerDetailService.transfer(user, dto, locale)
   }
}
