package com.cynergisuite.middleware.audit.discrepancy.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.middleware.audit.discrepancy.AuditDiscrepancyService
import com.cynergisuite.middleware.audit.discrepancy.AuditDiscrepancyValueObject
import com.cynergisuite.middleware.authentication.AuthenticationService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
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
class AuditDiscrepancyController @Inject constructor(
   private val auditDiscrepancyService: AuditDiscrepancyService,
   private val authenticationService: AuthenticationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditDiscrepancyController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("auditException-fetchOne")
   @Get(value = "/discrepancy/{id}", produces = [APPLICATION_JSON])
   @Operation(summary = "Fetch a single AuditDiscrepancy", description = "Fetch a single AuditDiscrepancy by it's system generated primary key", operationId = "auditDiscrepancy-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditDiscrepancyValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested AuditDiscrepancy was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @QueryValue("id") id: Long
   ): AuditDiscrepancyValueObject {
      logger.info("Fetching AuditDiscrepancy by {}", id)

      val response = auditDiscrepancyService.fetchById(id = id) ?: throw NotFoundException(id)

      logger.debug("Fetching AuditDiscrepancy by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("auditDetail-fetchAll")
   @Get(uri = "/{auditId}/discrepancy{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(summary = "Fetch a listing of AuditDetails", description = "Fetch a paginated listing of AuditDetails based on a parent Audit", operationId = "auditDetail-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "auditId", `in` = ParameterIn.PATH, description = "The audit for which the listing of details is to be loaded") @QueryValue("auditId") auditId: Long,
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") pageRequest: PageRequest
   ): Page<AuditDiscrepancyValueObject> {
      logger.info("Fetching all details associated with audit {} {}", auditId, pageRequest)
      val page =  auditDiscrepancyService.fetchAll(auditId, pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(value = "/{auditId}/discrepancy", processes = [APPLICATION_JSON])
   @AccessControl("auditDiscrepancy-save")
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(summary = "Create a single AuditDiscrepancy", description = "Save a single AuditDiscrepancy. The logged in Employee is used for the scannedBy property", operationId = "auditDiscrepancy-save")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditDiscrepancyValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The parent Audit was unable to be found, or the scanArea was unknown"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun save(
      @Parameter(name = "auditId", `in` = ParameterIn.PATH, description = "The audit for which the listing of details is to be loaded") @QueryValue("auditId") auditId: Long,
      @Body auditDiscrepancy: AuditDiscrepancyValueObject,
      authentication: Authentication?
   ): AuditDiscrepancyValueObject {
      logger.info("Requested Save AuditDiscrepancy {}", auditDiscrepancy)

      val employee: EmployeeValueObject = authenticationService.findEmployee(authentication) ?: throw NotFoundException("employee")
      val response = auditDiscrepancyService.create(auditDiscrepancy.copy(scannedBy = employee, audit = SimpleIdentifiableValueObject(auditId)))

      logger.debug("Requested Save AuditDiscrepancy {} resulted in {}", auditDiscrepancy, response)

      return response
   }

   @Put(value = "/{auditId}/discrepancy", processes = [APPLICATION_JSON])
   @AccessControl("auditDiscrepancy-update")
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(summary = "Update a single AuditDiscrepancy", description = "Update a single AuditDiscrepancy", operationId = "auditDiscrepancy-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditDiscrepancyValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested AuditDiscrepancy was unable to be found or the scanArea was unknown"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @Parameter(name = "auditId", `in` = ParameterIn.PATH, description = "The audit for which the listing of details is to be loaded") @QueryValue("auditId") auditId: Long,
      @Body vo: AuditDiscrepancyValueObject,
      authentication: Authentication?
   ): AuditDiscrepancyValueObject {
      logger.info("Requested Update AuditDiscrepancy {}", vo)

      val employee: EmployeeValueObject = authenticationService.findEmployee(authentication) ?: throw NotFoundException("employee")
      val response = auditDiscrepancyService.update(vo.copy(scannedBy = employee, audit = SimpleIdentifiableValueObject(auditId)))

      logger.debug("Requested Update AuditDiscrepancy {} resulted in {}", vo, response)

      return response
   }
}
