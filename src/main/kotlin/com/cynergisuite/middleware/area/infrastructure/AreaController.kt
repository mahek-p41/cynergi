package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.area.AreaDTO
import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.authentication.user.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(IS_AUTHENTICATED)
@Controller("/api/area")
class AreaController @Inject constructor(
   private val userService: UserService,
   private val areaService: AreaService,
) {
   private val logger: Logger = LoggerFactory.getLogger(AreaController::class.java)

   @Get
   @Operation(tags = ["AreaEndpoints"], description = "Fetch the canonical structure of areas - menus - modules", operationId = "area-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AreaDTO::class))])
      ]
   )
   fun fetchAll(
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): List<AreaDTO> {
      val locale = httpRequest.findLocaleWithDefault()

      val user = userService.fetchUser(authentication)
      val areas = areaService.fetchAllVisibleWithMenusAndAreas(user.myCompany(), locale)

      logger.debug("Canonical structure of resulted in {}", areas)

      return areas
   }
}
