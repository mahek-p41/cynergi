package com.cynergisuite.middleware.shipping.freight.term.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.shipping.freight.term.FreightTermTypeDTO
import com.cynergisuite.middleware.shipping.freight.term.FreightTermTypeService
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
@Controller("/api/shipping/freight/term")
class FreightTermTypeController @Inject constructor(
   private val freightTermTypeService: FreightTermTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(FreightTermTypeController::class.java)

   @Get
   @Operation(tags = ["FreightTermTypeEndpoints"], summary = "Fetch a list of freight term types", description = "Fetch a listing of freight term types", operationId = "freightTermType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = FreightTermTypeDTO::class))])
      ]
   )
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<FreightTermTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = freightTermTypeService.fetchAll().map {
         FreightTermTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of freight term types resulted in {}", types)

      return types
   }
}
