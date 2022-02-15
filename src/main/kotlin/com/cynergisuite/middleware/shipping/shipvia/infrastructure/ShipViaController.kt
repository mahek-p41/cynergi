package com.cynergisuite.middleware.shipping.shipvia.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.shipping.shipvia.ShipViaDTO
import com.cynergisuite.middleware.shipping.shipvia.ShipViaService
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
@Controller("/api/shipping/shipvia")
class ShipViaController @Inject constructor(
   private val shipViaService: ShipViaService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaController::class.java)

   @Throws(NotFoundException::class)
   @Get("/{id}", produces = [APPLICATION_JSON])
   @Operation(tags = ["ShipViaEndpoints"], summary = "Fetch a single Ship Via", description = "Fetch a single Ship Via by it's system generated primary key", operationId = "shipvia-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Ship Via was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ShipViaDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested Ship Via was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Ship Via with", `in` = PATH) @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): ShipViaDTO {
      logger.info("Fetching ShipVia by {}", id)

      val user = userService.fetchUser(authentication)
      val response = shipViaService.fetchById(id = id, company = user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetch ShipVia by {} resulted {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(value = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["ShipViaEndpoints"], summary = "Fetch a listing of Ship Vias", description = "Fetch a paginated listing of Ship Vias", operationId = "shipvia-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If there are Ship Vias that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Ship Via was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<ShipViaDTO> {
      val user = userService.fetchUser(authentication)
      val page = shipViaService.fetchAll(pageRequest, user.myCompany())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Delete(uri = "/{id:[0-9a-fA-F\\-]+}")
   @Operation(tags = ["ShipViaEndpoints"], summary = "Delete a single ship via", description = "Deletes a ship via based on passed id", operationId = "shipvia-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the ship via record was deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling the endpoint does not have permission"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete ship via", authentication)

      val user = userService.fetchUser(authentication)

      return shipViaService.delete(id, user.myCompany())
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["ShipViaEndpoints"], summary = "Create a single ship via", description = "Create a single ship via.", operationId = "shipvia-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Ship Via", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ShipViaDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested Ship Via was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun save(
      @Body @Valid
      vo: ShipViaDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): ShipViaDTO {
      logger.info("Requested Save ShipVia {}", vo)

      val user = userService.fetchUser(authentication)
      val response = shipViaService.create(dto = vo, company = user.myCompany())

      logger.debug("Requested Save ShipVia {} resulted in {}", vo, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["ShipViaEndpoints"], summary = "Create a single ship via", description = "Create a single ship via.", operationId = "shipvia-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Ship Via", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ShipViaDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested Ship Via was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Body @Valid
      vo: ShipViaDTO,
      authentication: Authentication
   ): ShipViaDTO {
      logger.info("Requested Update ShipVia {}", vo)

      val employee = userService.fetchUser(authentication)
      val response = shipViaService.update(vo, employee.myCompany())

      logger.debug("Requested Update ShipVia {} resulted in {}", vo, response)

      return response
   }
}
