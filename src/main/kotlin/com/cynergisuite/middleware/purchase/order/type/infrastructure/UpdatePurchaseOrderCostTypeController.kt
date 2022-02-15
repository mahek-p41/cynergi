package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostTypeService
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostTypeValueObject
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
@Controller("/api/purchase-order/type/cost")
class UpdatePurchaseOrderCostTypeController @Inject constructor(
   private val updatePurchaseOrderCostTypeService: UpdatePurchaseOrderCostTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(UpdatePurchaseOrderCostTypeController::class.java)

   @Get
   @Operation(tags = ["UpdatePurchaseOrderCostTypeEndpoints"], summary = "Fetch a list of update purchase order cost types", description = "Fetch a listing of update purchase order cost types", operationId = "updatePurchaseOrderCostType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = UpdatePurchaseOrderCostTypeValueObject::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<UpdatePurchaseOrderCostTypeValueObject> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = updatePurchaseOrderCostTypeService.fetchAll().map {
         UpdatePurchaseOrderCostTypeValueObject(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of update purchase order cost types resulted in {}", types)

      return types
   }
}
