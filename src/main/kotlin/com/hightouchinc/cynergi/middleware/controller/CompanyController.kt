package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.data.transfer.Business
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.BusinessService
import io.micronaut.context.annotation.Parameter
import io.micronaut.http.MediaType
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/api/v1/companies")
class CompanyController(
   private val businessService: BusinessService
) {

   @Get(value = "/{id}", processes = [APPLICATION_JSON])
   fun fetchOne(
      id: Long
   ): Business {
      return businessService.findById(id = id) ?: throw NotFoundException(id)
   }
}
