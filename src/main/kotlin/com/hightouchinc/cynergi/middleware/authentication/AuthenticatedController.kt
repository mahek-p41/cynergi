package com.hightouchinc.cynergi.middleware.authentication

import com.hightouchinc.cynergi.middleware.dto.MessageDto
import com.hightouchinc.cynergi.middleware.extensions.findLocaleWithDefault
import com.hightouchinc.cynergi.middleware.localization.MessageCodes
import com.hightouchinc.cynergi.middleware.service.LocalizationService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.UNAUTHORIZED
import io.micronaut.http.MediaType.APPLICATION_JSON
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
   @Get(produces = [APPLICATION_JSON])
   fun loggedIn(authentication: Authentication?, httpRequest: HttpRequest<*>): HttpResponse<MessageDto> {
      val locale = httpRequest.findLocaleWithDefault()

      return if (authentication != null) {
         val message = localizationService.localize(MessageCodes.System.LOGGED_IN, locale, arrayOf(authentication.name))

         HttpResponse.ok(MessageDto(message = message))
      } else {
         val message = localizationService.localize(MessageCodes.System.NOT_LOGGED_IN, locale, emptyArray())

         HttpResponse.status<MessageDto>(UNAUTHORIZED).body(MessageDto(message = message))
      }
   }
}
