package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.AuthenticatedUserInformation
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.localization.SystemCode.NotLoggedIn
import com.cynergisuite.middleware.localization.SystemCode.LoggedIn
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.UNAUTHORIZED
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Requires(env = ["local", "demo"])
@Controller("/api/authenticated")
class AuthenticatedController @Inject constructor(
   private val localizationService: LocalizationService
) {

   @Secured(IS_ANONYMOUS)
   @Get(produces = [APPLICATION_JSON])
   fun authenticated(authentication: Authentication?, httpRequest: HttpRequest<*>): HttpResponse<AuthenticatedUserInformation> {
      val locale = httpRequest.findLocaleWithDefault()

      return if (authentication != null) {
         val message = localizationService.localize(localizationCode = LoggedIn, locale = locale, arguments = arrayOf(authentication.name))

         HttpResponse
            .ok(AuthenticatedUserInformation(number = authentication.name, loginStatus = message))
      } else {
         val message = localizationService.localize(NotLoggedIn, locale, arguments = emptyArray())

         HttpResponse
            .status<AuthenticatedUserInformation>(UNAUTHORIZED)
            .body(AuthenticatedUserInformation(loginStatus = message))
      }
   }

   @Get("/check")
   @AccessControl("check")
   fun authenticationCheck(httpRequest: HttpRequest<*>): HttpResponse<Any> {
      return HttpResponse.ok()
   }
}
