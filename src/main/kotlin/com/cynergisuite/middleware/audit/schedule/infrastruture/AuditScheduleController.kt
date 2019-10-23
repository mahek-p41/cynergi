package com.cynergisuite.middleware.audit.schedule.infrastruture

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.schedule.AuditScheduleService
import com.cynergisuite.middleware.audit.schedule.AuditScheduleValueObject
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
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
@Controller("/api/audit/schedule")
class AuditScheduleController @Inject constructor(
   private val auditScheduleService: AuditScheduleService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditScheduleController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("auditSchedule-fetchOne")
   @Get(uri = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditScheduleEndpoints"], summary = "Fetch a single AuditSchedule", description = "Fetch a single AuditSchedule by it's system generated primary key", operationId = "auditSchedule-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the AuditSchedule was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditScheduleValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested AuditSchedule was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the AuditSchedule with", `in` = PATH) @QueryValue("id") id: Long,
      httpRequest: HttpRequest<*>
   ) : AuditScheduleValueObject {
      logger.info("Fetching audit schedule by {}", id)

      val response = auditScheduleService.fetchById(id, httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)

      logger.debug("Fetching audit schedule by {} resulted in {}", id, response)

      return response
   }
}
