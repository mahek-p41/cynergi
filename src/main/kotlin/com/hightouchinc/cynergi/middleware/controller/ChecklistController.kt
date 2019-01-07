package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.NestedCrudControllerBase
import com.hightouchinc.cynergi.middleware.entity.ChecklistDto
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.ChecklistService
import com.hightouchinc.cynergi.middleware.validator.ChecklistValidator
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import javax.inject.Inject

/**
 * Defines the primary CRUD controller for the verification process
 *
 * @param checklistService defines that the [ChecklistService] instance should be injected by the container
 * @param checklistValidator defines that the [ChecklistValidator] instance should be injected by the container
 *
 * @author garym@hightouchinc.com
 */
@Controller("/api/company/{parentId}/verification/")
class ChecklistController @Inject constructor(
   private val checklistService: ChecklistService,
   private val checklistValidator: ChecklistValidator
): NestedCrudControllerBase<ChecklistDto, String>(
   crudService = checklistService
) {

   @Post(processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   override fun save(
      @QueryValue("parentId") parentId: String,
      @Body dto: ChecklistDto
   ): ChecklistDto {
      checklistValidator.validateSave(dto = dto, parent = parentId)

      return checklistService.save(dto = dto, parent = parentId)
   }

   @Put(processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   override fun update(
      @QueryValue("parentId") parentId: String,
      @Body dto: ChecklistDto
   ): ChecklistDto {
      checklistValidator.validateUpdate(dto = dto, parent = parentId)

      return checklistService.update(dto = dto, parent = parentId)
   }
}
