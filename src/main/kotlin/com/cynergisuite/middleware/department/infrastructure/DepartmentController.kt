package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.AuthenticationService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.department.DepartmentService
import com.cynergisuite.middleware.department.DepartmentValueObject
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/department")
class DepartmentController @Inject constructor(
   private val authenticationService: AuthenticationService,
   private val departmentService: DepartmentService
) {
   private val logger: Logger = LoggerFactory.getLogger(DepartmentController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("department-fetchOne")
   @Get(uri = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["DepartmentEndpoints"], summary = "Fetch a single department", description = "Fetch a single department by it's system generated primary key", operationId = "audit-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the department was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = DepartmentValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested department was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the department with", `in` = ParameterIn.PATH) @QueryValue("id") id: Long
   ): DepartmentValueObject {
      logger.info("Fetching department by {}", id)

      val response = departmentService.fetchOne(id) ?: throw NotFoundException(id)

      logger.debug("Fetching department by {} resulted in {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("department-fetchAll")
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["DepartmentEndpoints"], summary = "Fetch a listing of departments", description = "Fetch a paginated listing of departments", operationId = "department-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      authentication: Authentication
   ): Page<DepartmentValueObject> {
      logger.info("Fetching all departments {}", pageRequest)

      val user = authenticationService.findUser(authentication)
      val page = departmentService.fetchAll(pageRequest, user.myDataset())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      return page
   }
}
