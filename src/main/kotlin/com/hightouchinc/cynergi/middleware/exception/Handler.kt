package com.hightouchinc.cynergi.middleware.exception

import com.hightouchinc.cynergi.middleware.domain.BadRequest
import com.hightouchinc.cynergi.middleware.domain.BadRequestField
import com.hightouchinc.cynergi.middleware.domain.NotFound
import com.hightouchinc.cynergi.middleware.service.LocalizationService
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes
import io.micronaut.http.*
import io.micronaut.http.HttpHeaders.*
import io.micronaut.http.HttpResponse.*
import io.micronaut.http.MediaType.*
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Produces
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Path

@Controller
class Handler(
   private val localizationService: LocalizationService
) {

   @Error(global = true)
   @Produces(APPLICATION_JSON)
   fun notFoundExceptionHandler(httpRequest: HttpRequest<*>, notFoundException: NotFoundException): HttpResponse<NotFound> {
      val locale = findLocale(httpRequest)

      return notFound(NotFound(localizationService.localize(ErrorCodes.System.NOT_FOUND, locale, notFoundException.notFound)))
   }

   @Error(global = true)
   @Produces(APPLICATION_JSON)
   fun constraintViolationException(httpRequest: HttpRequest<*>, constraintViolationException: ConstraintViolationException): HttpResponse<BadRequest> {
      val locale = findLocale(httpRequest)

      return badRequest(
         BadRequest(fields = constraintViolationException.constraintViolations.asSequence().map {
            val field = lastNode(it.propertyPath)

            BadRequestField(
               description = localizationService.localize(it.message, locale, field),
               field = field,
               value = it.invalidValue
            )
         }.toSet())
      ).contentType(APPLICATION_HAL_JSON_TYPE)
   }

   private fun findLocale(httpRequest: HttpRequest<*>): Locale {
      return httpRequest.headers.findFirst(ACCEPT_LANGUAGE)
         .map { it.substringBefore(";") }
         .map { localizationService.localeFor(it) }
         .orElse(Locale.US)!!
   }

   private fun lastNode(path: Path): String {
      var lastNode: Path.Node? = null

      for (node in path) {
         lastNode = node
      }

      return lastNode?.name ?: ""
   }
}
