package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.data.access.BusinessDataAccessObject
import io.micronaut.http.annotation.Controller

@Controller("/api/v1/businesses")
class BusinessController(
   private val businessDataAccessObject: BusinessDataAccessObject
) {
   fun fetchOne() {

   }
}
