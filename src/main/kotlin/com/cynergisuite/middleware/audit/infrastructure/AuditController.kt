package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.AuditCreateValueObject
import com.cynergisuite.middleware.audit.AuditService
import com.cynergisuite.middleware.audit.AuditSignOffAllExceptionsDataTransferObject
import com.cynergisuite.middleware.audit.AuditStatusCountDataTransferObject
import com.cynergisuite.middleware.audit.AuditUpdateValueObject
import com.cynergisuite.middleware.audit.AuditValueObject
import com.cynergisuite.middleware.authentication.AuthenticationService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.store.StoreValueObject
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
@Controller("/api/audit")
class AuditController @Inject constructor(
   private val auditService: AuditService,
   private val authenticationService: AuthenticationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("audit-fetchOne", accessControlProvider = AuditAccessProvider::class)
   @Get(uri = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditEndpoints"], summary = "Fetch a single Audit", description = "Fetch a single Audit by it's system generated primary key", operationId = "audit-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the Audit was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Audit with", `in` = PATH) @QueryValue("id") id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Fetching Audit by {}", id)

      val user = authenticationService.findUser(authentication)
      val response = auditService.fetchById(id = id, dataset = user.myDataset(), locale = httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)

      logger.debug("Fetching Audit by {} resulted in {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("audit-fetchAll", accessControlProvider = AuditAccessProvider::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditEndpoints"], summary = "Fetch a listing of Audits", description = "Fetch a paginated listing of Audits", operationId = "audit-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If there are Audits that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") pageRequest: AuditPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AuditValueObject> {
      logger.info("Fetching all audits {} {}", pageRequest)

      val user = authenticationService.findUser(authentication)
      val page =  auditService.fetchAll(pageRequest, user.myDataset(), httpRequest.findLocaleWithDefault())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Throws(ValidationException::class)
   @AccessControl("audit-fetchAllStatusCounts", accessControlProvider = AuditAccessProvider::class)
   @Get(uri = "/counts{?pageRequest*}", processes = [APPLICATION_JSON])
   @Operation(tags = ["AuditEndpoints"], summary = "Fetch a listing of Audit Status Counts", description = "Fetch a listing of Audit Status Counts", operationId = "audit-fetchAllStatusCounts")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the data was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Array<AuditStatusCountDataTransferObject>::class))]),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAuditStatusCounts(
      @Parameter(name = "auditStatusCountRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") pageRequest: AuditPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<AuditStatusCountDataTransferObject> {
      logger.debug("Fetching Audit status counts {}", pageRequest)

      val user = authenticationService.findUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()

      return auditService.findAuditStatusCounts(pageRequest, user.myDataset(), locale)
   }

   @Post(processes = [APPLICATION_JSON])
   @AccessControl("audit-create", accessControlProvider = AuditAccessProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Create a single audit", description = "Create a single audit in the CREATED state. The logged in Employee is used for the openedBy property", operationId = "audit-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to save Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun create(
      @Body audit: AuditCreateValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Requested Create Audit {}", audit)

      val user = authenticationService.findUser(authentication)
      val defaultStore = user.myLocation() ?: throw NotFoundException("store")
      val auditToCreate = if (audit.store != null) audit else audit.copy(store = StoreValueObject(defaultStore))

      val response = auditService.create(vo = auditToCreate, employee = user, locale = httpRequest.findLocaleWithDefault())

      logger.debug("Requested Create Audit {} resulted in {}", audit, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @AccessControl("audit-CompleteOrCancel", accessControlProvider = AuditAccessProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Update a single Audit", description = "This operation is useful for changing the state of the Audit.  Depending on the state being changed the logged in employee will be used for the appropriate fields", operationId = "audit-CompleteOrCancel")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun addNoteCompleteOrCancel(
      @Body audit: AuditUpdateValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Requested Audit status change or note  {}", audit)

      val user = authenticationService.findUser(authentication)
      val response = auditService.completeOrCancel(audit, user, httpRequest.findLocaleWithDefault())

      logger.debug("Requested Update Audit {} resulted in {}", audit, response)

      return response
   }

   @Put("/sign-off", processes = [APPLICATION_JSON])
   @AccessControl("audit-updateSignOff", accessControlProvider = AuditAccessProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Sign off on an audit", description = "This operation will sign off all on audit exceptions associated with the provided audit that haven't already been signed off on as well as signing off the audit.", operationId = "audit-updateSignOff")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun signOff(
      @Body audit: SimpleIdentifiableDataTransferObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditValueObject {
      logger.info("Requested sign-off of audit {}", audit)

      val user = authenticationService.findUser(authentication)
      val response = auditService.signOff(audit, user, httpRequest.findLocaleWithDefault())

      logger.debug("Requested sign-off of audit {} resulted in {}", audit, response)

      return response
   }

   @Put("/sign-off/exceptions", processes = [APPLICATION_JSON])
   @AccessControl("audit-updateSignOffAllExceptions", accessControlProvider = AuditAccessProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditEndpoints"], summary = "Sign off on all audit exceptions", description = "This operation will sign off all on audit exceptions associated with the provided audit that haven't already been signed off on", operationId = "audit-updateSignOffAllExceptions")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update Audit", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested Audit was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun signOffAllExceptions(
      @Body audit: SimpleIdentifiableDataTransferObject,
      authentication: Authentication
   ): AuditSignOffAllExceptionsDataTransferObject {
      logger.info("Requested sign of on all audit exceptions associated with audit {}", audit)

      val user = authenticationService.findUser(authentication)

      return auditService.signOffAllExceptions(audit, user)
   }
}
