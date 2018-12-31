package com.hightouchinc.cynergi.middleware.controller.spi

import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.Service
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get

abstract class CrudControllerBase<T> (
   private val service: Service<T>
) {

   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      id: Long
   ): T {
      return service.findById(id = id) ?: throw NotFoundException(id)
   }
}
