package com.cynergisuite.middleware.accounting.routine.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.routine.RoutineDateRangeDTO
import com.cynergisuite.middleware.accounting.routine.RoutineDTO
import com.cynergisuite.middleware.accounting.routine.RoutineService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/routine")
class RoutineController @Inject constructor(
   private val routineService: RoutineService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(RoutineController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["RoutineEndpoints"], summary = "Fetch a single Routine", description = "Fetch a single Routine by it's system generated primary key", operationId = "routine-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RoutineDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Routine was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Valid @QueryValue("id")
      id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): RoutineDTO {
      logger.info("Fetching Routine by {}", id)

      val user = userService.findUser(authentication)
      val response = routineService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Routine by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["RoutineEndpoints"], summary = "Fetch a listing of Routines", description = "Fetch a paginated listing of Routine", operationId = "routine-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Routine was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<RoutineDTO> {
      logger.info("Fetching all Routines {}", pageRequest)

      val user = userService.findUser(authentication)
      val page = routineService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(value = "/open-gl", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class)
   @Operation(tags = ["RoutineEndpoints"], summary = "Sets all GLAccounts Open to false", description = "Clears GLAccounts", operationId = "routine-open-gl")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RoutineDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun open(@Body @Valid dto: RoutineDateRangeDTO, authentication: Authentication, httpRequest: HttpRequest<*>) {

      val user = userService.findUser(authentication)
      return routineService.clearRoutineAccounts(dto.periodFrom, dto.periodTo, user.myCompany())
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["RoutineEndpoints"], summary = "Create a single Routine", description = "Create a single Routine", operationId = "routine-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RoutineDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The Routine was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: RoutineDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): RoutineDTO {
      logger.debug("Requested Create Routine {}", dto)

      val user = userService.findUser(authentication)
      val response = routineService.create(dto, user.myCompany())

      logger.debug("Requested Create Routine {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["RoutineEndpoints"], summary = "Update a single Routine", description = "Update a single Routine", operationId = "routine-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RoutineDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Routine was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the Routine being updated")
      @QueryValue("id")
      id: Long,
      @Body @Valid
      dto: RoutineDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): RoutineDTO {
      logger.info("Requested Update Routine {}", dto)

      val user = userService.findUser(authentication)
      val response = routineService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Routine {} resulted in {}", dto, response)

      return response
   }
}
