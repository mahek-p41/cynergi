package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderTypeService
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/purchase/order/type/default")
class DefaultPurchaseOrderTypeController @Inject constructor(
   private val defaultPurchaseOrderTypeService: DefaultPurchaseOrderTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(DefaultPurchaseOrderTypeController::class.java)

   @Get
   @Operation(tags = ["DefaultPurchaseOrderTypeEndpoints"], summary = "Fetch a list of default purchase order types", description = "Fetch a listing of default purchase order types", operationId = "defaultPurchaseOrderType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = DefaultPurchaseOrderTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<DefaultPurchaseOrderTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = defaultPurchaseOrderTypeService.fetchAll().map {
         DefaultPurchaseOrderTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of default purchase order types resulted in {}", types)

      return types
   }
}
