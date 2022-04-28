package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.vendor.VendorTypeDTO
import com.cynergisuite.middleware.vendor.VendorTypeService
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
@Controller("/api/vendor/type")
class VendorTypeController @Inject constructor(
   private val vendorTypeService: VendorTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorTypeController::class.java)

   @Get
   @Operation(tags = ["VendorTypeEndpoints"], summary = "Fetch a list of vendor 1099 types", description = "Fetch a listing of vendor 1099 types", operationId = "vendorType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = VendorTypeDTO::class))])
      ]
   )
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<VendorTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val vendorTypes = vendorTypeService.fetchAll().map {
         VendorTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of Vendor Types resulted in {}", vendorTypes)

      return vendorTypes
   }
}
