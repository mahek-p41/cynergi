package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.ChecklistDto
import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.extensions.isNotEmpty
import com.hightouchinc.cynergi.middleware.service.ChecklistService
import org.eclipse.collections.impl.factory.Lists
import javax.inject.Singleton

@Singleton
class ChecklistValidator(
   private val checklistService: ChecklistService
) {

   @Throws(ValidationException::class)
   fun validateSave(dto: ChecklistDto, parent: String) {
      val errors = if (checklistService.exists(customerAccount = dto.customerAccount)) {
         Lists.immutable.of(ValidationError("cust_acct", ErrorCodes.Validation.DUPLICATE, Lists.immutable.of(dto.customerAccount)))
      } else {
         Lists.immutable.empty()
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(dto: ChecklistDto, parent: String) {
      val errors = Lists.mutable.of<ValidationError>()
      val id = dto.id

      if (id == null) {
         errors.add(element = ValidationError("id", ErrorCodes.Validation.NOT_NULL, Lists.immutable.of("id")))
      } else {
         val existingChecklist: ChecklistDto? = checklistService.fetchById(id = id)

         if (existingChecklist == null) {
            errors.add(element = ValidationError("id", ErrorCodes.System.NOT_FOUND, Lists.immutable.of(id)))
         } else if (existingChecklist.customerAccount != dto.customerAccount) {
            errors.add(element = ValidationError("cust_acct", ErrorCodes.Validation.NOT_UPDATABLE, Lists.immutable.of(dto.customerAccount)))
         }
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors = errors.toImmutable())
      }
   }
}
