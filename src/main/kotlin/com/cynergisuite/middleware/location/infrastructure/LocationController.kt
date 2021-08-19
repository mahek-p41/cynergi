package com.cynergisuite.middleware.location.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.location.LocationDTO
import com.cynergisuite.middleware.location.LocationService
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
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
@Controller("/api/location")
class LocationController @Inject constructor(
   private val locationService: LocationService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(LocationController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["LocationEndpoints"], summary = "Fetch a single Location", description = "Fetch a single Location by it's system generated primary key", operationId = "location-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = LocationDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested Location was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Location with", `in` = PATH) @QueryValue("id")
      id: Long,
      authentication: Authentication
   ): LocationDTO {
      logger.info("Fetching Location by id {}", id)

      val user = userService.fetchUser(authentication)
      val response = locationService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Location by {} resulted in {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["LocationEndpoints"], summary = "Fetch a listing of Locations", description = "Fetch a paginated listing of Locations", operationId = "location-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication
   ): Page<LocationDTO> {
      logger.info("Fetching all locations {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = locationService.fetchAll(pageRequest, user.myCompany())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      return page
   }
}
