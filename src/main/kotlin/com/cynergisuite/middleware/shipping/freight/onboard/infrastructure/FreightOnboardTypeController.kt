package com.cynergisuite.middleware.shipping.freight.onboard.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeDTO
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeService
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
@Controller("/api/shipping/freight/onboard")
class FreightOnboardTypeController @Inject constructor(
   private val freightOnboardTypeService: FreightOnboardTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(FreightOnboardTypeController::class.java)

   @Get
   @Operation(tags = ["FreightOnboardTypeEndpoints"], summary = "Fetch a list of Vendor freight onboard types", description = "Fetch a listing of Vendor freight onboard types", operationId = "freightOnboardType-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = FreightOnboardTypeDTO::class))])
   ])
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<FreightOnboardTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = freightOnboardTypeService.fetchAll().map {
         FreightOnboardTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of Vendor freight onboard types resulted in {}", types)

      return types
   }
}
