package com.cynergisuite.middleware.vendor.group.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.vendor.group.VendorGroupDTO
import com.cynergisuite.middleware.vendor.group.VendorGroupService
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
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/vendor/group")
class VendorGroupController @Inject constructor(
   private val vendorGroupService: VendorGroupService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorGroupController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorGroupEndpoints"], summary = "Fetch a single VendorGroup", description = "Fetch a single VendorGroup by it's system generated primary key", operationId = "vendorGroup-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorGroupDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested VendorGroup was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorGroupDTO {
      logger.info("Fetching VendorGroup by {}", id)

      val user = userService.fetchUser(authentication)
      val response = vendorGroupService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching VendorGroup by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorGroupEndpoints"], summary = "Fetch a listing of VendorGroups", description = "Fetch a paginated listing of VendorGroups including associated schedule records", operationId = "vendorGroup-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested VendorGroup was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<VendorGroupDTO> {
      logger.info("Fetching all VendorGroups {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = vendorGroupService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorGroupEndpoints"], summary = "Create a single VendorGroup", description = "Create a single VendorGroup. The logged in Employee is used for the scannedBy property", operationId = "vendorGroup-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorGroupDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The VendorGroup was unable to be found or the scanArea was unknown"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      vo: VendorGroupDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorGroupDTO {
      logger.debug("Requested Create VendorGroup {}", vo)

      val user = userService.fetchUser(authentication)
      val response = vendorGroupService.create(vo, user.myCompany())

      logger.debug("Requested Create VendorGroup {} resulted in {}", vo, response)

      return response
   }

   @Put(value = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorGroupEndpoints"], summary = "Update a single VendorGroup", description = "Update a single VendorGroup where the update is the addition of a note", operationId = "vendorGroup-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorGroupDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested VendorGroup was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the VendorGroup being updated") @QueryValue("id")
      id: UUID,
      @Body @Valid
      vo: VendorGroupDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorGroupDTO {
      logger.info("Requested Update VendorGroup {}", vo)

      val user = userService.fetchUser(authentication)
      val response = vendorGroupService.update(id, vo, user.myCompany())

      logger.debug("Requested Update VendorGroup {} resulted in {}", vo, response)

      return response
   }

   @Delete(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @AccessControl
   @Operation(tags = ["VendorGroupEndpoints"], summary = "Delete a vendor group", description = "Delete a single vendor group", operationId = "vendorGroup-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the vendor group was able to be deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "409", description = "If the vendor group is still referenced from other tables"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete vendor group", authentication)

      val user = userService.fetchUser(authentication)

      return vendorGroupService.delete(id, user.myCompany())
   }
}
