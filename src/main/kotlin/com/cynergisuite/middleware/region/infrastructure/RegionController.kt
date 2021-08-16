package com.cynergisuite.middleware.region.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.region.RegionDTO
import com.cynergisuite.middleware.region.RegionService
import com.cynergisuite.middleware.store.StoreDTO
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
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.ValidationException

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/region")
class RegionController @Inject constructor(
   private val userService: UserService,
   private val regionService: RegionService
) {
   private val logger: Logger = LoggerFactory.getLogger(RegionController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["RegionEndpoints"], summary = "Fetch a single Region", description = "Fetch a single Region by ID", operationId = "region-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RegionDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested Region was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): RegionDTO {
      logger.info("Fetching Region by ID {}", id)

      val user = userService.fetchUser(authentication)
      val response = regionService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching AuditDetail by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["RegionEndpoints"], summary = "Fetch a list of regions", description = "Fetch a list of regions", operationId = "region-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))])
      ]
   )
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<RegionDTO> {
      val user = userService.fetchUser(authentication)
      val regions = regionService.fetchAll(user.myCompany(), pageRequest)

      if (regions.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of regions resulted in {}", regions)

      return regions
   }

   @Post(processes = [APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["RegionEndpoints"], summary = "Create a single region", description = "Create a single region.", operationId = "region-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Region", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RegionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun save(
      @Body @Valid
      dto: RegionDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): RegionDTO {
      logger.info("Requested Save Region {}", dto)

      val user = userService.fetchUser(authentication)
      val response = regionService.create(dto, user.myCompany())

      logger.debug("Requested Save Region {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["RegionEndpoints"], summary = "Update a single region", description = "Update a single region.", operationId = "region-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Region", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RegionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested Region was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      dto: RegionDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): RegionDTO {
      logger.info("Requested Update Region {}", dto)

      val user = userService.fetchUser(authentication)
      val response = regionService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Region {} resulted in {}", dto, response)

      return response
   }

   @Delete(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @AccessControl
   @Operation(tags = ["RegionEndpoints"], summary = "Delete a single Region", description = "Delete a single Region by it's system generated primary key", operationId = "region-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Region was able to be deleted", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RegionDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Region was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @Parameter(description = "Primary Key to delete the Region with", `in` = ParameterIn.PATH) @QueryValue("id")
      id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): RegionDTO {
      logger.debug("User {} requested Region Deletion by ID {}", authentication, id)

      val user = userService.fetchUser(authentication)

      return regionService.delete(id, user.myCompany()) ?: throw NotFoundException(id)
   }

   @Post(uri = "/{regionId:[0-9a-fA-F\\-]+}/store", processes = [APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["RegionEndpoints"], summary = "Assign a store to region", description = "Assign a store to region.", operationId = "region-assign-store")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to assign a store to region", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = StoreDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun assignStore(
      @QueryValue("regionId") regionId: UUID,
      @Body storeDTO: SimpleLegacyIdentifiableDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested assign a Store to Region {}", storeDTO)

      val user = userService.fetchUser(authentication)

      regionService.assignStoreToRegion(regionId, storeDTO, user.myCompany())
   }

   @Delete(uri = "/{regionId:[0-9a-fA-F\\-]+}/store/{storeId:[0-9]+}", produces = [APPLICATION_JSON])
   @AccessControl
   @Operation(tags = ["RegionEndpoints"], summary = "Disassociate a store from region", description = "Disassociate a store from region", operationId = "region-disassociate-store")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Region was able to be deleted", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RegionDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Region was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun disassociateStore(
      @QueryValue("regionId") regionId: UUID,
      @QueryValue("storeId") storeId: Long,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.info("Requested disassociate a Store {} from Region {}", storeId, regionId)

      val user = userService.fetchUser(authentication)

      regionService.disassociateStoreFromRegion(regionId, storeId, user.myCompany())
   }
}
