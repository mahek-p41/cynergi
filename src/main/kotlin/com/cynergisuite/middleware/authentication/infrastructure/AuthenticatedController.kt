package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AuthenticatedUserInformation
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.company.CompanyValueObject
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Head
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/authenticated")
class AuthenticatedController @Inject constructor(
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticatedController::class.java)

   @Get(produces = [APPLICATION_JSON])
   @Operation(tags = ["AuthenticationEndpoints"], summary = "Check authentication credentials and list claims", operationId = "authenticated")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the authentication token is valid", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuthenticatedUserInformation::class))]),
      ApiResponse(responseCode = "401", description = "For any other reason that this endpoint can't be accessed, example the token has expired or is not a valid token", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuthenticatedUserInformation::class))])
   ])
   fun authenticated(authentication: Authentication, httpRequest: HttpRequest<*>): HttpResponse<AuthenticatedUserInformation> {
      logger.debug("Checking authentication {}", authentication)

      var user = userService.findUser(authentication)
         val company = user.myCompany()
         val department = user.myDepartment()
         var companyWithNullFederalIdNumber = CompanyValueObject(company = company)
         val permissions = when {
            user.isCynergiAdmin() -> {
               userService.fetchAllPermissions()
            }
            department != null -> {
               userService.fetchPermissions(department)
            }
            else -> {
               emptySet()
            }
         }

         logger.debug("User is authenticated {}", user)

         HttpResponse.ok(AuthenticatedUserInformation(user, permissions, companyWithNullFederalIdNumber))
      } else {
         logger.debug("User was not authenticated")

         HttpResponse
            .status<AuthenticatedUserInformation>(UNAUTHORIZED)
            .body(AuthenticatedUserInformation())
      }
   }

   @Head("/check")
   @Operation(tags = ["AuthenticationEndpoints"], summary = "Check if an authentication is valid", description = "Simple HEAD operation that allows for checking if a token is valid or not.  Useful to check on application load if a stored token is still valid.", operationId = "authenticated-check")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If the authentication token is valid"),
      ApiResponse(responseCode = "401", description = "For any other reason that this endpoint can't be accessed, example the token has expired or is not a valid token")
   ])
   fun authenticationCheck(): HttpResponse<Any> {
      return HttpResponse.ok()
   }
}
