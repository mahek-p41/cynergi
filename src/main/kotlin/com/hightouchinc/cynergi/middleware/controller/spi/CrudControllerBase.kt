package com.hightouchinc.cynergi.middleware.controller.spi

import com.hightouchinc.cynergi.middleware.controller.CrudController
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.CrudService
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import org.springframework.validation.annotation.Validated

abstract class CrudControllerBase<DTO> (
   private val crudService: CrudService<DTO>
): CrudController<DTO> {

   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   override fun fetchOne(
      id: Long
   ): DTO {
      return crudService.findById(id = id) ?: throw NotFoundException(id)
   }
}
