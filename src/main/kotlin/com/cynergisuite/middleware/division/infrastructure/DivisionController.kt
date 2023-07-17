package com.cynergisuite.middleware.division.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.division.DivisionDTO
import com.cynergisuite.middleware.division.DivisionService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
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
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid
import javax.validation.ValidationException

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/division")
class DivisionController @Inject constructor(
   private val userService: UserService,
   private val divisionService: DivisionService
) {
   private val logger: Logger = LoggerFactory.getLogger(DivisionController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["DivisionEndpoints"], summary = "Fetch a single Division", description = "Fetch a single Division by ID", operationId = "division-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = DivisionDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested Division was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): DivisionDTO {
      logger.info("Fetching Division by ID {}", id)

      val user = userService.fetchUser(authentication)
      val response = divisionService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching AuditDetail by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["DivisionEndpoints"], summary = "Fetch a list of divisions", description = "Fetch a list of divisions", operationId = "division-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))])
      ]
   )
   @Get(uri = "{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<DivisionDTO> {
      val user = userService.fetchUser(authentication)
      val divisions = divisionService.fetchAll(user.myCompany(), pageRequest)

      if (divisions.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of divisions resulted in {}", divisions)

      return divisions
   }

   @Secured("MCFDIVADD")
   @Post(processes = [MediaType.APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["DivisionEndpoints"], summary = "Create a single division", description = "Create a single division.", operationId = "division-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Division", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = DivisionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun save(
      @Body @Valid
      dto: DivisionDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): DivisionDTO {
      logger.info("Requested Save Division {}", dto)

      val user = userService.fetchUser(authentication)
      val response = divisionService.create(dto, user.myCompany())

      logger.debug("Requested Save Division {} resulted in {}", dto, response)

      return response
   }

   @Secured("MCFDIVCHG")
   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [MediaType.APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["DivisionEndpoints"], summary = "Update a single division", description = "Update a single division.", operationId = "division-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Division", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = DivisionDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested Division was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      dto: DivisionDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): DivisionDTO {
      logger.info("Requested Update Division {}", dto)

      val user = userService.fetchUser(authentication)
      val response = divisionService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Division {} resulted in {}", dto, response)

      return response
   }

   @Delete(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [MediaType.APPLICATION_JSON])
   @AccessControl
   @Operation(tags = ["DivisionEndpoints"], summary = "Delete a single Division", description = "Delete a single Division by it's system generated primary key", operationId = "division-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Division was able to be deleted", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = DivisionDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Division was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @Parameter(description = "Primary Key to delete the Division with", `in` = ParameterIn.PATH) @QueryValue("id")
      id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): DivisionDTO {
      logger.debug("User {} requested Division Deletion by ID {}", authentication, id)

      val user = userService.fetchUser(authentication)

      return divisionService.delete(id, user.myCompany()) ?: throw NotFoundException(id)
   }
}
