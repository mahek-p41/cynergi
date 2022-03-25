package com.cynergisuite.middleware.accounting.general.ledger.journal.entry.infrastructure

import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDTO
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryService
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.micronaut.http.annotation.Controller
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/general-ledger/new-journal-entry")
class GeneralLedgerJournalEntryController @Inject constructor(
   private val journalEntryService: GeneralLedgerJournalEntryService,
   private val userService: UserService
){
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerJournalEntryController::class.java)

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerJournalEntryEndpoints"], summary = "Create a new GeneralLedgerJournalEntry", description = "Create a new GeneralLedgerJournalEntry", operationId = "generalLedgerJournalEntry-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerJournalEntryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The GeneralLedgerJournalEntry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerJournalEntryDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerJournalEntryDTO {
      logger.debug("Requested Create GeneralLedgerJournalEntry {}", dto)

      val user = userService.fetchUser(authentication)
      val response = journalEntryService.create(dto, user)

      logger.debug("Requested Create GeneralLedgerJournalEntry {} resulted in {}", dto, response)

      return response
   }
}
