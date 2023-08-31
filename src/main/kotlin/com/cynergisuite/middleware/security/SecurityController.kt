package com.cynergisuite.middleware.security

import com.cynergisuite.middleware.authentication.user.SecurityGroupDTO
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
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
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Secured(SecurityRule.IS_AUTHENTICATED, "MCFADMIN")
@Controller("/api/security")
class SecurityController @Inject constructor(
   private val userService: UserService,
   private val securityService: SecurityService,
   private val employeeService: EmployeeService
) {

   private val logger: Logger = LoggerFactory.getLogger(SecurityController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/company", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["SecurityGroupEndpoints"], summary = "Fetch a list of Security Groups By Company", description = "Fetch a list of Security Groups By Company", operationId = "securityGroup-fetchAllByCompany")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = SecurityGroupDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Security Groups were unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByCompany(
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<SecurityGroupDTO> {
      logger.info("Fetching all Security Groups by company")

      val user = userService.fetchUser(authentication)
      val response = securityService.findByCompany(user.myCompany())

      logger.debug("Fetching all Security Groups by {} resulted in {}", user.myCompany(), response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get(value = "/employee", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["SecurityGroupEndpoints"], summary = "Fetch a list of Security Groups By Employee", description = "Fetch a list of Security Groups By Employee", operationId = "securityGroup-fetchAllByEmployee")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = SecurityGroupDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Security Groups were unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByEmployee(
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<SecurityGroupDTO> {
      logger.info("Fetching all Security Groups by company")

      val user = userService.fetchUser(authentication)
      val response = securityService.findByEmployee(user.myId())

      logger.debug("Fetching all Security Groups by {} resulted in {}", user.myId(), response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get(value = "/security-group/{id:[0-9a-fA-F\\-]+}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["SecurityGroupEndpoints"], summary = "Fetch a list of Employees by Security Group", description = "Fetch a list of Employees by Security Group", operationId = "securityGroup-fetchEmployeesBySecurityGroup")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = SecurityGroupDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Employees were unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllEmployeesBySecurityGroup(
      @Parameter(name = "id", `in` = ParameterIn.PATH, description = "The id for the Security Group to be queried") @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<EmployeeValueObject> {
      logger.info("Fetching all Employees by Security Group")

      val user = userService.fetchUser(authentication)
      val response = employeeService.fetchAllBySecurityGroup(id, user.myCompany())

      logger.debug("Fetching all Employees by {} resulted in {}", user.myId(), response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get(value = "/access-point/{id:[0-9a-fA-F\\-]+}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["SecurityGroupEndpoints"], summary = "Fetch a list of Employees by Access Point", description = "Fetch a list of Employees by Access Point", operationId = "securityGroup-fetchEmployeesByAccessPoint")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = SecurityGroupDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Employees were unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllEmployeesByAccessPoint(
      @Parameter(name = "id", `in` = ParameterIn.PATH, description = "The id for the Access Point to be queried") @QueryValue("id")
      id: Int,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<EmployeeValueObject> {
      logger.info("Fetching all Employees by Access Point")

      val user = userService.fetchUser(authentication)
      val response = employeeService.fetchAllByAccessPoint(id, user.myCompany())

      logger.debug("Fetching all Employees by {} resulted in {}", user.myId(), response)

      return response
   }

   @Post(value = "/security-group", processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["SecurityGroupEndpoints"], summary = "Create a single Security Group", description = "Create a single Security Group", operationId = "securityGroup-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = SecurityGroupDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The Security Group was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: SecurityGroupDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): SecurityGroupDTO {
      logger.debug("Requested Create Security Group {}", dto)

      val user = userService.fetchUser(authentication)
      val response = securityService.create(dto, user.myCompany())

      logger.debug("Requested Create Security Group {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/security-group/{id:[0-9a-fA-F\\-]+}", processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["SecurityGroupEndpoints"], summary = "Update a single Security Group", description = "Update a single Security Group", operationId = "securityGroup-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = SecurityGroupDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Security Group was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = ParameterIn.PATH, description = "The id for the Security Group being updated") @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: SecurityGroupDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): SecurityGroupDTO {
      logger.info("Requested Update Security Group {}", dto)

      val user = userService.fetchUser(authentication)
      val response = securityService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Security Group {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/employee-to-security-group/{employeeId:[0-9a-fA-F\\-]+}", processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["SecurityGroupEndpoints"], summary = "Update an Employee to Security Group", description = "Update an Employee to Security Group", operationId = "securityGroup-updateEmployeeToSecurity")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = SecurityGroupDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Security Group was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun updateEmployeeToSecurityGroup(
      @Parameter(name = "employeeId", `in` = ParameterIn.PATH, description = "The id for the Employee being updated") @QueryValue("employeeId")
      employeeId: Long,
      @Body
      securityGroupId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<SecurityGroupDTO> {
      logger.info("Requested Update Employee to Security Group {}")

      val user = userService.fetchUser(authentication)
      val response = securityService.addEmployeeToSecurityGroup(employeeId, securityGroupId)

      logger.debug("Requested Update Employee to Security Group {} resulted in {}", response)

      return response
   }

   @Put(value = "/access-points/{id:[0-9a-fA-F\\-]+}", processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["SecurityGroupEndpoints"], summary = "Assign Access Points to a Security Group", description = "Assign Access Points to a Security Group", operationId = "securityGroup-assignAccessPoints")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = SecurityGroupDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Access Point was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun assignAccessPointToSecurityGroup(
      @Parameter(name = "id", `in` = ParameterIn.PATH, description = "The id for the Security Group being updated") @QueryValue("id")
      id: UUID,
      @Body
      accessPoints: List<Int>,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested Update Security Group to Security Access Points {}")

      val response = securityService.assignAccessPointsToSecurityGroups(id, accessPoints)

      logger.debug("Requested Update Access Points to a Security Group {} resulted in {}", response)
   }
}
