package com.cynergisuite.middleware.employee.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.employee.EmployeePageRequest
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.employee.EmployeeValueObject
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
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(IS_AUTHENTICATED)
@Controller("/api/employee")
class EmployeeController @Inject constructor(
   private val employeeService: EmployeeService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeController::class.java)

   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["EmployeeEndpoints"], summary = "Fetch a listing of Employees", description = "Fetch a paginated listing of Employee's associated with a User's company", operationId = "employee-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: EmployeePageRequest,
      authentication: Authentication
   ): Page<EmployeeValueObject> {
      logger.info("Fetching all employees {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = employeeService.fetchAll(pageRequest, user.myCompany())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      return page
   }
}
