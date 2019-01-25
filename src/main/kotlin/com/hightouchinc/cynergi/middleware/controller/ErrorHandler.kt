package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.LocalizationService
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes
import io.micronaut.http.HttpHeaders.ACCEPT_LANGUAGE
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.badRequest
import io.micronaut.http.HttpResponse.notFound
import io.micronaut.http.HttpResponse.serverError
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.hateos.JsonError
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Locale
import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Path

@Controller
class ErrorHandler @Inject constructor(
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(ErrorHandler::class.java)

   @Error(global = true, exception = Throwable::class)
   fun allElseFailsExceptionHandler(httpRequest: HttpRequest<*>, throwable: Throwable): HttpResponse<JsonError> {
      logger.error("Unknown Error", throwable)

      val locale = findLocale(httpRequest)

      return serverError(JsonError(localizationService.localize(ErrorCodes.System.INTERNAL_ERROR, locale)))
   }

   @Error(global = true, exception = NotImplementedError::class)
   fun notImplemented(httpRequest: HttpRequest<*>, exception: NotImplementedError): HttpResponse<JsonError> {
      logger.error("Endpoint not implemented", exception)

      val locale = findLocale(httpRequest)

      return HttpResponse.status<JsonError>(HttpStatus.NOT_IMPLEMENTED).body(JsonError(localizationService.localize(ErrorCodes.System.NOT_IMPLEMENTED, locale, httpRequest.path)))
   }

   @Error(global = true, exception = UnsatisfiedRouteException::class)
   fun unsatisifedRouteException(httpRequest: HttpRequest<*>, exception: UnsatisfiedRouteException): HttpResponse<JsonError> {
      logger.trace("Unsatisfied Route Error", exception)

      val locale = findLocale(httpRequest)

      return badRequest(JsonError(localizationService.localize(ErrorCodes.System.REQUIRED_ARGUMENT, locale, exception.argument.name)))
   }

   @Error(global = true, exception = NotFoundException::class)
   fun notFoundExceptionHandler(httpRequest: HttpRequest<*>, notFoundException: NotFoundException): HttpResponse<JsonError> {
      logger.trace("Not Found Error", notFoundException)

      val locale = findLocale(httpRequest)

      return notFound(JsonError(localizationService.localize(ErrorCodes.System.NOT_FOUND, locale, notFoundException.notFound)))
   }

   @Error(global = true, exception = ValidationException::class)
   fun validationException(httpRequest: HttpRequest<*>, validationException: ValidationException): HttpResponse<List<JsonError>> {
      logger.trace("Validation Error", validationException)

      val locale = findLocale(httpRequest)

      return badRequest(
         validationException.errors.map {
            val jsonError = JsonError(localizationService.localize(it.messageTemplate, locale, it.arguments.toTypedArray()))

            jsonError.path(it.path)

            jsonError
         }
      )
   }

   @Error(global = true, exception = ConstraintViolationException::class)
   fun constraintViolationException(httpRequest: HttpRequest<*>, constraintViolationException: ConstraintViolationException): HttpResponse<List<JsonError>> {
      logger.trace("Constraint Violation Error", constraintViolationException)

      val locale = findLocale(httpRequest)

      return badRequest(
         constraintViolationException.constraintViolations.map {
            val field = buildPropertyPath(rootPath = it.propertyPath)
            val value = if (it.invalidValue != null) it.invalidValue else EMPTY // just use the empty string if invalidValue is null to make the varargs call to localize happy
            val jsonError = JsonError(localizationService.localize(it.constraintDescriptor.messageTemplate, locale, field, value))

            jsonError.path(field)

            jsonError
         }
      )
   }

   private fun buildPropertyPath(rootPath: Path): String =
      rootPath.asSequence()
         .filter { it.name != "save" && it.name != "update" && it.name != "dto" && !it.name.startsWith("arg") }
         .joinToString(".")

   private fun findLocale(httpRequest: HttpRequest<*>): Locale {
      return httpRequest.headers.findFirst(ACCEPT_LANGUAGE)
         .map { it.substringBefore(";") }
         .map { localizationService.localeFor(it) }
         .orElse(Locale.US)!!
   }
}
