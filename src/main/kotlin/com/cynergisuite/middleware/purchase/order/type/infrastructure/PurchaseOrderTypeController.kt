package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeService
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeValueObject
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
@Controller("/api/purchase-order/type/type")
class PurchaseOrderTypeController @Inject constructor(
   private val purchaseOrderTypeService: PurchaseOrderTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderTypeController::class.java)

   @Get
   @Operation(tags = ["PurchaseOrderTypeEndpoints"], summary = "Fetch a list of purchase order types", description = "Fetch a listing of purchase order types", operationId = "purchaseOrderType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderTypeValueObject::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<PurchaseOrderTypeValueObject> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = purchaseOrderTypeService.fetchAll().map {
         PurchaseOrderTypeValueObject(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of purchase order types resulted in {}", types)

      return types
   }
}
