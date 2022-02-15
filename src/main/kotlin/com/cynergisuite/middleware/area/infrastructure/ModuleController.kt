package com.cynergisuite.middleware.module.infrastructure

import com.cynergisuite.middleware.area.ModuleDTO
import com.cynergisuite.middleware.area.ModuleService
import com.cynergisuite.middleware.area.ModuleType
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid
import javax.validation.ValidationException

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/module")
class ModuleController @Inject constructor(
   private val userService: UserService,
   private val moduleService: ModuleService
) {
   private val logger: Logger = LoggerFactory.getLogger(ModuleController::class.java)

   @Post(uri = "/{id:[0-9]+}", processes = [MediaType.APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["ModuleEndpoints"], description = "Create module level for company.", operationId = "create-level-module")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully create module level for company"),
         ApiResponse(responseCode = "400", description = "If the module level was unable to be create"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun createLevelConfig(
      @QueryValue("id") id: Long,
      @Body @Valid
      moduleDTO: ModuleDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): ModuleType {
      val company = userService.fetchUser(authentication).myCompany()

      logger.info("Create module level config {} for company {}", moduleDTO.id, company.id)

      return moduleService.createLevelConfig(company, moduleDTO)
   }

   @Put(uri = "/{id:[0-9]+}", processes = [MediaType.APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["ModuleEndpoints"], description = "Update module level for company.", operationId = "update-level-module")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully update module level for company"),
         ApiResponse(responseCode = "400", description = "If the module level was unable to be updated"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun updateLevelConfig(
      @QueryValue("id") id: Long,
      @Body @Valid
      moduleDTO: ModuleDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): ModuleType {
      val company = userService.fetchUser(authentication).myCompany()

      logger.info("Update module level config {} for company {}", moduleDTO.id, company.id)

      return moduleService.updateLevelConfig(company, moduleDTO)
   }
}
