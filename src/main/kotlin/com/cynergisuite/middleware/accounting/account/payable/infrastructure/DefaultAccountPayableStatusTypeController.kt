package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeService
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(IS_AUTHENTICATED)
@AreaControl("AP")
@Controller("/api/accounting/account-payable/type/default-account-payable-status")
class DefaultAccountPayableStatusTypeController @Inject constructor(
   private val defaultAccountPayableStatusTypeService: DefaultAccountPayableStatusTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(DefaultAccountPayableStatusTypeController::class.java)

   @Get
   @Operation(tags = ["DefaultAccountPayableStatusTypeEndpoints"], summary = "Fetch a list of default account payable status types", description = "Fetch a listing of default account payable status types", operationId = "defaultAccountPayableStatusType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = DefaultAccountPayableStatusTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<DefaultAccountPayableStatusTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = defaultAccountPayableStatusTypeService.fetchAll().map {
         DefaultAccountPayableStatusTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of default account payable status types resulted in {}", types)

      return types
   }
}
