package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.account.AccountStatusTypeService
import com.cynergisuite.middleware.accounting.account.AccountStatusTypeValueObject
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
@Controller("/api/accounting/account/status")
class AccountStatusTypeController @Inject constructor(
   private val accountStatusTypeService: AccountStatusTypeService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountStatusTypeController::class.java)

   @Get
   @Operation(tags = ["AccountStatusTypeEndpoints"], summary = "Fetch a list of account statuses", description = "Fetch a listing of account statuses", operationId = "accountStatusType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountStatusTypeValueObject::class))])
      ]
   )
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<AccountStatusTypeValueObject> {
      val locale = httpRequest.findLocaleWithDefault()

      val statuses = accountStatusTypeService.fetchAll().map {
         AccountStatusTypeValueObject(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of account statuses resulted in {}", statuses)

      return statuses
   }
}
