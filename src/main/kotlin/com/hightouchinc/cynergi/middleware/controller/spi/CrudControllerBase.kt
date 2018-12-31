package com.hightouchinc.cynergi.middleware.controller.spi

import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.CrudService
import com.hightouchinc.cynergi.middleware.validator.Validator
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import org.springframework.validation.annotation.Validated
import javax.validation.Valid

@Validated
abstract class CrudControllerBase<DTO> (
   private val crudService: CrudService<DTO>,
   private val validator: Validator<DTO>
) {

   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      id: Long
   ): DTO {
      return crudService.findById(id = id) ?: throw NotFoundException(id)
   }

   @Post(processes = [APPLICATION_JSON])
   fun save(
      @Valid dto: DTO
   ): DTO {
      validator.validateSave(dto = dto)

      return crudService.save(dto = dto)
   }

   @Put(processes = [APPLICATION_JSON])
   fun update(
      @Valid dto: DTO
   ): DTO {
      validator.validateUpdate(dto = dto)

      return crudService.update(dto = dto)
   }
}
