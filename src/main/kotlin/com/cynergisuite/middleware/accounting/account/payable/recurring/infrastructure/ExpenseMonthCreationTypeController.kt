package com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.recurring.ExpenseMonthCreationTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.recurring.ExpenseMonthCreationTypeService
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
import jakarta.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/account-payable/type/expense-month-creation")
class ExpenseMonthCreationTypeController @Inject constructor(
   private val expenseMonthCreationTypeService: ExpenseMonthCreationTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(ExpenseMonthCreationTypeController::class.java)

   @Get
   @Operation(tags = ["ExpenseMonthCreationTypeEndpoints"], summary = "Fetch a list of expense month creation types", description = "Fetch a listing of expense month creation types", operationId = "expenseMonthCreationType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ExpenseMonthCreationTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<ExpenseMonthCreationTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = expenseMonthCreationTypeService.fetchAll().map {
         ExpenseMonthCreationTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of expense month creation types resulted in {}", types)

      return types
   }
}
