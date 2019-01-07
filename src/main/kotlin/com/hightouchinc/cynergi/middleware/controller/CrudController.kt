package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import javax.validation.Valid

/**
 * Basic contract that a top level CRUD controller should provide.
 *
 * @param DTO the data transfer object that is the "web" representation of the Entity being manipulated via the CRUD pattern
 *
 * @author garym@hightouchinc.com
 */
interface CrudController<DTO> {

   /**
    * Defines the READ operation in the CRUD pattern via the GET HTTP verb
    *
    * @param id: Long defines the primary key that is to be used to look up the Entity from the database
    * @return the Entity if it is found
    * @throws NotFoundException if the Entity using the provided id was unable to be located.  This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    */
   @Throws(NotFoundException::class)
   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("id") id: Long
   ): DTO

   /**
    * Defines the CREATE operation in the CRUD pattern via the POST HTTP verb
    *
    * @param dto: DTO defines the request body from the HTTP POST which is expected to be in JSON format
    * @throws ValidationException when the dto is invalid.  This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    * @throws NotFoundException if the parentId was unable to be resolved.  This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    */
   @Throws(ValidationException::class)
   @Post(processes = [APPLICATION_JSON])
   fun save(
      @Valid @Body dto: DTO
   ): DTO

   /**
    * Defines the UPDATE operation in the CRUD pattern via the PUT HTTP verb
    *
    * @param dto: DTO defines the request body from the HTTP POST which is expected to be in JSON format
    * @throws ValidationException when the dto is invalid such as the ID of the Entity being updated is null. This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    * @throws NotFoundException if the parentId was unable to be resolved. This will be translated to a proper response in the [com.hightouchinc.cynergi.middleware.exception.Handler] controller
    */
   @Throws(ValidationException::class)
   @Put(processes = [APPLICATION_JSON])
   fun update(
      @Valid @Body dto: DTO
   ): DTO
}
