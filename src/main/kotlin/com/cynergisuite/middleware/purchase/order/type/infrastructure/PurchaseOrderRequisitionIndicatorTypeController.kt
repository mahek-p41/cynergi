package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderRequisitionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderRequisitionIndicatorTypeService
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
@Controller("/api/purchase-order/type/requisition-indicator")
class PurchaseOrderRequisitionIndicatorTypeController @Inject constructor(
   private val purchaseOrderRequisitionIndicatorTypeService: PurchaseOrderRequisitionIndicatorTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderRequisitionIndicatorTypeController::class.java)

   @Get
   @Operation(tags = ["PurchaseOrderRequisitionIndicatorTypeEndpoints"], summary = "Fetch a list of purchase order requisition indicator types", description = "Fetch a listing of purchase order requisition indicator types", operationId = "purchaseOrderRequisitionIndicatorType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderRequisitionIndicatorTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<PurchaseOrderRequisitionIndicatorTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = purchaseOrderRequisitionIndicatorTypeService.fetchAll().map {
         PurchaseOrderRequisitionIndicatorTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of purchase order requisition indicator types resulted in {}", types)

      return types
   }
}
