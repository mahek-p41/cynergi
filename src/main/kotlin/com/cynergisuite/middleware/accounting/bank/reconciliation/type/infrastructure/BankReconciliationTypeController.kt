package com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeService
import com.cynergisuite.middleware.localization.LocalizationService
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
@Controller("/api/accounting/bank-recon/type")
class BankReconciliationTypeController @Inject constructor(
   private val bankReconciliationTypeService: BankReconciliationTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(BankReconciliationTypeController::class.java)

   @Get
   @Operation(tags = ["BankReconciliationTypeEndpoints"], summary = "Fetch a list of bank reconciliation types", description = "Fetch a listing of bank reconciliation types", operationId = "bankReconciliationType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = BankReconciliationTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<BankReconciliationTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = bankReconciliationTypeService.fetchAll().map {
         BankReconciliationTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of bank reconciliation types resulted in {}", types)

      return types
   }
}
