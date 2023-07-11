package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.area.AreaDTO
import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.area.findAreaType
import com.cynergisuite.middleware.area.toAreaTypeEntity
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
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
import javax.validation.Valid

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

   @Put(processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AreaEndpoints"], summary = "Update a company's Areas", description = "Update a company's Areas", operationId = "area-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AreaDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerJournal was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Body @Valid
      areaDTO: AreaDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested Update Area {}", areaDTO)

      val user = userService.fetchUser(authentication)
      val areaType = findAreaType(areaDTO.value!!)

      if (areaService.isEnabledFor(user.myCompany(), areaType)) {
         areaService.disableArea(user.myCompany(), areaType.toAreaTypeEntity())
      } else {
        areaService.enableArea(user.myCompany(), areaType.id)
      }

      logger.debug("Requested Update Area with ID {} resulted in {}", areaDTO)

   }

   @Post(processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AreaEndpoints"], summary = "Enable a company's Areas", description = "Enable a company's Areas", operationId = "area-enable")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AreaDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "404", description = "The requested Area was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun enableArea(
      @Body @Valid
      areaDTO: AreaDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested Update Area {}", areaDTO)

      val user = userService.fetchUser(authentication)
      val areaType = findAreaType(areaDTO.value!!)

      if (!areaService.isEnabledFor(user.myCompany(), areaType)) {
         areaService.enableArea(user.myCompany(), areaType.id)
      }

      logger.debug("Requested enable Area with ID {} resulted in {}", areaDTO)
   }

   @Delete(processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AreaEndpoints"], summary = "Disable a company's Areas", description = "Disable a company's Areas", operationId = "area-disable")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AreaDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "404", description = "The requested Area was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun disableArea(
      @Body @Valid
      areaDTO: AreaDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested Update Area {}", areaDTO)

      val user = userService.fetchUser(authentication)
      val areaType = findAreaType(areaDTO.value!!)

      if (areaService.isEnabledFor(user.myCompany(), areaType)) {
         areaService.disableArea(user.myCompany(), areaType.toAreaTypeEntity())
      }

      logger.debug("Requested disable Area with ID {} resulted in {}", areaType)
   }
}
