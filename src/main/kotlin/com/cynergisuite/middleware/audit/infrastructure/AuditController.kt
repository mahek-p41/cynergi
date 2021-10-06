package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.AuditApproveAllExceptionsDTO
import com.cynergisuite.middleware.audit.AuditCreateValueObject
import com.cynergisuite.middleware.audit.AuditService
import com.cynergisuite.middleware.audit.AuditStatusCountDTO
import com.cynergisuite.middleware.audit.AuditUpdateDTO
import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.store.StoreDTO
import com.cynergisuite.middleware.threading.CynergiExecutor
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
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
import java.util.UUID
import javax.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/audit")
class AuditController @Inject constructor(
   private val auditService: AuditService,
   private val executor: CynergiExecutor,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditEndpoints"], summary = "Fetch a single Audit", description = "Fetch a single Audit by it's system generated primary key", operationId = "audit-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Audit was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Audit with", `in` = PATH) @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Fetching Audit by {}", id)

      val user = userService.fetchUser(authentication)
      val response = auditService.fetchById(id = id, company = user.myCompany(), locale = httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)

      logger.debug("Fetching Audit by {} resulted in {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditEndpoints"], summary = "Fetch a listing of Audits", description = "Fetch a paginated listing of Audits", operationId = "audit-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If there are Audits that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @Valid @QueryValue("pageRequest")
      pageRequest: AuditPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AuditValueObject> {
      logger.info("Fetching all audits {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = auditService.fetchAll(pageRequest, user, httpRequest.findLocaleWithDefault())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      logger.debug("Page: {}", page)

      return page
   }

   @Throws(ValidationException::class)
   @Get(uri = "/counts{?pageRequest*}", processes = [APPLICATION_JSON])
   @Operation(tags = ["AuditEndpoints"], summary = "Fetch a listing of Audit Status Counts", description = "Fetch a listing of Audit Status Counts", operationId = "audit-fetchAllStatusCounts")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the data was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Array<AuditStatusCountDTO>::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAuditStatusCounts(
      @Parameter(name = "auditStatusCountRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: AuditPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<AuditStatusCountDTO> {
      logger.debug("Fetching Audit status counts {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()

      return auditService.findAuditStatusCounts(pageRequest, user, locale)
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Create a single audit", description = "Create a single audit in the CREATED state. The logged in Employee is used for the openedBy property", operationId = "audit-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      audit: AuditCreateValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Requested Create Audit {}", audit)

      val user = userService.fetchUser(authentication)
      val defaultStore = user.myLocation()
      val auditToCreate = if (audit.store != null) audit else audit.copy(store = StoreDTO(defaultStore))

      val response = auditService.create(vo = auditToCreate, user = user, locale = httpRequest.findLocaleWithDefault())

      logger.debug("Requested Create Audit {} resulted in {}", audit, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Update a single Audit", description = "This operation is useful for changing the state of the Audit.  Depending on the state being changed the logged in employee will be used for the appropriate fields", operationId = "audit-completeOrCancel")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Body @Valid
      dto: AuditUpdateDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Requested Audit status change or note  {}", dto)

      val user = userService.fetchUser(authentication)
      val response = auditService.update(dto, user, httpRequest.findLocaleWithDefault())

      logger.debug("Requested Update Audit {} resulted in {}", dto, response)

      return response
   }

   @Put("/approve", processes = [APPLICATION_JSON])
   @AccessControl("audit-approver", accessControlProvider = AuditAccessControlProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Approve an audit", description = "This operation will approve all audit exceptions associated with the provided audit that haven't already been approved as well as approving the audit.", operationId = "audit-updateApproved")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun approve(
      @Body @Valid
      audit: SimpleIdentifiableDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Requested approval of audit {}", audit)

      val user = userService.fetchUser(authentication)
      val response = auditService.approve(audit, user, httpRequest.findLocaleWithDefault())

      logger.debug("Requested approval of audit {} resulted in {}", audit, response)

      return response
   }

   @Get(uri = "/{id:[0-9a-fA-F\\-]+}/report/exception", produces = ["application/pdf"])
   @Throws(NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Request Audit Exception Report", description = "This operation will generate a PDF representation of the Audit's exceptions on demand.", operationId = "audit-fetchAuditExceptionReport")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to generate Audit Exception Report", content = [Content(mediaType = "application/pdf")]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAuditExceptionReport(
      @Parameter(description = "Primary Key to lookup the Audit with that the Audit Exception Report will be generated from", `in` = PATH) @QueryValue("id")
      id: UUID,
      authentication: Authentication
   ): HttpResponse<*> {
      val user = userService.fetchUser(authentication)

      logger.info("Audit Exception Report requested by user: {}", user)

      val stream = executor.pipeBlockingOutputToStreamedFile("application/pdf") { os ->
         auditService.fetchAuditExceptionReport(id, user.myCompany(), os)
      }

      return HttpResponse.ok(stream)
   }

   @Get(uri = "/{id:[0-9a-fA-F\\-]+}/report/unscanned", produces = ["application/pdf"])
   @Throws(NotFoundException::class)
   @Operation(
      tags = ["AuditEndpoints"],
      summary = "Request Unscanned Idle Inventory Report",
      description = "This operation will generate a PDF representation of the Audit's items that haven't been scanned.",
      operationId = "audit-fetchUnscannedIdleInventoryReport"
   )
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to generate Unscanned Idle Inventory Report", content = [Content(mediaType = "application/pdf")]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchUnscannedIdleInventoryReport(
      @Parameter(description = "Primary Key to lookup the Audit with that the Unscanned Idle Inventory Report will be generated from", `in` = PATH)
      @QueryValue("id")
      id: UUID,
      authentication: Authentication
   ): HttpResponse<*> {
      val user = userService.fetchUser(authentication)

      logger.info("Unscanned Idle Inventory Report requested by user: {}", user)

      val stream = executor.pipeBlockingOutputToStreamedFile("application/pdf") { os ->
         auditService.fetchUnscannedIdleInventoryReport(id, user.myCompany(), os)
      }

      return HttpResponse.ok(stream)
   }

   @Put("/approve/exceptions", processes = [APPLICATION_JSON])
   @AccessControl("audit-approver", accessControlProvider = AuditAccessControlProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Approve all audit exceptions", description = "This operation will approve all audit exceptions associated with the provided audit that haven't already been approved", operationId = "audit-updateApprovedAllExceptions")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun approveAllExceptions(
      @Body audit: SimpleIdentifiableDTO,
      authentication: Authentication
   ): AuditApproveAllExceptionsDTO {
      logger.info("Requested approval on all audit exceptions associated with audit {}", audit)

      val user = userService.fetchUser(authentication)

      return auditService.approveAllExceptions(audit, user)
   }
}
