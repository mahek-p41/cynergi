package com.cynergisuite.middleware.audit.detail.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.detail.AuditDetailService
import com.cynergisuite.middleware.audit.detail.AuditDetailValueObject
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

@Secured(IS_AUTHENTICATED)
@Controller("/api/audit")
class AuditDetailController @Inject constructor(
   private val auditDetailService: AuditDetailService,
   private val authenticationService: AuthenticationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("auditDetail-fetchOne")
   @Get(uri = "/detail/{id}", produces = [APPLICATION_JSON])
   @Operation(summary = "Fetch a single AuditDetail", description = "Fetch a single AuditDetail by it's system generated primary key", operationId = "auditDetail-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditDetailValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested AuditDetail was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @QueryValue("id") id: Long,
      httpRequest: HttpRequest<*>
   ): AuditDetailValueObject {
      logger.info("Fetching AuditDetail by {}", id)

      val response = auditDetailService.fetchById(id = id, locale = httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)

      logger.debug("Fetching AuditDetail by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("auditDetail-fetchAll")
   @Get(uri = "/{auditId}/detail{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(summary = "Fetch a listing of AuditDetails", description = "Fetch a paginated listing of AuditDetails based on a parent Audit", operationId = "auditDetail-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "auditId", `in` = ParameterIn.PATH, description = "The audit for which the listing of details is to be loaded") @QueryValue("auditId") auditId: Long,
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") pageRequest: PageRequest,
      httpRequest: HttpRequest<*>
   ): Page<AuditDetailValueObject> {
      logger.info("Fetching all details associated with audit {} {}", auditId, pageRequest)
      val page =  auditDetailService.fetchAll(auditId, pageRequest, httpRequest.findLocaleWithDefault())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(uri = "/{auditId}/detail", processes = [APPLICATION_JSON])
   @AccessControl("auditDetail-save")
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(summary = "Create a single AuditDetail", description = "Save a single AuditDetail. The logged in Employee is used for the scannedBy property", operationId = "auditDetail-save")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditDetailValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The parent Audit was unable to be found, or the scanArea was unknown"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun save(
      @Parameter(name = "auditId", `in` = ParameterIn.PATH, description = "The audit for which the listing of details is to be loaded") @QueryValue("auditId") auditId: Long,
      @Body vo: AuditDetailValueObject,
      authentication: Authentication?,
      httpRequest: HttpRequest<*>
   ): AuditDetailValueObject {
      logger.info("Requested Save AuditDetail {}", vo)

      val employee: EmployeeValueObject = authenticationService.findEmployee(authentication) ?: throw NotFoundException("employee")
      val response = auditDetailService.create(vo.copy(audit = SimpleIdentifiableValueObject(auditId), scannedBy = employee), httpRequest.findLocaleWithDefault())

      logger.debug("Requested Save AuditDetail {} resulted in {}", vo, response)

      return response
   }

   @Put(uri = "/{auditId}/detail", processes = [APPLICATION_JSON])
   @AccessControl("auditDetail-update")
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(summary = "Update a single AuditDetail", description = "Update a single AuditDetail", operationId = "auditDetail-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditDetailValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested AuditDetail was unable to be found or the scanArea was unknown"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @Parameter(name = "auditId", `in` = ParameterIn.PATH, description = "The audit for which the listing of details is to be loaded") @QueryValue("auditId") auditId: Long,
      @Body vo: AuditDetailValueObject,
      httpRequest: HttpRequest<*>
   ): AuditDetailValueObject {
      logger.info("Requested Update AuditDetail {}", vo)

      val response = auditDetailService.update(vo.copy(audit = SimpleIdentifiableValueObject(auditId)), httpRequest.findLocaleWithDefault())

      logger.debug("Requested Update AuditDetail {} resulted in {}", vo, response)

      return response
   }
}
