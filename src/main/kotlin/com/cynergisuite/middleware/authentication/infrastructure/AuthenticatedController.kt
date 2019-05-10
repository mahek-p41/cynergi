package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.AuthenticatedUserInformation
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.localization.MessageCodes.System.LOGGED_IN
import com.cynergisuite.middleware.localization.MessageCodes.System.NOT_LOGGED_IN
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.UNAUTHORIZED
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import javax.inject.Inject

@Requires(env = ["local", "demo"])
@Controller("/api/authenticated")
class AuthenticatedController @Inject constructor(
   private val localizationService: LocalizationService
) {

   @Secured("isAnonymous()")
   @Get(produces = [APPLICATION_JSON])
   fun authenticated(authentication: Authentication?, httpRequest: HttpRequest<*>): HttpResponse<AuthenticatedUserInformation> {
      val locale = httpRequest.findLocaleWithDefault()

      return if (authentication != null) {
         val message = localizationService.localize(LOGGED_IN, locale, arrayOf(authentication.name))

         HttpResponse
            .ok(AuthenticatedUserInformation(number = authentication.name, loginStatus = message))
      } else {
         val message = localizationService.localize(NOT_LOGGED_IN, locale, emptyArray())

         HttpResponse
            .status<AuthenticatedUserInformation>(UNAUTHORIZED)
            .body(AuthenticatedUserInformation(loginStatus = message))
      }
   }
}
