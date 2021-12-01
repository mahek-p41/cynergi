package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.CredentialsProvidedDidNotMatch
import com.cynergisuite.middleware.authentication.CredentialsRequireStore
import com.cynergisuite.middleware.authentication.LoginCredentials
import com.cynergisuite.middleware.authentication.UserAuthenticated
import com.cynergisuite.middleware.authentication.UserAuthenticatedAsAdmin
import com.cynergisuite.middleware.error.ErrorDTO
import com.cynergisuite.middleware.localization.AccessDeniedCredentialsDoNotMatch
import com.cynergisuite.middleware.localization.AccessDeniedStore
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.UNAUTHORIZED
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.endpoints.LoginController
import io.micronaut.security.event.LoginFailedEvent
import io.micronaut.security.event.LoginSuccessfulEvent
import io.micronaut.security.handlers.LoginHandler
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

/**
 * This class is provided in addition to the Micronaut LoginController to allow for a store to be overridden by the user
 * logging in.
 * <br />
 * It consumes a POST body that contains the store number that the user is requesting to login as.
 */
@Replaces(LoginController::class)
@Secured(IS_ANONYMOUS)
@Controller("/api/login")
class SystemLoginController @Inject constructor(
   private val eventPublisher: ApplicationEventPublisher<ApplicationEvent>,
   private val localizationService: LocalizationService,
   private val loginHandler: LoginHandler,
   private val userAuthenticationProvider: UserAuthenticationProvider,
) {
   private val logger: Logger = LoggerFactory.getLogger(SystemLoginController::class.java)

   @Post
   @Consumes(APPLICATION_JSON)
   @Operation(
      tags = ["AuthenticationEndpoints"],
      summary = "Login with a username, password and store number",
      description = "Allows for a different login process where the store associated with the user by default can be overridden",
      operationId = "store-login"
   )
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the login was successful"),
         ApiResponse(responseCode = "401", description = "If the login was not successful")
      ]
   )
   fun login(
      @Valid @Body loginCredentials: LoginCredentials,
      request: HttpRequest<*>
   ) : HttpResponse<*> {
      logger.debug("Store login attempted with {}", loginCredentials)

      val locale = request.findLocaleWithDefault()

      return when(val authentication = userAuthenticationProvider.authenticate(loginCredentials)) {
         is UserAuthenticated -> {
            eventPublisher.publishEvent(LoginSuccessfulEvent(authentication.user))

            loginHandler.loginSuccess(authentication.user, request)
         }
         is UserAuthenticatedAsAdmin -> {
            eventPublisher.publishEvent(LoginSuccessfulEvent(authentication.user))

            loginHandler.loginSuccess(authentication.user, request)
         }

         is CredentialsProvidedDidNotMatch -> {
            eventPublisher.publishEvent(LoginFailedEvent(authentication.identity))

            HttpResponse
               .status<ErrorDTO>(UNAUTHORIZED)
               .body(localizationService.localizeError(AccessDeniedCredentialsDoNotMatch(authentication.identity), locale))
         }

         is CredentialsRequireStore -> {
            eventPublisher.publishEvent(LoginFailedEvent(authentication.identity))

            HttpResponse
               .status<ErrorDTO>(UNAUTHORIZED)
               .body(localizationService.localizeError(AccessDeniedStore(authentication.identity), locale))
         }
      }
   }
}
