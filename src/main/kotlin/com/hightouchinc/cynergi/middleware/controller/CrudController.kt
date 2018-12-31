package com.hightouchinc.cynergi.middleware.controller

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import javax.validation.Valid

interface CrudController<DTO> {

   @Get(value = "/{id}", produces = [MediaType.APPLICATION_JSON])
   fun fetchOne(
      id: Long
   ): DTO

   @Post(processes = [MediaType.APPLICATION_JSON])
   fun save(
      @Valid @Body dto: DTO
   ): DTO

   @Put(processes = [MediaType.APPLICATION_JSON])
   fun update(
      @Valid @Body dto: DTO
   ): DTO
}
