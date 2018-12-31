package com.hightouchinc.cynergi.middleware.exception

import com.hightouchinc.cynergi.middleware.domain.BadRequest
import com.hightouchinc.cynergi.middleware.domain.NotFound
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.status
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import javax.validation.ConstraintViolationException

@Controller
class Handler {

   @Error(global = true)
   fun notFoundExceptionHandler(notFoundException: NotFoundException): HttpResponse<NotFound> {
      val response: MutableHttpResponse<NotFound> = status(HttpStatus.NOT_FOUND)

      response.body(notFoundException.notFound)
      response.contentType(MediaType.APPLICATION_JSON_TYPE)

      return response
   }

   @Error(global = true)
   fun constraintViolationException(constraintViolationException: ConstraintViolationException): HttpResponse<BadRequest> {
      val response: MutableHttpResponse<BadRequest> = status(HttpStatus.BAD_REQUEST)

      response.body(BadRequest())
      response.contentType(MediaType.APPLICATION_JSON_TYPE)

      return response
   }
}
