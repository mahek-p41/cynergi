package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
import javax.validation.Valid

/**
 * Basic contract a controller should follow that has a parent
 *
 * @param DTO the data transfer object that represents the Entity being manipulated via the CRUD pattern
 * @param PARENT the parent of the Entity being manipulated such as the parent of a Store being a Company
 *
 * @author garym@hightouchinc.com
 */
@Validated
interface NestedCrudController<DTO, PARENT> {

   /**
    * Defines the READ operation in the CRUD pattern via the GET HTTP verb
    *
    * @param parentId: PARENT defines the parent for this entity such as a company owning a store
    * @param id: Long defines the primary key that is to be used to look up the Entity from the database
    * @return the Entity if it is found
    * @throws NotFoundException if the Entity using the provided id was unable to be located.  This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    */
   @Throws(NotFoundException::class)
   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("parentId") parentId: PARENT,
      @QueryValue("id") id: Long
   ): DTO

   /**
    * Defines the CREATE operation in the CRUD pattern via the POST HTTP verb
    *
    * @param parentId: PARENT defines the parent for this entity such as a company owning a store
    * @param dto: DTO defines the request body from the HTTP POST which is expected to be in JSON format
    * @throws ValidationException when the dto is invalid.  This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    * @throws NotFoundException if the parentId was unable to be resolved.  This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    */
   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun save(
      @QueryValue("parentId") parentId: PARENT,
      @Valid @Body dto: DTO
   ): DTO

   /**
    * Defines the UPDATE operation in the CRUD pattern via the PUT HTTP verb
    *
    * @param parentId: PARENT defines the parent for this entity such as a company owning a store
    * @param dto: DTO defines the request body from the HTTP POST which is expected to be in JSON format
    * @throws ValidationException when the dto is invalid such as the ID of the Entity being updated is null. This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    * @throws NotFoundException if the parentId was unable to be resolved. This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    */
   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun update(
      @QueryValue("parentId") parentId: PARENT,
      @Valid @Body dto: DTO
   ): DTO
}
