package com.cynergisuite.middleware.shipping.freight.calc.method.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodTypeService
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodTypeDTO
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
@Controller("/api/vendor/freight/calc/method")
class FreightCalcMethodTypeController @Inject constructor(
   private val freightCalcMethodTypeService: FreightCalcMethodTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(FreightCalcMethodTypeController::class.java)

   @Get
   @Operation(tags = ["FreightCalcMethodTypeEndpoints"], summary = "Fetch a list of Vendor freight calc method types", description = "Fetch a listing of Vendor freight method types", operationId = "freightMethodType-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = FreightCalcMethodTypeDTO::class))])
   ])
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<FreightCalcMethodTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = freightCalcMethodTypeService.fetchAll().map {
         FreightCalcMethodTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of Freight calc method types resulted in {}", types)

      return types
   }
}
