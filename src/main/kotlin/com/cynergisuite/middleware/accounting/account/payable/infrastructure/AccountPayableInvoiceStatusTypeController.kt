package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeService
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
@Controller("/api/accounting/account-payable/type/invoice-status")
class AccountPayableInvoiceStatusTypeController @Inject constructor(
   private val accountPayableInvoiceStatusTypeService: AccountPayableInvoiceStatusTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceStatusTypeController::class.java)

   @Get
   @Operation(tags = ["AccountPayableInvoiceStatusTypeEndpoints"], summary = "Fetch a list of account payable invoice status types", description = "Fetch a listing of account payable invoice status types", operationId = "accountPayableInvoiceStatusType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountPayableInvoiceStatusTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<AccountPayableInvoiceStatusTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = accountPayableInvoiceStatusTypeService.fetchAll().map {
         AccountPayableInvoiceStatusTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of account payable invoice status types resulted in {}", types)

      return types
   }
}
