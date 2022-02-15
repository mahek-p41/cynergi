package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeService
import com.cynergisuite.middleware.localization.LocalizationService
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
@Controller("/api/accounting/account-payable/type/purchase-order-number-required-indicator")
class PurchaseOrderNumberRequiredIndicatorTypeController @Inject constructor(
   private val purchaseOrderNumberRequiredIndicatorTypeService: PurchaseOrderNumberRequiredIndicatorTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderNumberRequiredIndicatorTypeController::class.java)

   @Get
   @Operation(tags = ["PurchaseOrderNumberRequiredIndicatorTypeEndpoints"], summary = "Fetch a list of purchase order number required indicator types", description = "Fetch a listing of purchase order number required indicator types", operationId = "purchaseOrderNumberRequiredIndicatorType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderNumberRequiredIndicatorTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<PurchaseOrderNumberRequiredIndicatorTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = purchaseOrderNumberRequiredIndicatorTypeService.fetchAll().map {
         PurchaseOrderNumberRequiredIndicatorTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of purchase order number required indicator types resulted in {}", types)

      return types
   }
}
