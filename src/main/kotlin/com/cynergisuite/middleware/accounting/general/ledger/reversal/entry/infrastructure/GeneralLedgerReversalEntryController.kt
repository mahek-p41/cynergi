package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.GeneralLedgerReversalEntryDTO
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Secured(IS_AUTHENTICATED)
@AreaControl("GL")
@Controller("/api/accounting/general-ledger/reversal/entry")
class GeneralLedgerReversalEntryController @Inject constructor(
   private val generalLedgerDetailService: GeneralLedgerDetailService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalEntryController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerReversalEntryEndpoints"], summary = "Post a single General Ledger Reversal Entry", description = "Post a single GeneralLedgerReversalEntry", operationId = "generalLedgerReversalEntry-postReversalEntry")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerReversalEntryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The General Ledger Reversal Entry was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun postReversalEntry(
      @Parameter(name = "id", description = "The General Ledger Reversal id for the General Ledger Reversal Entry being posted")
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.debug("Requested Post General Ledger Reversal Entry for General Ledger Reversal {}", id)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()
      val response = generalLedgerDetailService.postReversalEntry(id, user, locale)

      logger.debug("Requested Post General Ledger Reversal Entry for General Ledger Reversal {} resulted in {}", id, response)
   }
}
