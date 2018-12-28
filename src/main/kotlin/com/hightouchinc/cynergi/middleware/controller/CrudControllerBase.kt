package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.IdentityService
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get

abstract class CrudControllerBase<T> (
   private val identityService: IdentityService<T>
) {

   @Get(value = "/{id}", processes = [APPLICATION_JSON], produces = [APPLICATION_JSON])
   fun fetchOne(
      id: Long
   ): T {
      return identityService.findById(id = id) ?: throw NotFoundException(id)
   }
}
