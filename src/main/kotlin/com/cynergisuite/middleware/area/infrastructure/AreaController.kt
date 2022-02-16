package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.domain.SimpleTypeDomainDTO
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.area.AreaDTO
import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.area.AreaTypeService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
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
import javax.validation.ValidationException

@Secured(IS_AUTHENTICATED)
@Controller("/api/area")
class AreaController @Inject constructor(
   private val userService: UserService,
   private val areaService: AreaService,
   private val areaTypeService: AreaTypeService,
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

   @Post(processes = [MediaType.APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AreaEndpoints"], description = "Enable area for company.", operationId = "enable-area")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully enable area for company"),
         ApiResponse(responseCode = "400", description = "If the area was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun enableArea(
      @Body areaIdDTO: SimpleTypeDomainDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      val company = userService.fetchUser(authentication).myCompany()

      logger.info("Enable area {} for company {}", areaIdDTO.id, company.id)

      areaService.enableArea(company, areaIdDTO.id!!)
   }

   @Delete(uri = "/{id:[0-9]+}")
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AreaEndpoints"], description = "Disable area for company.", operationId = "disable-area")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully disable area for company"),
         ApiResponse(responseCode = "400", description = "If the area was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun disableArea(
      id: Int,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      val company = userService.fetchUser(authentication).myCompany()
      val areaType = areaTypeService.findById(id) ?: throw NotFoundException(id)

      logger.info("Disable area {} for company {}", id, company.id)

      areaService.disableArea(company, areaType)
   }
}
