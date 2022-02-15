package com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringTypeDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringTypeService
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/general-ledger/type/recurring-type")
class GeneralLedgerRecurringTypeController @Inject constructor(
   private val generalLedgerRecurringTypeService: GeneralLedgerRecurringTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringTypeController::class.java)

   @Get
   @Operation(tags = ["GeneralLedgerRecurringTypeEndpoints"], summary = "Fetch a list of general ledger recurring types", description = "Fetch a listing of general ledger recurring types", operationId = "generalLedgerRecurringType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerRecurringTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<GeneralLedgerRecurringTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = generalLedgerRecurringTypeService.fetchAll().map {
         GeneralLedgerRecurringTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of general ledger recurring types resulted in {}", types)

      return types
   }
}
