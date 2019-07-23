package com.cynergisuite.middleware.audit.status.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.status.AuditStatusService
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
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

@Secured(SecurityRule.IS_AUTHENTICATED) // require access to this controller to at the very least be authenticated
@Controller("/api/audit/status")
class AuditStatusController @Inject constructor(
   private val auditStatusService: AuditStatusService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditStatusController::class.java)

   @Get
   @AccessControl("auditStatus-fetchAll")
   @Operation(summary = "Fetch a list of valid audit statuses", description = "Fetch a listing of supported audit statuses", operationId = "auditStatus-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "Successfully loaded a listing of possible Audit Statuses", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Array<AuditStatusValueObject>::class))])
   ])
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<AuditStatusValueObject> {
      val locale = httpRequest.findLocaleWithDefault()

      val statuses = auditStatusService.fetchAll().map {
         AuditStatusValueObject(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of Audit Statuses resulted in {}", statuses)

      return statuses
   }
}
