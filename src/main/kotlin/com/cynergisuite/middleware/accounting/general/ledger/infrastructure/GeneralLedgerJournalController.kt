package com.cynergisuite.middleware.accounting.general.ledger.infrastructure

import com.cynergisuite.domain.GeneralLedgerJournalExportRequest
import com.cynergisuite.domain.GeneralLedgerJournalFilterRequest
import com.cynergisuite.domain.GeneralLedgerJournalReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerPendingReportTemplate
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.server.types.files.StreamedFile
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
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
import java.io.ByteArrayInputStream

@Secured(IS_AUTHENTICATED)
@Controller("/api/general-ledger/journal")
class GeneralLedgerJournalController @Inject constructor(
   private val generalLedgerJournalService: GeneralLedgerJournalService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerJournalController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerJournalEndpoints"], summary = "Fetch a single GeneralLedgerJournal", description = "Fetch a single GeneralLedgerJournal by its system generated primary key", operationId = "generalLedgerJournal-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerJournalDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerJournal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerJournalDTO {
      logger.info("Fetching GeneralLedgerJournal by {}", id)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerJournalService.fetchOne(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching GeneralLedgerJournal by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerJournalEndpoints"], summary = "Fetch a listing of GeneralLedgerJournals", description = "Fetch a paginated listing of GeneralLedgerJournals", operationId = "generalLedgerJournal-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested GeneralLedgerJournal was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: GeneralLedgerJournalFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<GeneralLedgerJournalDTO> {
      logger.info("Fetching all GeneralLedgerJournals {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val page = generalLedgerJournalService.fetchAll(user.myCompany(), filterRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = filterRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerJournalEndpoints"], summary = "Create a single GeneralLedgerJournal", description = "Create a single GeneralLedgerJournal", operationId = "generalLedgerJournal-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerJournalDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The GeneralLedgerJournal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerJournalDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerJournalDTO {
      logger.debug("Requested Create GeneralLedgerJournal {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerJournalService.create(dto, user.myCompany())

      logger.debug("Requested Create GeneralLedgerJournal {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerJournalEndpoints"], summary = "Update a single GeneralLedgerJournal", description = "Update a single GeneralLedgerJournal", operationId = "generalLedgerJournal-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerJournalDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerJournal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = ParameterIn.PATH, description = "The id for the GeneralLedgerJournal being updated") @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: GeneralLedgerJournalDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerJournalDTO {
      logger.info("Requested Update GeneralLedgerJournal {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerJournalService.update(id, dto, user.myCompany())

      logger.debug("Requested Update GeneralLedgerJournal {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id:[0-9a-fA-F\\-]+}")
   @AccessControl
   @Throws(NotFoundException::class)
   @Operation(tags = ["GeneralLedgerJournalEndpoints"], summary = "Delete a single GeneralLedgerJournal", description = "Delete a single GeneralLedgerJournal", operationId = "GeneralLedgerJournal-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If GeneralLedgerJournal was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerJournal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete GeneralLedgerJournal", authentication)

      val user = userService.fetchUser(authentication)

      return generalLedgerJournalService.delete(id, user.myCompany())
   }

   @Delete(value = "/purge{?filterRequest*}")
   @AccessControl
   @Throws(NotFoundException::class)
   @Operation(tags = ["GeneralLedgerJournalEndpoints"], summary = "Purge a list of GeneralLedgerJournal", description = "Purge a list of GeneralLedgerJournal", operationId = "GeneralLedgerJournal-purge")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If GeneralLedgerJournal was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerJournal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun purge(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: GeneralLedgerJournalFilterRequest,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested purge a list of GeneralLedgerJournal", authentication)

      val user = userService.fetchUser(authentication)

      return generalLedgerJournalService.purge(filterRequest, user.myCompany())
   }

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
      filterRequest: GeneralLedgerJournalFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Fetching all General Ledger Recurring Entries that meet the criteria {} to create General Ledger Details", filterRequest)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()
      generalLedgerJournalService.transfer(user, filterRequest, locale)
   }

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
      dto: GeneralLedgerJournalDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Transfer a single General Ledger Recurring Entry to create General Ledger Details", dto)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()
      generalLedgerJournalService.transfer(user, dto, locale)
   }

   @Throws(NotFoundException::class)
   @Get(uri = "/export{?filterRequest*}")
   @Operation(
      tags = ["GeneralLedgerJournalEndpoints"],
      summary = "Export a listing of GeneralLedgerJournals",
      description = "Export a listing of GeneralLedgerJournals to a file",
      operationId = "generalLedgerJournal-export"
   )
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200"),
         ApiResponse(responseCode = "204", description = "The requested GeneralLedgerJournal was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun export(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @QueryValue("filterRequest")
      filterRequest: GeneralLedgerJournalExportRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): StreamedFile {
      logger.info("Fetching all GeneralLedgerJournals {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val byteArray = generalLedgerJournalService.export(filterRequest, user.myCompany())
      return StreamedFile(ByteArrayInputStream(byteArray), MediaType.ALL_TYPE).attach("GL Journal Export.csv")
   }

   @Throws(NotFoundException::class)
   @Get(uri = "report{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerJournalEndpoints"], summary = "Fetch a report of pending GeneralLedgerJournals", description = "Fetch a pending report of GeneralLedgerJournals", operationId = "generalLedgerJournal-fetchReport")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerPendingReportTemplate::class))]),
         ApiResponse(responseCode = "204", description = "The requested GeneralLedgerJournal was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: GeneralLedgerJournalReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerPendingReportTemplate {
      logger.info("Fetching all GeneralLedgerJournals {}", filterRequest)

      val user = userService.fetchUser(authentication)
      return generalLedgerJournalService.fetchReport(user.myCompany(), filterRequest)
   }
}
