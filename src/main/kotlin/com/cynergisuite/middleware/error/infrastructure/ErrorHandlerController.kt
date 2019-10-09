package com.cynergisuite.middleware.error.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.error.ErrorValueObject
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.OperationNotPermittedException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.ConversionError
import com.cynergisuite.middleware.localization.InternalError
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotImplemented
import com.cynergisuite.middleware.localization.PageOutOfBounds
import com.cynergisuite.middleware.localization.RouteError
import com.cynergisuite.middleware.localization.UnableToParseJson
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.badRequest
import io.micronaut.http.HttpResponse.notFound
import io.micronaut.http.HttpResponse.serverError
import io.micronaut.http.HttpStatus.FORBIDDEN
import io.micronaut.http.HttpStatus.NOT_IMPLEMENTED
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Path

@Controller
class ErrorHandlerController @Inject constructor(
   private val localizationService: LocalizationService
) {
   private val logger: Logger = LoggerFactory.getLogger(ErrorHandlerController::class.java)

   @Error(global = true, exception = Throwable::class)
   fun allElseFailsExceptionHandler(httpRequest: HttpRequest<*>, throwable: Throwable): HttpResponse<ErrorValueObject> {
      logger.error("Unknown Error", throwable)

      val locale = httpRequest.findLocaleWithDefault()

      return serverError(ErrorValueObject(localizationService.localize(localizationCode = InternalError(), locale = locale)))
   }

   @Error(global = true, exception = JsonParseException::class)
   fun jsonParseExceptionHandler(httpRequest: HttpRequest<*>, exception: JsonParseException): HttpResponse<ErrorValueObject> {
      logger.error("Unable to parse request body", exception)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         ErrorValueObject(
            message = localizationService.localize(localizationCode = UnableToParseJson(exception.localizedMessage), locale = locale)
         )
      )
   }

   @Error(global = true, exception = IOException::class)
   fun inputOutputExceptionHandler(httpRequest: HttpRequest<*>, exception: IOException) {
      when (exception.message?.trim()?.toLowerCase()) {
         "an existing connection was forcibly closed by the remote host", "connection reset by peer" ->
            logger.warn("{} - {}:{}", exception.message, httpRequest.method, httpRequest.path)
         else ->
            logger.error("Unknown IOException occurred during request processing", exception)
      }
   }

   @Error(global = true, exception = NotImplementedError::class)
   fun notImplemented(httpRequest: HttpRequest<*>, exception: NotImplementedError): HttpResponse<ErrorValueObject> {
      logger.error("Endpoint not implemented", exception)

      val locale = httpRequest.findLocaleWithDefault()

      return HttpResponse
         .status<ErrorValueObject>(NOT_IMPLEMENTED)
         .body(ErrorValueObject(localizationService.localize(localizationCode = NotImplemented(httpRequest.path), locale = locale)))
   }

   @Error(global = true, exception = ConversionErrorException::class)
   fun conversionError(httpRequest: HttpRequest<*>, exception: ConversionErrorException): HttpResponse<ErrorValueObject> {
      logger.error("Unable to parse request body", exception)

      val locale = httpRequest.findLocaleWithDefault()
      val argument = exception.argument
      val conversionError = exception.conversionError
      val conversionErrorCause = conversionError.cause

      return when {
         conversionErrorCause is InvalidFormatException && conversionErrorCause.path.size > 0 && conversionErrorCause.value is String -> {
            processBadRequest(conversionErrorCause.path[0].fieldName, conversionErrorCause.value, locale)
         }

         conversionErrorCause is JsonMappingException -> {
            badRequest(
               ErrorValueObject(
                  message = localizationService.localize(ConversionError(argument.name, conversionError.originalValue.orElse(null)), locale),
                  path = conversionErrorCause.path.joinToString(".") { it.fieldName }
               )
            )
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
            message = localizationService.localize(messageKey = exception.messageTemplate, locale = locale, arguments = emptyArray()),
            path = exception.path
         )
      )
   }

   private fun processBadRequest(argumentName: String, argumentValue: Any?, locale: Locale): HttpResponse<ErrorValueObject> {
      return badRequest(
         ErrorValueObject(
            message = localizationService.localize(ConversionError(argumentName, argumentValue), locale),
            path = argumentName
         )
      )
   }

   @Error(global = true, exception = UnsatisfiedRouteException::class)
   fun unsatisfiedRouteException(httpRequest: HttpRequest<*>, exception: UnsatisfiedRouteException): HttpResponse<ErrorValueObject> {
      logger.trace("Unsatisfied Route Error", exception)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         ErrorValueObject(
            localizationService.localize(localizationCode = RouteError(exception.argument.name), locale = locale)
         )
      )
   }

   @Error(global = true, exception = PageOutOfBoundsException::class)
   fun pageOutOfBoundsExceptionHandler(httpRequest: HttpRequest<*>, exception: PageOutOfBoundsException): HttpResponse<ErrorValueObject> {
      logger.error("Page out of bounds was requested {}", exception.toString())

      val locale = httpRequest.findLocaleWithDefault()
      val pageRequest = exception.pageRequest

      return notFound(
         ErrorValueObject(
            localizationService.localize(PageOutOfBounds(pageRequest.page, pageRequest.size, pageRequest.sortBy, pageRequest.sortDirection), locale)
         )
      )
   }

   @Error(global = true, exception = NotFoundException::class)
   fun notFoundExceptionHandler(httpRequest: HttpRequest<*>, notFoundException: NotFoundException): HttpResponse<ErrorValueObject> {
      logger.trace("Not Found Error", notFoundException)

      val locale = httpRequest.findLocaleWithDefault()

      return notFound(
         ErrorValueObject(
            localizationService.localize(localizationCode = NotFound(notFoundException.notFound), locale = locale)
         )
      )
   }

   @Error(global = true, exception = ValidationException::class)
   fun validationException(httpRequest: HttpRequest<*>, validationException: ValidationException): HttpResponse<List<ErrorValueObject>> {
      logger.trace("Validation Error", validationException)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         validationException.errors.map { validationError: ValidationError ->
            ErrorValueObject(message = localizationService.localize(validationError.localizationCode, locale), path = validationError.path)
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

            ErrorValueObject(message = localizationService.localize(it.constraintDescriptor.messageTemplate, locale, arguments = arrayOf(field, value)), path = field)
         }
      )
   }

   @Error(global = true, exception = AccessException::class)
   fun accessExceptionHandler(httpRequest: HttpRequest<*>, accessException: AccessException) : HttpResponse<ErrorValueObject> {
      logger.info("Unauthorized exception", accessException)

      val locale = httpRequest.findLocaleWithDefault()

      return HttpResponse
         .status<ErrorValueObject>(FORBIDDEN)
         .body(ErrorValueObject(localizationService.localize(localizationCode = accessException.error, locale = locale)))
   }


   private fun buildPropertyPath(rootPath: Path): String =
      rootPath.asSequence()
         .filter {
            it.name != "save"
               && it.name != "create"
               && it.name != "update"
               && it.name != "dto"
               && it.name != "vo"
               && it.name != "fetchAll"
               && !it.name.startsWith("arg")
         }
         .joinToString(".")
}
