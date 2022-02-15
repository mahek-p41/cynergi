package com.cynergisuite.middleware.shipping.location.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.shipping.location.ShipLocationTypeDTO
import com.cynergisuite.middleware.shipping.location.ShipLocationTypeService
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
@Controller("/api/shipping/location")
class ShipLocationTypeController @Inject constructor(
   private val shipLocationTypeService: ShipLocationTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(ShipLocationTypeController::class.java)

   @Get
   @Operation(tags = ["ShipLocationTypeEndpoints"], summary = "Fetch a list of ship location types", description = "Fetch a listing of ship location types", operationId = "shipLocationType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ShipLocationTypeDTO::class))])
      ]
   )
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<ShipLocationTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = shipLocationTypeService.fetchAll().map {
         ShipLocationTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of ship location types resulted in {}", types)

      return types
   }
}
