package com.hightouchinc.cynergi.middleware.controller.spi

import com.hightouchinc.cynergi.middleware.controller.NestedCrudController
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.NestedCrudService
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated

/**
 * Basic helper implementation of the [NestedCrudController]
 *
 * @param DTO the data transfer object that represents the Entity being manipulated via the CRUD pattern
 * @param PARENT the parent of the Entity being manipulated such as the parent of a Store being a Company
 * @property crudService holds an implementation of the [NestedCrudService] that is used for calling the fetchById
 *
 * Note: That when using this the path defined on the child [io.micronaut.http.annotation.Controller] will need to
 * have a {parentId} path param defined in it.
 *
 * @author garym
 */
@Validated
abstract class NestedCrudControllerBase<DTO, PARENT> (
   private val crudService: NestedCrudService<DTO, PARENT>
): NestedCrudController<DTO, PARENT> {

   @Throws(NotFoundException::class)
   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   override fun fetchOne(
      @QueryValue("parentId") parentId: PARENT,
      @QueryValue("id") id: Long
   ): DTO {
      return crudService.fetchById(id = id) ?: throw NotFoundException(id)
   }
}
