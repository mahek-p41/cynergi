package com.hightouchinc.cynergi.middleware.controller

import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import javax.validation.Valid

/**
 * Basic contract that a CRUD controller should provide.  Implementing this interface should be a basic thing that is
 * done unless special circumstances come up within the domain that the methods defined here can't full fill.  This doesn't
 * mean that if there are additional methods that can be full filled by implementing this interface and adding additional
 * methods with appropriate HTTP verbs.
 */
interface CrudController<DTO> {

   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("id") id: Long
   ): DTO

   @Post(processes = [APPLICATION_JSON])
   fun save(
      @Valid @Body dto: DTO
   ): DTO

   @Put(processes = [APPLICATION_JSON])
   fun update(
      @Valid @Body dto: DTO
   ): DTO
}
