package com.hightouchinc.cynergi.middleware.exception

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error

@Controller
class Handler {

   @Error(global = true)
   fun notFoundExceptionHandler(notFoundException: NotFoundException): HttpResponse<String> {
      return HttpResponse.notFound(notFoundException.message)
   }
}
