package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.AuditCreateValueObject
import com.cynergisuite.middleware.audit.AuditService
import com.cynergisuite.middleware.audit.AuditUpdateValueObject
import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.authentication.AuthenticationService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.employee.EmployeeValueObject
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
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED) // require access to this controller to at the very least be authenticated
@Controller("/api/audit")
class AuditController @Inject constructor(
   private val auditService: AuditService,
   private val authenticationService: AuthenticationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("audit-fetchOne")
   @Get(uri = "/{id}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditEndpoints"], summary = "Fetch a single Audit", description = "Fetch a single Audit by it's system generated primary key", operationId = "audit-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the Audit was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Audit with", `in` = ParameterIn.PATH) @QueryValue("id") id: Long,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Fetching Audit by {}", id)

      val response = auditService.fetchById(id = id, locale = httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)

      logger.debug("Fetching Audit by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("audit-fetchAll")
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditEndpoints"], summary = "Fetch a listing of Audits", description = "Fetch a paginated listing of Audits", operationId = "audit-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If there are Audits that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") pageRequest: AuditPageRequest,
      httpRequest: HttpRequest<*>
   ): Page<AuditValueObject> {
      logger.info("Fetching all audits {} {}", pageRequest)
      val page =  auditService.fetchAll(pageRequest, httpRequest.findLocaleWithDefault())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @AccessControl("audit-create")
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Create a single audit", description = "Create a single audit in he OPENED state. The logged in Employee is used for the openedBy property", operationId = "audit-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to save Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun create(
      @Body audit: AuditCreateValueObject,
      authentication: Authentication?,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Requested Create Audit {}", audit)

      val employee: EmployeeValueObject = authenticationService.findEmployee(authentication) ?: throw NotFoundException("employee")
      val auditToCreate = if (audit.store != null) audit else audit.copy(store = employee.store)

      val response = auditService.create(vo = auditToCreate, employee = employee, locale = httpRequest.findLocaleWithDefault())

      logger.debug("Requested Create Audit {} resulted in {}", audit, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @AccessControl("audit-update")
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Update a single Audit", description = "This operation is useful for changing the state of the Audit.  Depending on the state being changed the logged in employee will be used for the appropriate fields", operationId = "audit-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @Body audit: AuditUpdateValueObject,
      authentication: Authentication?,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Requested Update Audit {}", audit)

      val employee: EmployeeValueObject = authenticationService.findEmployee(authentication) ?: throw NotFoundException("employee")
      val response = auditService.update(audit = audit, employee = employee, locale = httpRequest.findLocaleWithDefault())

      logger.debug("Requested Update Audit {} resulted in {}", audit, response)

      return response
   }
}
