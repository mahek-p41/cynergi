package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeService
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounting/account-payable/type/print-currency-indicator")
class PrintCurrencyIndicatorTypeController @Inject constructor(
   private val printCurrencyIndicatorTypeService: PrintCurrencyIndicatorTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(PrintCurrencyIndicatorTypeController::class.java)

   @Get
   @Operation(tags = ["PrintCurrencyIndicatorTypeEndpoints"], summary = "Fetch a list of print currency indicator types", description = "Fetch a listing of print currency indicator types", operationId = "printCurrencyIndicatorType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = PrintCurrencyIndicatorTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<PrintCurrencyIndicatorTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = printCurrencyIndicatorTypeService.fetchAll().map {
         PrintCurrencyIndicatorTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of print currency indicator types resulted in {}", types)

      return types
   }
}
