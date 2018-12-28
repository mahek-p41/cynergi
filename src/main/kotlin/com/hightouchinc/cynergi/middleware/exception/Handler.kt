package com.hightouchinc.cynergi.middleware.exception

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error

@Controller
class Handler {

   @Error(global = true)
   fun notFoundExceptionHandler(notFoundException: NotFoundException): HttpResponse<NotFound> {
      val response: MutableHttpResponse<NotFound> = HttpResponse.status(HttpStatus.NOT_FOUND)

      response.body(notFoundException.notFound)
      response.contentType(MediaType.APPLICATION_JSON_TYPE)

      return response
   }
}
