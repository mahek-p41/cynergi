package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeService
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeValueObject
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
@AreaControl("PO")
@Controller("/api/purchase-order/type/status")
class PurchaseOrderStatusTypeController @Inject constructor(
   private val purchaseOrderStatusTypeService: PurchaseOrderStatusTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderStatusTypeController::class.java)

   @Get
   @Operation(tags = ["PurchaseOrderStatusTypeEndpoints"], summary = "Fetch a list of purchase order status types", description = "Fetch a listing of purchase order status types", operationId = "purchaseOrderStatusType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderStatusTypeValueObject::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<PurchaseOrderStatusTypeValueObject> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = purchaseOrderStatusTypeService.fetchAll().map {
         PurchaseOrderStatusTypeValueObject(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of purchase order status types resulted in {}", types)

      return types
   }
}
