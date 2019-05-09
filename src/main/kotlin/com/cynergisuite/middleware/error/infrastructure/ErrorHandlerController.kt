package com.cynergisuite.middleware.error.infrastructure

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.cynergisuite.middleware.error.ErrorValueObject
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.error.OperationNotPermittedException
import com.cynergisuite.middleware.localization.MessageCodes
import com.cynergisuite.middleware.localization.LocalizationFacade
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.badRequest
import io.micronaut.http.HttpResponse.notFound
import io.micronaut.http.HttpResponse.serverError
import io.micronaut.http.HttpStatus.NOT_IMPLEMENTED
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Locale
import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Path

@Controller
class ErrorHandlerController @Inject constructor(
   private val localizationService: LocalizationFacade
) {
   private val logger: Logger = LoggerFactory.getLogger(ErrorHandlerController::class.java)

   @Error(global = true, exception = Throwable::class)
   fun allElseFailsExceptionHandler(httpRequest: HttpRequest<*>, throwable: Throwable): HttpResponse<ErrorValueObject> {
      logger.error("Unknown Error", throwable)

      val locale = httpRequest.findLocaleWithDefault()

      return serverError(ErrorValueObject(localizationService.localize(MessageCodes.System.INTERNAL_ERROR, locale, emptyArray())))
   }

   @Error(global = true, exception = NotImplementedError::class)
   fun notImplemented(httpRequest: HttpRequest<*>, exception: NotImplementedError): HttpResponse<ErrorValueObject> {
      logger.error("Endpoint not implemented", exception)

      val locale = httpRequest.findLocaleWithDefault()

      return HttpResponse.status<ErrorValueObject>(NOT_IMPLEMENTED).body(ErrorValueObject(localizationService.localize(MessageCodes.System.NOT_IMPLEMENTED, locale, arrayOf(httpRequest.path))))
   }

   @Error(global = true, exception = ConversionErrorException::class)
   fun conversionError(httpRequest: HttpRequest<*>, exception: ConversionErrorException): HttpResponse<ErrorValueObject> {
      logger.error("Endpoint not implemented", exception)

      val locale = httpRequest.findLocaleWithDefault()
      val argument = exception.argument
      val conversionError = exception.conversionError
      val conversionErrorCause = conversionError.cause

      return when {
         conversionErrorCause is InvalidFormatException && conversionErrorCause.path.size > 0 && conversionErrorCause.value is String -> {
            processBadRequest(conversionErrorCause.path[0].fieldName, conversionErrorCause.value, locale)
         }

         else -> {
            processBadRequest(argument.name, conversionError.originalValue.orElse(null), locale)
         }
      }
   }

   @Error(global = true, exception = OperationNotPermittedException::class)
   fun operationNotPermitted(httpRequest: HttpRequest<*>, exception: OperationNotPermittedException): HttpResponse<ErrorValueObject> {
      logger.error("An operation that is not permitted was initiated", exception)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         ErrorValueObject(
            message = localizationService.localize(exception.messageTemplate, locale, emptyArray()),
            path = exception.path
         )
      )
   }

   private fun processBadRequest(argumentName: String, argumentValue: Any?, locale: Locale): HttpResponse<ErrorValueObject> {
      return badRequest(
         ErrorValueObject(
            message = localizationService.localize(MessageCodes.Cynergi.CONVERSION_ERROR, locale, arrayOf(argumentName, argumentValue)),
            path = argumentName
         )
      )
   }

   @Error(global = true, exception = UnsatisfiedRouteException::class)
   fun unsatisifedRouteException(httpRequest: HttpRequest<*>, exception: UnsatisfiedRouteException): HttpResponse<ErrorValueObject> {
      logger.trace("Unsatisfied Route Error", exception)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(ErrorValueObject(localizationService.localize(MessageCodes.System.REQUIRED_ARGUMENT, locale, arrayOf(exception.argument.name))))
   }

   @Error(global = true, exception = NotFoundException::class)
   fun notFoundExceptionHandler(httpRequest: HttpRequest<*>, notFoundException: NotFoundException): HttpResponse<ErrorValueObject> {
      logger.trace("Not Found Error", notFoundException)

      val locale = httpRequest.findLocaleWithDefault()

      return notFound(ErrorValueObject(localizationService.localize(MessageCodes.System.NOT_FOUND, locale, arrayOf(notFoundException.notFound))))
   }

   @Error(global = true, exception = ValidationException::class)
   fun validationException(httpRequest: HttpRequest<*>, validationException: ValidationException): HttpResponse<List<ErrorValueObject>> {
      logger.trace("Validation Error", validationException)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         validationException.errors.map {
            ErrorValueObject(message = localizationService.localize(it.messageTemplate, locale, it.arguments.toTypedArray()), path = it.path)
         }
      )
   }

   @Error(global = true, exception = ConstraintViolationException::class)
   fun constraintViolationException(httpRequest: HttpRequest<*>, constraintViolationException: ConstraintViolationException): HttpResponse<List<ErrorValueObject>> {
      logger.trace("Constraint Violation Error", constraintViolationException)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         constraintViolationException.constraintViolations.map {
            val field = buildPropertyPath(rootPath = it.propertyPath)
            val value = if (it.invalidValue != null) it.invalidValue else EMPTY // just use the empty string if invalidValue is null to make the varargs call to localize happy

            ErrorValueObject(message = localizationService.localize(it.constraintDescriptor.messageTemplate, locale, arrayOf(field, value)), path = field)
         }
      )
   }

   private fun buildPropertyPath(rootPath: Path): String =
      rootPath.asSequence()
         .filter {
            it.name != "save"
               && it.name != "create"
               && it.name != "update"
               && it.name != "dto"
               && it.name != "vo"
               && !it.name.startsWith("arg")
         }
         .joinToString(".")
}
