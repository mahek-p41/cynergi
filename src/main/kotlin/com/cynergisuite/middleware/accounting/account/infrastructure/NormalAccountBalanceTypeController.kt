package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.NormalAccountBalanceTypeService
import com.cynergisuite.middleware.accounting.account.NormalAccountBalanceTypeValueObject
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
import jakarta.inject.Inject

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounting/account/balance-type")
class NormalAccountBalanceTypeController @Inject constructor(
   private val normalAccountBalanceTypeService: NormalAccountBalanceTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(NormalAccountBalanceTypeController::class.java)

   @Get
   @Operation(tags = ["NormalAccountBalanceTypeEndpoints"], summary = "Fetch a list of account statuses", description = "Fetch a listing of normal account balances", operationId = "normalAccountBalanceType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = NormalAccountBalanceTypeValueObject::class))])
      ]
   )
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<NormalAccountBalanceTypeValueObject> {
      val locale = httpRequest.findLocaleWithDefault()

      val statuses = normalAccountBalanceTypeService.fetchAll().map {
         NormalAccountBalanceTypeValueObject(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of normal account balances resulted in {}", statuses)

      return statuses
   }
}
