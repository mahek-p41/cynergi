package com.cynergisuite.middleware.audit.permission.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.infrastructure.AuditAccessControlProvider
import com.cynergisuite.middleware.audit.permission.AuditPermissionCreateUpdateDataTransferObject
import com.cynergisuite.middleware.audit.permission.AuditPermissionService
import com.cynergisuite.middleware.audit.permission.AuditPermissionTypeValueObject
import com.cynergisuite.middleware.audit.permission.AuditPermissionValueObject
import com.cynergisuite.middleware.authentication.AuthenticationService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
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
@Controller("/api/audit/permission")
class AuditPermissionController @Inject constructor(
   private val auditPermissionService: AuditPermissionService,
   private val authenticationService: AuthenticationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditPermissionController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @AccessControl("auditPermission-fetchOne", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Fetch a single Audit Permission", description = "Fetch a single Audit Permission by it's system generated primary key", operationId = "auditPermission-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the Audit Permission was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditPermissionValueObject::class))]),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested Audit Permission was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Audit with", `in` = PATH) @QueryValue("id") id: Long,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): AuditPermissionValueObject {
      logger.debug("User {} requested Audit Permission by ID {}", authentication, id)

      val user = authenticationService.findUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()

      return auditPermissionService.fetchById(id, user.myDataset(), locale) ?: throw NotFoundException(id)
   }

   @Throws(PageOutOfBoundsException::class)
   @Get("{?pageRequest*}", processes = [APPLICATION_JSON])
   @AccessControl("auditPermission-fetchAll", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Fetch a single Audit Permission", description = "Fetch a listing of Audit Permissions", operationId = "auditPermission-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If there are Audit Permissions that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): Page<AuditPermissionValueObject> {
      logger.debug("User {} requested Audit Permissions {}", authentication, pageRequest)

      val user = authenticationService.findUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()
      val page = auditPermissionService.fetchAll(pageRequest, user, locale)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/type{?pageRequest*}", processes = [APPLICATION_JSON])
   @AccessControl("auditPermissionType-fetchAll", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Fetch a listing of all Audit Permissions Types", description = "Fetch a listing of Audit Permissions", operationId = "auditPermissionType-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If there are Audit Permissions that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAllPermissionTypes(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      httpRequest: HttpRequest<*>
   ): Page<AuditPermissionTypeValueObject> {
      logger.debug("Fetching all audit permissions {}", pageRequest)

      val locale = httpRequest.findLocaleWithDefault()
      val page = auditPermissionService.fetchAllPermissionTypes(pageRequest, locale)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class)
   @AccessControl("auditPermission-create", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Create a single audit permission", description = "Create a single audit permission associated with a department and permission type.", operationId = "auditPermission-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to save Audit Permission", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditPermissionValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun create(
      @Body permission: AuditPermissionCreateUpdateDataTransferObject,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): AuditPermissionValueObject {
      logger.info("User {} requested creation of audit permission {}", authentication, permission)

      val user = authenticationService.findUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()

      return auditPermissionService.create(permission, user, locale)
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @AccessControl("auditPermission-update", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Update a single audit permission", description = "Update a single audit permission associations with a department or permission type.", operationId = "auditPermission-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update Audit Permission", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditPermissionValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @Body permission: AuditPermissionCreateUpdateDataTransferObject,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): AuditPermissionValueObject {
      logger.info("User {} requested update of audit permission {}", authentication, permission)

      val user = authenticationService.findUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()
      val result = auditPermissionService.update(permission, user, locale)

      logger.debug("Requested update of audit permission {} resulted in {}", permission, result)

      return result
   }

   @Delete(uri = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @AccessControl("auditPermission-delete", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Delete a single Audit Permission", description = "Delete a single Audit Permission by it's system generated primary key", operationId = "auditPermission-delete")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the Audit Permission was able to be deleted", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditPermissionValueObject::class))]),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested Audit Permission was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun delete(
      @Parameter(description = "Primary Key to lookup the Audit with", `in` = PATH) @QueryValue("id") id: Long,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): AuditPermissionValueObject {
      logger.debug("User {} requested Audit Permission by ID {}", authentication, id)

      val user = authenticationService.findUser(authentication)

      return auditPermissionService.deleteById(id, user.myDataset(), httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)
   }
}
