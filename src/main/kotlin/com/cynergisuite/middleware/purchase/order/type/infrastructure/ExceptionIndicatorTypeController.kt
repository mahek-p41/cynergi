package com.cynergisuite.middleware.purchase.order.type.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeService
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
import jakarta.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/purchase-order/type/exception-indicator")
class ExceptionIndicatorTypeController @Inject constructor(
   private val exceptionIndicatorTypeService: ExceptionIndicatorTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(ExceptionIndicatorTypeController::class.java)

   @Get
   @Operation(tags = ["ExceptionIndicatorTypeEndpoints"], summary = "Fetch a list of exception indicator types", description = "Fetch a listing of exception indicator types", operationId = "exceptionIndicatorType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ExceptionIndicatorTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<ExceptionIndicatorTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = exceptionIndicatorTypeService.fetchAll().map {
         ExceptionIndicatorTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of exception indicator types resulted in {}", types)

      return types
   }
}
