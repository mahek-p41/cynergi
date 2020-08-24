package com.cynergisuite.middleware.accounting.account.payable.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeService
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/account/payable/type/check-form")
class AccountPayableCheckFormTypeController @Inject constructor(
   private val accountPayableCheckFormTypeService: AccountPayableCheckFormTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableCheckFormTypeController::class.java)

   @Get
   @Operation(tags = ["AccountPayableCheckFormTypeEndpoints"], summary = "Fetch a list of account payable check form types", description = "Fetch a listing of account payable check form types", operationId = "accountPayableCheckFormType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountPayableCheckFormTypeDTO::class))])
      ]
   )
   fun fetchAll(httpRequest: HttpRequest<*>): List<AccountPayableCheckFormTypeDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val types = accountPayableCheckFormTypeService.fetchAll().map {
         AccountPayableCheckFormTypeDTO(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of account payable check form types resulted in {}", types)

      return types
   }
}
