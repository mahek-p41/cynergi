package com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeService
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
@Controller("/api/accounting/financial-calendar/type/overall-period")
class OverallPeriodTypeController @Inject constructor(
   private val overallPeriodTypeService: OverallPeriodTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(OverallPeriodTypeController::class.java)

   @Get
   @Operation(tags = ["OverallPeriodTypeEndpoints"], summary = "Fetch a list of overall period types", description = "Fetch a listing of overall period types", operationId = "overallPeriodType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = OverallPeriodTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<OverallPeriodTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = overallPeriodTypeService.fetchAll().map {
         OverallPeriodTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of overall period types resulted in {}", types)

      return types
   }
}
