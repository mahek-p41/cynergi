package com.cynergisuite.middleware.error.infrastructure

import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.extensions.isDigits
import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.error.ErrorDTO
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.OperationNotPermittedException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AccessDenied
import com.cynergisuite.middleware.localization.AccessDeniedCredentialsDoNotMatch
import com.cynergisuite.middleware.localization.AccessDeniedStore
import com.cynergisuite.middleware.localization.ConversionError
import com.cynergisuite.middleware.localization.InternalError
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotImplemented
import com.cynergisuite.middleware.localization.NotLoggedIn
import com.cynergisuite.middleware.localization.RouteError
import com.cynergisuite.middleware.localization.UnableToParseJson
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.badRequest
import io.micronaut.http.HttpResponse.noContent
import io.micronaut.http.HttpResponse.notFound
import io.micronaut.http.HttpResponse.serverError
import io.micronaut.http.HttpStatus.CONFLICT
import io.micronaut.http.HttpStatus.FORBIDDEN
import io.micronaut.http.HttpStatus.NOT_IMPLEMENTED
import io.micronaut.http.HttpStatus.UNAUTHORIZED
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.security.authentication.AuthenticationException
import io.micronaut.security.authentication.AuthorizationException
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import org.apache.commons.lang3.StringUtils.EMPTY
import org.postgresql.util.PSQLException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
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

   @Error(global = true, exception = JsonParseException::class)
   fun jsonParseExceptionHandler(httpRequest: HttpRequest<*>, exception: JsonParseException): HttpResponse<ErrorDTO> {
      logger.warn("Unable to parse request body", exception)

      val locale = httpRequest.findLocaleWithDefault()
      val localizationCode = UnableToParseJson(exception.localizedMessage)

      return badRequest(
         ErrorDTO(
            message = localizationService.localize(localizationCode = localizationCode, locale = locale),
            code = localizationCode.getCode()
         )
      )
   }

   @Error(global = true, exception = NotImplementedError::class)
   fun notImplemented(httpRequest: HttpRequest<*>, exception: NotImplementedError): HttpResponse<ErrorDTO> {
      logger.warn("Endpoint not implemented", exception)

      val locale = httpRequest.findLocaleWithDefault()

      return HttpResponse
         .status<ErrorDTO>(NOT_IMPLEMENTED)
         .body(localizationService.localizeError(NotImplemented(httpRequest.path), locale))
   }

   @Error(global = true, exception = ConversionErrorException::class)
   fun conversionError(httpRequest: HttpRequest<*>, exception: ConversionErrorException): HttpResponse<ErrorDTO> {
      logger.warn("Unable to parse request body", exception)

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
               localizationService.localizeError(
                  localizationCode = ConversionError(argument.name, conversionError.originalValue.orElse(null)),
                  locale = locale,
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
   fun operationNotPermitted(httpRequest: HttpRequest<*>, exception: OperationNotPermittedException): HttpResponse<ErrorDTO> {
      logger.warn("An operation that is not permitted was initiated", exception)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         ErrorDTO(
            message = localizationService.localize(messageKey = exception.messageTemplate, locale = locale, arguments = emptyArray()),
            path = exception.path,
            code = exception.messageTemplate
         )
      )
   }

   private fun processBadRequest(argumentName: String, argumentValue: Any?, locale: Locale): HttpResponse<ErrorDTO> {
      return badRequest(
         localizationService.localizeError(ConversionError(argumentName, argumentValue), locale, argumentName)
      )
   }

   @Error(global = true, exception = UnsatisfiedRouteException::class)
   fun unsatisfiedRouteException(httpRequest: HttpRequest<*>, exception: UnsatisfiedRouteException): HttpResponse<ErrorDTO> {
      logger.trace("Unsatisfied Route Error", exception)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         localizationService.localizeError(localizationCode = RouteError(exception.argument.name), locale = locale)
      )
   }

   @Error(global = true, exception = PageOutOfBoundsException::class)
   fun pageOutOfBoundsExceptionHandler(exception: PageOutOfBoundsException): HttpResponse<ErrorDTO> {
      logger.error("Page out of bounds was requested {}", exception.toString())

      return noContent()
   }

   @Error(global = true, exception = NotFoundException::class)
   fun notFoundExceptionHandler(httpRequest: HttpRequest<*>, notFoundException: NotFoundException): HttpResponse<ErrorDTO> {
      logger.trace("Not Found Error {}", notFoundException.message)

      val locale = httpRequest.findLocaleWithDefault()

      return notFound(
         localizationService.localizeError(localizationCode = NotFound(notFoundException.notFound), locale = locale)
      )
   }

   @Error(global = true, exception = ValidationException::class)
   fun validationException(httpRequest: HttpRequest<*>, validationException: ValidationException): HttpResponse<List<ErrorDTO>> {
      logger.trace("Validation Error", validationException)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         validationException.errors.map { validationError: ValidationError ->
            localizationService.localizeError(validationError.localizationCode, locale, validationError.path)
         }
      )
   }

   @Error(global = true, exception = DataIntegrityViolationException::class)
   fun constraintViolationException(httpRequest: HttpRequest<*>, dataIntegrityViolationException: DataIntegrityViolationException): HttpResponse<ErrorDTO> {
      logger.trace("DataIntegrityViolationException Error", dataIntegrityViolationException)

      // Return a brief message to client
      val detail = dataIntegrityViolationException.localizedMessage.substringAfterLast("Detail:").trim()

      return HttpResponse
         .status<ErrorDTO>(CONFLICT)
         .body(
            ErrorDTO(
               message = detail,
               code = "system.data.access.exception"
            )
         )
   }

   @Error(global = true, exception = ConstraintViolationException::class)
   fun constraintViolationException(httpRequest: HttpRequest<*>, constraintViolationException: ConstraintViolationException): HttpResponse<List<ErrorDTO>> {
      logger.trace("Constraint Violation Error", constraintViolationException)

      val locale = httpRequest.findLocaleWithDefault()

      return badRequest(
         constraintViolationException.constraintViolations.map {
            val field = buildPropertyPath(rootPath = it.propertyPath)
            val value = if (it.invalidValue != null) it.invalidValue else EMPTY // just use the empty string if invalidValue is null to make the varargs call to localize happy

            ErrorDTO(
               message = localizationService.localize(it.constraintDescriptor.messageTemplate, locale, arguments = arrayOf(field, value.toString())),
               path = field,
               code = it.messageTemplate.removeSurrounding("{", "}")
            )
         }
      )
   }

   @Error(global = true, exception = AuthenticationException::class)
   fun authenticationExceptionHandler(httpRequest: HttpRequest<*>, authenticationException: AuthenticationException): HttpResponse<ErrorDTO> {
      logger.warn("AuthenticationException {}", authenticationException.localizedMessage)

      val userName = httpRequest.body.map { if (it is ObjectNode && it.has("username")) it.get("username").textValue() else null }.orElse(null)
      val locale = httpRequest.findLocaleWithDefault()

      return if (authenticationException.message.isDigits()) { // most likely store should have been provided
         val message = localizationService.localizeError(AccessDeniedStore(authenticationException.message!!), locale)

         HttpResponse.status<ErrorDTO>(UNAUTHORIZED).body(message)
      } else if (!authenticationException.message.isNullOrBlank() && authenticationException.message == "Credentials Do Not Match" && userName != null) {
         val message = localizationService.localizeError(AccessDeniedCredentialsDoNotMatch(userName), locale)

         HttpResponse.status<ErrorDTO>(UNAUTHORIZED).body(message)
      } else {
         val message = localizationService.localizeError(AccessDenied(), locale)

         HttpResponse.status<ErrorDTO>(FORBIDDEN).body(message)
      }
   }

   @Error(global = true, exception = AuthorizationException::class)
   fun authorizationExceptionHandler(httpRequest: HttpRequest<*>, authorizationException: AuthorizationException): HttpResponse<ErrorDTO> {
      logger.warn("AuthorizationException {}", authorizationException.localizedMessage)

      val locale = httpRequest.findLocaleWithDefault()

      return if (authorizationException.isForbidden) {
         val message = localizationService.localizeError(AccessDenied(), locale)

         HttpResponse
            .status<ErrorDTO>(FORBIDDEN)
            .body(message)
      } else {
         val message = localizationService.localizeError(NotLoggedIn(), locale)

         HttpResponse
            .status<ErrorDTO>(UNAUTHORIZED)
            .body(message)
      }
   }

   @Error(global = true, exception = AccessException::class)
   fun accessExceptionHandler(httpRequest: HttpRequest<*>, accessException: AccessException): HttpResponse<ErrorDTO> {
      logger.warn("Unauthorized exception", accessException)

      val locale = httpRequest.findLocaleWithDefault()

      return HttpResponse
         .status<ErrorDTO>(FORBIDDEN)
         .body(localizationService.localizeError(localizationCode = accessException.error, locale = locale))
   }

   @Error(global = true, exception = IOException::class)
   fun inputOutputExceptionHandler(httpRequest: HttpRequest<*>, exception: IOException) {
      logger.trace("InputOutput Exception", exception)

      when (exception.message?.trim()?.toLowerCase()) {
         "an existing connection was forcibly closed by the remote host", "connection reset by peer" ->
            logger.warn("{} - {}:{}", exception.message, httpRequest.method, httpRequest.path)
         else ->
            logger.error("Unknown IOException occurred during request processing", exception)
      }
   }

   @Error(global = true, exception = PSQLException::class)
   fun sqlExceptionHandler(httpRequest: HttpRequest<*>, throwable: PSQLException): HttpResponse<ErrorDTO> {
      logger.error("SQL Error", throwable)

      val locale = httpRequest.findLocaleWithDefault()

      return serverError(
         ErrorDTO(
            localizationService.localize(
               localizationCode = InternalError(),
               locale = locale
            ),
            "system.data.access.exception"
         )
      )
   }

   @Error(global = true, exception = Throwable::class)
   fun allElseFailsExceptionHandler(httpRequest: HttpRequest<*>, throwable: Throwable): HttpResponse<ErrorDTO> {
      logger.error("Unknown Error", throwable)

      val locale = httpRequest.findLocaleWithDefault()

      return serverError(localizationService.localizeError(localizationCode = InternalError(), locale = locale))
   }

   private fun buildPropertyPath(rootPath: Path): String =
      rootPath.asSequence()
         .filter {
            it.name != "save" &&
               it.name != "create" &&
               it.name != "update" &&
               it.name != "dto" &&
               it.name != "vo" &&
               it.name != "fetchAll" &&
               it.name != "completeOrCancel" &&
               !it.name.startsWith("arg")
         }
         .joinToString(".")
}
