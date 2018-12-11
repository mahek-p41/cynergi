package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.data.access.BusinessDataAccessObject
import com.hightouchinc.cynergi.middleware.data.transfer.Business
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import io.micronaut.context.annotation.Parameter
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/api/v1/businesses")
class BusinessController(
   private val businessDataAccessObject: BusinessDataAccessObject
) {

   @Get("/{id}")
   fun fetchOne(
      @Parameter("id") id: Long
   ): Business {
      return businessDataAccessObject.fetchOne(id = id) ?: throw NotFoundException(id)
   }
}
