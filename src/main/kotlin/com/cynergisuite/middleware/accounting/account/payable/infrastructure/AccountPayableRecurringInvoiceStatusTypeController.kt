package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeService
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
@Controller("/api/accounting/account-payable/type/recurring-invoice-status")
class AccountPayableRecurringInvoiceStatusTypeController @Inject constructor(
   private val accountPayableRecurringInvoiceStatusTypeService: AccountPayableRecurringInvoiceStatusTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableRecurringInvoiceStatusTypeController::class.java)

   @Get
   @Operation(tags = ["AccountPayableRecurringInvoiceStatusTypeEndpoints"], summary = "Fetch a list of account payable recurring invoice status types", description = "Fetch a listing of account payable recurring invoice status types", operationId = "accountPayableRecurringInvoiceStatusType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountPayableRecurringInvoiceStatusTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<AccountPayableRecurringInvoiceStatusTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = accountPayableRecurringInvoiceStatusTypeService.fetchAll().map {
         AccountPayableRecurringInvoiceStatusTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of account payable recurring invoice status types resulted in {}", types)

      return types
   }
}
