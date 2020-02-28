package com.cynergisuite.middleware.shipvia.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.infrastructure.AlwaysAllowAccessControlProvider
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.shipvia.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.micronaut.validation.Validated
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
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/shipvia")
class ShipViaController @Inject constructor(
   private val shipViaService: ShipViaService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("shipvia-fetchOne", accessControlProvider = AlwaysAllowAccessControlProvider::class) // FIXME change this to using the default once the Cynergi permission system is implemented there
   @Get("/{id}", produces = [APPLICATION_JSON])
   @Operation(tags = ["ShipViaEndpoints"], summary = "Fetch a single Ship Via", description = "Fetch a single Ship Via by it's system generated primary key", operationId = "shipvia-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the Ship Via was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ShipViaValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested Ship Via was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Ship Via with", `in` = PATH) @QueryValue("id") id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): ShipViaValueObject {
      logger.info("Fetching ShipVia by {}", id)

      val user = userService.findUser(authentication)
      val response = shipViaService.fetchById(id = id) ?: throw NotFoundException(id)

      logger.debug("Fetch ShipVia by {} resulted {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("shipvia-fetchAll", accessControlProvider = AlwaysAllowAccessControlProvider::class) // FIXME change this to using the default once the Cynergi permission system is implemented there
   @Get(value = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["ShipViaEndpoints"], summary = "Fetch a listing of Ship Vias", description = "Fetch a paginated listing of Ship Vias", operationId = "shipvia-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If there are Ship Vias that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested Ship Via was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @Valid @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<ShipViaValueObject> {
      val user = userService.findUser(authentication)
      val page =  shipViaService.fetchAll(pageRequest, user.myCompany())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @AccessControl("audit-create", accessControlProvider = AlwaysAllowAccessControlProvider::class) // FIXME change this to using the default once the Cynergi permission system is implemented there
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["ShipViaEndpoints"], summary = "Create a single ship via", description = "Create a single ship via.", operationId = "shipvia-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to save Ship Via", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ShipViaValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested Ship Via was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun save(
      @Body vo: ShipViaValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): ShipViaValueObject {
      logger.info("Requested Save ShipVia {}", vo)

      val user = userService.findUser(authentication)
      val response = shipViaService.create(vo = vo, employee = user)

      logger.debug("Requested Save ShipVia {} resulted in {}", vo, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @AccessControl("audit-update", accessControlProvider = AlwaysAllowAccessControlProvider::class) // FIXME change this to using the default once the Cynergi permission system is implemented there
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["ShipViaEndpoints"], summary = "Create a single ship via", description = "Create a single ship via.", operationId = "shipvia-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update Ship Via", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ShipViaValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested Ship Via was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @Body vo: ShipViaValueObject,
      authentication: Authentication
   ): ShipViaValueObject {
      logger.info("Requested Update ShipVia {}", vo)

      val employee = userService.findUser(authentication)
      val response = shipViaService.update(vo, employee)

      logger.debug("Requested Update ShipVia {} resulted in {}", vo, response)

      return response
   }

}
