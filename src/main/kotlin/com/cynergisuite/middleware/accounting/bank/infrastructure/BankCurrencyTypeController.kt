package com.cynergisuite.middleware.accounting.bank.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.bank.BankCurrencyTypeService
import com.cynergisuite.middleware.accounting.bank.BankCurrencyTypeValueObject
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounting/bank/currency")
class BankCurrencyTypeController @Inject constructor(
   private val BankCurrencyTypeService: BankCurrencyTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(BankCurrencyTypeController::class.java)

   @Get
   @Operation(tags = ["BankCurrencyTypeEndpoints"], summary = "Fetch a list of bank currencies", description = "Fetch a listing of bank supported currencies", operationId = "bankCurrencyType-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = BankCurrencyTypeValueObject::class))])
   ])
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<BankCurrencyTypeValueObject> {
      val locale = httpRequest.findLocaleWithDefault()

      val statuses = BankCurrencyTypeService.fetchAll().map {
         BankCurrencyTypeValueObject(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of Bank Currency Codes resulted in {}", statuses)

      return statuses
   }
}
