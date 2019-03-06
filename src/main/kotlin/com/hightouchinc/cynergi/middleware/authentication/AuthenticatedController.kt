package com.hightouchinc.cynergi.middleware.authentication

import com.hightouchinc.cynergi.middleware.extensions.findLocaleWithDefault
import com.hightouchinc.cynergi.middleware.service.LocalizationService
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import javax.inject.Inject

@Controller("/api/authenticated")
class AuthenticatedController @Inject constructor(
   private val localizationService: LocalizationService
) {

   @Secured("isAnonymous()")
   @Get(produces = [MediaType.TEXT_PLAIN])
   fun loggedIn(authentication: Authentication?, httpRequest: HttpRequest<*>): String {
      val locale = httpRequest.findLocaleWithDefault()

      return if (authentication != null) {
         localizationService.localize(ErrorCodes.System.LOGGED_IN, locale, arrayOf(authentication.name))
      } else {
         localizationService.localize(ErrorCodes.System.NOT_LOGGED_IN, locale, emptyArray())
      }
   }
}
