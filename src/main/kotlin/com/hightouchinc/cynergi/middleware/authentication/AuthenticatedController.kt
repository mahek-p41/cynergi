package com.hightouchinc.cynergi.middleware.authentication

import com.hightouchinc.cynergi.middleware.extensions.findLocaleWithDefault
import com.hightouchinc.cynergi.middleware.service.LocalizationService
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import javax.inject.Inject

/**
 * This controller exists to give the framework some place to redirect AJAX requests to
 */
@Controller("/api/authenticated")
class AuthenticatedController @Inject constructor(
   private val localizationService: LocalizationService
) {

   @Secured("isAnonymous()")
   @Get(produces = [MediaType.TEXT_PLAIN])
   fun loggedIn(authentication: Authentication?, httpRequest: HttpRequest<*>): HttpResponse<String> {
      val locale = httpRequest.findLocaleWithDefault()

      return if (authentication != null) {
         HttpResponse.ok(localizationService.localize(ErrorCodes.System.LOGGED_IN, locale, arrayOf(authentication.name)))
      } else {
         HttpResponse.status<String>(HttpStatus.UNAUTHORIZED)
            .body(localizationService.localize(ErrorCodes.System.NOT_LOGGED_IN, locale, emptyArray()))
      }
   }
}
