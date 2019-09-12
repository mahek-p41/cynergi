package com.cynergisuite.middleware.audit.status.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.status.AuditStatusService
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/audit/status")
class AuditStatusController @Inject constructor(
   private val auditStatusService: AuditStatusService,
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditStatusController::class.java)

   @Get(processes = [APPLICATION_JSON])
   @AccessControl("auditStatus-fetchAll")
   @Operation(tags = ["AuditStatusEndpoints"], summary = "Fetch a list of valid audit statuses", description = "Fetch a listing of supported audit statuses", operationId = "auditStatus-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "Successfully loaded a listing of possible Audit Statuses", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Array<AuditStatusValueObject>::class))])
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

   @AccessControl("auditStatus-fetchNext")
   @Get("/{value}", processes = [APPLICATION_JSON])
   @Operation(tags = ["AuditStatusEndpoints"], summary = "Fetch a list of valid audit statuses", description = "Fetch a listing of supported audit statuses", operationId = "auditStatus-fetchNext")
   fun fetchNext(
      @Parameter(description = "Value of the parent status that is used to load the children from", `in` = PATH) @QueryValue("value") value: String,
      httpRequest: HttpRequest<*>
   ): List<AuditStatusValueObject> {
      val locale = httpRequest.findLocaleWithDefault()

      val statuses = auditStatusService.fetchByValue(value)
         ?.nextStates
         ?.map { AuditStatusValueObject(it, it.localizeMyDescription(locale, localizationService)) }
         ?: throw NotFoundException(value)

      logger.debug("Listing of next Audit Statuses resulted in {}", statuses)

      return statuses
   }
}
