package com.cynergisuite.middleware.audit.schedule.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.infrastructure.AuditAccessControlProvider
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.schedule.AuditScheduleCreateUpdateDataTransferObject
import com.cynergisuite.middleware.audit.schedule.AuditScheduleDataTransferObject
import com.cynergisuite.middleware.audit.schedule.AuditScheduleService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
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
   private val auditScheduleService: AuditScheduleService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditScheduleController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("auditSchedule-fetchOne", accessControlProvider = AuditAccessControlProvider::class)
   @Get(uri = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditScheduleEndpoints"], summary = "Fetch a single Audit Schedule", description = "Fetch a single Audit Schedule by it's system generated primary key", operationId = "auditSchedule-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the Audit Schedule was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditScheduleDataTransferObject::class))]),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested Audit Schedule was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Audit Schedule with", `in` = PATH) @QueryValue("id") id: Long,
      authentication: Authentication
   ): AuditScheduleDataTransferObject {
      logger.info("Fetching Audit Schedule by {}", id)

      val user = userService.findUser(authentication)
      val response = auditScheduleService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Audit Schedule by {} resulted in {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("auditSchedule-fetchAll", accessControlProvider = AuditAccessControlProvider::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditScheduleEndpoints"], summary = "Fetch a listing of Audits", description = "Fetch a paginated listing of Audit Schedules", operationId = "auditSchedule-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If there are Audit Schedules that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested Audit Schedule was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") pageRequest: AuditPageRequest,
      authentication: Authentication
   ): Page<AuditScheduleDataTransferObject> {
      logger.info("Fetching all audit schedules {} {}", pageRequest)

      val user = userService.findUser(authentication)
      val page = auditScheduleService.fetchAll(pageRequest, user.myCompany())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @AccessControl("auditSchedule-create", accessControlProvider = AuditAccessControlProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditScheduleEndpoints"], summary = "Create a single audit schedule", description = "Create a single audit schedule for the provided stores and to be executed by a department", operationId = "auditSchedule-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to save audit schedule", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditScheduleDataTransferObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun create(
      @Body auditSchedule: AuditScheduleCreateUpdateDataTransferObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
      ): AuditScheduleDataTransferObject {
      logger.info("Requested Create Audit Schedule {}", auditSchedule)

      val locale = httpRequest.findLocaleWithDefault()
      val user = userService.findUser(authentication)
      val response = auditScheduleService.create(auditSchedule, user, locale)

      logger.debug("Requested creation of audit schedule using {} resulted in {}", auditSchedule, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @AccessControl("auditSchedule-update", accessControlProvider = AuditAccessControlProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditScheduleEndpoints"], summary = "Update a single audit schedule", description = "This operation is useful for changing the state of the audit schedule", operationId = "auditSchedule-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditScheduleDataTransferObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @Body auditSchedule: AuditScheduleCreateUpdateDataTransferObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) : AuditScheduleDataTransferObject {
      logger.info("Requested update audit schedule {}", auditSchedule)

      val locale = httpRequest.findLocaleWithDefault()
      val user = userService.findUser(authentication)
      val response = auditScheduleService.update(auditSchedule, user, locale)

      logger.debug("Requested update of audit schedule {} resulted in {}", auditSchedule, response)

      return response
   }
}
