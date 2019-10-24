package com.cynergisuite.middleware.audit.schedule.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.schedule.AuditScheduleService
import com.cynergisuite.middleware.audit.schedule.AuditScheduleValueObject
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
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
   @Operation(tags = ["AuditScheduleEndpoints"], summary = "Fetch a single Audit Schedule", description = "Fetch a single Audit Schedule by it's system generated primary key", operationId = "auditSchedule-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the Audit Schedule was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditScheduleValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested Audit Schedule was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Audit Schedule with", `in` = ParameterIn.PATH) @QueryValue("id") id: Long
   ): AuditScheduleValueObject {
      logger.info("Fetching Audit Schedule by {}", id)

      val response = auditScheduleService.fetchById(id = id) ?: throw NotFoundException(id)

      logger.debug("Fetching Audit Schedule by {} resulted in {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("auditSchedule-fetchAll")
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditScheduleEndpoints"], summary = "Fetch a listing of Audits", description = "Fetch a paginated listing of Audit Schedules", operationId = "auditSchedule-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If there are Audit Schedules that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "404", description = "The requested Audit Schedule was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") pageRequestIn: AuditPageRequest?
   ): Page<AuditScheduleValueObject> {
      logger.info("Fetching all audit schedules {} {}", pageRequestIn)
      val pageRequest = PageRequest(pageRequestIn) // copy the result applying defaults if they are missing
      val page =  auditScheduleService.fetchAll(pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }
}
