package com.cynergisuite.middleware.audit.detail.scan.area.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.infrastructure.AlwaysAllowAccessControlProvider
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
@Controller("/api/audit/detail/scan-area")
class AuditScanAreaController @Inject constructor(
   private val auditScanAreaService: AuditScanAreaService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditScanAreaController::class.java)

   @Get
   @AccessControl("auditDetailScanArea-fetchAll", accessControlProvider = AlwaysAllowAccessControlProvider::class)
   @Operation(tags = ["AuditScanAreaEndpoints"], summary = "Fetch a list of valid audit detail scan areas", description = "Fetch a listing of supported audit detail scan areas", operationId = "auditDetailScanArea-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AuditScanAreaValueObject::class))])
   ])
   fun fetchAll(
      httpRequest: HttpRequest<*>
   ): List<AuditScanAreaValueObject> {
      val locale = httpRequest.findLocaleWithDefault()

      val statuses = auditScanAreaService.fetchAll().map {
         AuditScanAreaValueObject(it, it.localizeMyDescription(locale, localizationService))
      }

      logger.debug("Listing of Audit Scan Areas resulted in {}", statuses)

      return statuses
   }
}
