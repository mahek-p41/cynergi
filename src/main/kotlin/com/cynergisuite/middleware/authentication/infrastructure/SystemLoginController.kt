package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.LoginCredentials
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.endpoints.LoginController
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.micronaut.validation.Validated
import io.reactivex.Single
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

/**
 * This class is provided in addition to the Micronaut LoginController to allow for a store to be overridden by the user
 * logging in.
 * <br />
 * It consumes a POST body that contains the store number that the user is requesting to login as.
 */
@Validated
@Secured(IS_ANONYMOUS)
@Controller("/api/login")
class SystemLoginController(
   private val loginController: LoginController // proxying this controller's path and slightly different payload to the existing LoginController
) {
   private val logger: Logger = LoggerFactory.getLogger(SystemLoginController::class.java)

   @Post
   @Consumes(APPLICATION_JSON)
   @Operation(tags = ["AuthenticationEndpoints"], summary = "Login with a username, password and store number", description = "Allows for a different login process where the store associated with the user by default can be overridden", operationId = "store-login")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the login was successful"),
         ApiResponse(responseCode = "401", description = "If the login was not successful")
      ]
   )
   fun login(
      @Valid @Body
      loginCredentials: LoginCredentials,
      request: HttpRequest<*>
   ): Single<HttpResponse<*>> {
      logger.debug("Store login attempted with {}", loginCredentials)

      return loginController.login(loginCredentials, request)
   }
}
