package com.cynergisuite.middleware.audit.exception.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.exception.AuditExceptionCreateValueObject
import com.cynergisuite.middleware.audit.exception.AuditExceptionService
import com.cynergisuite.middleware.audit.exception.AuditExceptionUpdateValueObject
import com.cynergisuite.middleware.audit.exception.AuditExceptionValueObject
import com.cynergisuite.middleware.authentication.AuthenticationService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
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
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/audit")
class AuditExceptionController @Inject constructor(
   private val auditExceptionService: AuditExceptionService,
   private val authenticationService: AuthenticationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditExceptionController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("auditException-fetchOne")
   @Get(value = "/exception/{id}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditExceptionEndpoints"], summary = "Fetch a single AuditException", description = "Fetch a single AuditException by it's system generated primary key", operationId = "auditException-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditExceptionValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested AuditException was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @QueryValue("id") id: Long,
      httpRequest: HttpRequest<*>
   ): AuditExceptionValueObject {
      logger.info("Fetching AuditException by {}", id)

      val locale = httpRequest.findLocaleWithDefault()
      val response = auditExceptionService.fetchById(id, locale) ?: throw NotFoundException(id)

      logger.debug("Fetching AuditException by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("auditException-fetchAll")
   @Get(uri = "/{auditId}/exception{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditExceptionEndpoints"], summary = "Fetch a listing of AuditExceptions", description = "Fetch a paginated listing of AuditExceptions based on a parent Audit", operationId = "auditException-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "auditId", `in` = PATH, description = "The audit for which the listing of exceptions is to be loaded") @QueryValue("auditId") auditId: Long,
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      httpRequest: HttpRequest<*>
   ): Page<AuditExceptionValueObject> {
      logger.info("Fetching all details associated with audit {} {}", auditId, pageRequest)
      val locale = httpRequest.findLocaleWithDefault()
      val page =  auditExceptionService.fetchAll(auditId, pageRequest, locale)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(value = "/{auditId}/exception", processes = [APPLICATION_JSON])
   @AccessControl("auditException-create")
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditExceptionEndpoints"], summary = "Create a single AuditException", description = "Create a single AuditException. The logged in Employee is used for the scannedBy property", operationId = "auditException-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditExceptionValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If the request body is invalid"),
      ApiResponse(responseCode = "404", description = "The parent Audit was unable to be found or the scanArea was unknown"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun create(
      @Parameter(name = "auditId", `in` = PATH, description = "The audit that is the parent of the exception being created") @QueryValue("auditId") auditId: Long,
      @Body vo: AuditExceptionCreateValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditExceptionValueObject {
      logger.info("Requested Create AuditException {}", vo)

      val locale = httpRequest.findLocaleWithDefault()
      val user = authenticationService.findUser(authentication)
      val response = auditExceptionService.create(auditId, vo, user, locale)

      logger.debug("Requested Create AuditException {} resulted in {}", vo, response)

      return response
   }

   @Put(value = "/{auditId}/exception", processes = [APPLICATION_JSON])
   @AccessControl("auditException-update")
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditExceptionEndpoints"], summary = "Update a single AuditException", description = "Update a single AuditException where the update is the addition of a note", operationId = "auditException-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditExceptionValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If request body is invalid"),
      ApiResponse(responseCode = "404", description = "The requested parent Audit or AuditException was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @Parameter(name = "auditId", `in` = PATH, description = "The audit that is the parent of the exception being updated") @QueryValue("auditId") auditId: Long,
      @Body vo: AuditExceptionUpdateValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditExceptionValueObject {
      logger.info("Requested Update AuditException {}", vo)

      val locale = httpRequest.findLocaleWithDefault()
      val user = authenticationService.findUser(authentication)
      val response = auditExceptionService.addNote(auditId, vo, user, locale)

      logger.debug("Requested Update AuditException {} resulted in {}", vo, response)

      return response
   }
}
