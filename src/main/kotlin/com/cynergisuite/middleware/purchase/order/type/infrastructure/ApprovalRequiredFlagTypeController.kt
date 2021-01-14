package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagTypeService
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
@Controller("/api/purchase-order/type/approval-required-flag")
class ApprovalRequiredFlagTypeController @Inject constructor(
   private val approvalRequiredFlagTypeService: ApprovalRequiredFlagTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(ApprovalRequiredFlagTypeController::class.java)

   @Get
   @Operation(tags = ["ApprovalRequiredFlagTypeEndpoints"], summary = "Fetch a list of approval required flag types", description = "Fetch a listing of approval required flag types", operationId = "approvalRequiredFlagType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ApprovalRequiredFlagDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<ApprovalRequiredFlagDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = approvalRequiredFlagTypeService.fetchAll().map {
         ApprovalRequiredFlagDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of approval required flag types resulted in {}", types)

      return types
   }
}
