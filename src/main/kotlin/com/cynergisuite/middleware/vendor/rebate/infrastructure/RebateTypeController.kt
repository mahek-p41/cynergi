package com.cynergisuite.middleware.vendor.rebate.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.vendor.rebate.RebateTypeDTO
import com.cynergisuite.middleware.vendor.rebate.RebateTypeService
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
@Controller("/api/vendor/rebate/type")
class RebateTypeController @Inject constructor(
   private val rebateTypeService: RebateTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(RebateTypeController::class.java)

   @Get
   @Operation(tags = ["RebateTypeEndpoints"], summary = "Fetch a list of rebate types", description = "Fetch a listing of rebate types", operationId = "rebateType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RebateTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<RebateTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = rebateTypeService.fetchAll().map {
         RebateTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of rebate types resulted in {}", types)

      return types
   }
}
