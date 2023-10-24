package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeService
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
@Controller("/api/accounting/account-payable/type/invoice-selected")
class AccountPayableInvoiceSelectedTypeController @Inject constructor(
   private val accountPayableInvoiceSelectedTypeService: AccountPayableInvoiceSelectedTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceSelectedTypeController::class.java)

   @Get
   @Operation(tags = ["AccountPayableInvoiceSelectedTypeEndpoints"], summary = "Fetch a list of account payable invoice selected types", description = "Fetch a listing of account payable invoice selected types", operationId = "accountPayableInvoiceSelectedType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountPayableInvoiceSelectedTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<AccountPayableInvoiceSelectedTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = accountPayableInvoiceSelectedTypeService.fetchAll().map {
         AccountPayableInvoiceSelectedTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of account payable invoice selected types resulted in {}", types)

      return types
   }
}
