package com.cynergisuite.middleware.audit.permission.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.infrastructure.AuditAccessControlProvider
import com.cynergisuite.middleware.audit.permission.AuditPermissionCreateDTO
import com.cynergisuite.middleware.audit.permission.AuditPermissionService
import com.cynergisuite.middleware.audit.permission.AuditPermissionTypeValueObject
import com.cynergisuite.middleware.audit.permission.AuditPermissionValueObject
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
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
@Controller("/api/audit/permission")
class AuditPermissionController @Inject constructor(
   private val auditPermissionService: AuditPermissionService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditPermissionController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @AccessControl("audit-permission-manager", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Fetch a single Audit Permission", description = "Fetch a single Audit Permission by it's system generated primary key", operationId = "auditPermission-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Audit Permission was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditPermissionValueObject::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Audit Permission was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Audit with", `in` = PATH) @QueryValue("id")
      id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): AuditPermissionValueObject {
      logger.debug("User {} requested Audit Permission by ID {}", authentication, id)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()

      return auditPermissionService.fetchById(id, user.myCompany(), locale) ?: throw NotFoundException(id)
   }

   @Throws(PageOutOfBoundsException::class)
   @Get("{?pageRequest*}", processes = [APPLICATION_JSON])
   @AccessControl("audit-permission-manager", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Fetch a single Audit Permission", description = "Fetch a listing of Audit Permissions", operationId = "auditPermission-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If there are Audit Permissions that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): Page<AuditPermissionValueObject> {
      logger.debug("User {} requested Audit Permissions {}", authentication, pageRequest)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()
      val page = auditPermissionService.fetchAll(pageRequest, user, locale)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/type/{typeId:[0-9]+}{?pageRequest*}", processes = [APPLICATION_JSON])
   @AccessControl("audit-permission-manager", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Fetch a listing of all Audit Permissions of a certain Type", description = "Fetch a listing of Audit Permissions of a certain Type", operationId = "auditPermission-fetchAllByType")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If there are Audit Permissions that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByType(
      @Parameter(description = "Type Id to filter the audits", `in` = PATH) @QueryValue("typeId")
      typeId: Long,
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): Page<AuditPermissionValueObject> {
      logger.debug("User {} requested Audit Permissions {} of TypeId {}", authentication, pageRequest, typeId)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()
      val page = auditPermissionService.fetchAllByType(typeId, pageRequest, user, locale)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/type{?pageRequest*}", processes = [APPLICATION_JSON])
   @AccessControl("audit-permission-manager", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Fetch a listing of all Audit Permissions Types", description = "Fetch a listing of Audit Permissions", operationId = "auditPermissionType-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If there are Audit Permissions that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllPermissionTypes(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
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
   @AccessControl("audit-permission-manager", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Create a single audit permission", description = "Create a single audit permission associated with a department and permission type.", operationId = "auditPermission-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Audit Permission", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditPermissionValueObject::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      permission: AuditPermissionCreateDTO,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): AuditPermissionValueObject {
      logger.info("User {} requested creation of audit permission {}", authentication, permission)

      val user = userService.fetchUser(authentication)
      val locale = httpRequest.findLocaleWithDefault()

      return auditPermissionService.create(permission, user, locale)
   }

   @Delete(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @AccessControl("audit-permission-manager", accessControlProvider = AuditAccessControlProvider::class)
   @Operation(tags = ["AuditPermissionEndpoints"], summary = "Delete a single Audit Permission", description = "Delete a single Audit Permission by it's system generated primary key", operationId = "auditPermission-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Audit Permission was able to be deleted", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditPermissionValueObject::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Audit Permission was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @Parameter(description = "Primary Key to delete the Audit Permission with", `in` = PATH) @QueryValue("id")
      id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): AuditPermissionValueObject {
      logger.debug("User {} requested Audit Permission Deletion by ID {}", authentication, id)

      val user = userService.fetchUser(authentication)

      return auditPermissionService.deleteById(id, user.myCompany(), httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(id)
   }
}
