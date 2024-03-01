package com.cynergisuite.middleware.audit.exception.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.audit.exception.AuditExceptionCreateDTO
import com.cynergisuite.middleware.audit.exception.AuditExceptionDTO
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.AuditExceptionService
import com.cynergisuite.middleware.audit.exception.AuditExceptionUpdateDTO
import com.cynergisuite.middleware.audit.exception.AuditExceptionValidator
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
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/audit")
class AuditExceptionController @Inject constructor(
   private val auditExceptionService: AuditExceptionService,
   private val auditExceptionValidator: AuditExceptionValidator,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditExceptionController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/exception/{id}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditExceptionEndpoints"], summary = "Fetch a single AuditException", description = "Fetch a single AuditException by it's system generated primary key", operationId = "auditException-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditExceptionDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested AuditException was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditExceptionDTO {
      logger.info("Fetching AuditException by {}", id)

      val user = userService.fetchUser(authentication)
      val response = auditExceptionService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching AuditException by {} resulted in", id, response)

      return transformEntity(response)
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/{auditId}/exception{?pageRequest*,includeUnscanned}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditExceptionEndpoints"], summary = "Fetch a listing of AuditExceptions", description = "Fetch a paginated listing of AuditExceptions based on a parent Audit", operationId = "auditException-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "auditId", `in` = PATH, description = "The audit for which the listing of exceptions is to be loaded") @QueryValue("auditId") auditId: UUID,
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest")
      @Valid pageRequest: StandardPageRequest,
      @Parameter(name = "includeUnscanned", `in` = ParameterIn.QUERY, required = false) @QueryValue("includeUnscanned") includeUnscanned: Boolean? = false,
      authentication: Authentication
   ): Page<AuditExceptionDTO> {
      logger.info("Fetching all exceptions associated with audit {} {}", auditId, includeUnscanned, pageRequest)

      val user = userService.fetchUser(authentication)
      val page = auditExceptionService.fetchAll(auditId, user.myCompany(), includeUnscanned ?: false, pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page.toPage { transformEntity(it) }
   }

   @Post(value = "/{auditId}/exception", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditExceptionEndpoints"], summary = "Create a single AuditException", description = "Create a single AuditException. The logged in Employee is used for the scannedBy property", operationId = "auditException-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditExceptionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The parent Audit was unable to be found or the scanArea was unknown"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Parameter(name = "auditId", `in` = PATH, description = "The audit that is the parent of the exception being created") @QueryValue("auditId") auditId: UUID,
      @Valid @Body
      vo: AuditExceptionCreateDTO,
      authentication: Authentication
   ): AuditExceptionDTO {
      logger.info("Requested Create AuditException {}", vo)

      val user = userService.fetchUser(authentication)
      val auditException = auditExceptionValidator.validateCreate(auditId, vo, user)
      val response = auditExceptionService.create(auditException)

      logger.debug("Requested Create AuditException {} resulted in {}", vo, response)

      return transformEntity(response)
   }

   @Put(value = "/{auditId}/exception", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditExceptionEndpoints"], summary = "Update a single AuditException", description = "Update a single AuditException where the update is the addition of a note", operationId = "auditException-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditExceptionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested parent Audit or AuditException was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "auditId", `in` = PATH, description = "The audit that is the parent of the exception being updated") @QueryValue("auditId") auditId: UUID,
      @Valid @Body
      vo: AuditExceptionUpdateDTO,
      authentication: Authentication
   ): AuditExceptionDTO {
      logger.info("Requested Update AuditException {}", vo)

      val user = userService.fetchUser(authentication)
      val auditException = auditExceptionValidator.validateUpdate(auditId, vo, user)
      val response = auditExceptionService.update(auditException)

      logger.debug("Requested Update AuditException {} resulted in {}", vo, response)

      return transformEntity(response)
   }

   private fun transformEntity(auditException: AuditExceptionEntity): AuditExceptionDTO {
      return AuditExceptionDTO(auditException, auditException.scanArea?.let { AuditScanAreaDTO(it) })
   }
}
