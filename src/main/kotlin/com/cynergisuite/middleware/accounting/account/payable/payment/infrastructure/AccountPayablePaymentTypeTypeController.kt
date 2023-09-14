package com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeService
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
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
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(IS_AUTHENTICATED)
@AreaControl("AP")
@Controller("/api/accounting/account-payable/payment/type/type")
class AccountPayablePaymentTypeTypeController @Inject constructor(
   private val accountPayablePaymentTypeTypeService: AccountPayablePaymentTypeTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayablePaymentTypeTypeController::class.java)

   @Get
   @Operation(tags = ["AccountPayablePaymentTypeTypeEndpoints"], summary = "Fetch a list of account payable payment type types", description = "Fetch a listing of account payable payment type types", operationId = "accountPayablePaymentTypeType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AccountPayablePaymentTypeTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<AccountPayablePaymentTypeTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = accountPayablePaymentTypeTypeService.fetchAll().map {
         AccountPayablePaymentTypeTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of account payable payment type types resulted in {}", types)

      return types
   }
}
