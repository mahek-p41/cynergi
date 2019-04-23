package com.hightouchinc.cynergi.middleware.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.micronaut.views.View

@Secured(IS_ANONYMOUS)
@Controller("/")
class ViewsController {

   @Get("/", produces = [MediaType.TEXT_HTML])
   @View("login")
   fun root(): HttpResponse<Any> {
      return HttpResponse.ok()
   }

   @Get("/login", produces = [MediaType.TEXT_HTML])
   @View("login")
   fun login(): HttpResponse<Any> {
      return HttpResponse.ok()
   }
}
