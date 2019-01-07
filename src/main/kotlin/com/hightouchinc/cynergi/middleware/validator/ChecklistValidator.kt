package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.ChecklistDto
import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.service.ChecklistService
import com.hightouchinc.cynergi.middleware.validator.spi.NestedValidatorBase
import org.eclipse.collections.api.list.ImmutableList
import org.eclipse.collections.impl.factory.Lists
import javax.inject.Singleton

@Singleton
class ChecklistValidator(
   private val checklistService: ChecklistService
): NestedValidatorBase<ChecklistDto, String>() {
   override fun doValidateSave(dto: ChecklistDto, parent: String): ImmutableList<ValidationError> {
      return if (checklistService.exists(customerAccount = dto.customerAccount)) {
         Lists.immutable.of(ValidationError("cust_acct", ErrorCodes.Validation.DUPLICATE, Lists.immutable.of(dto.customerAccount)))
      } else {
         Lists.immutable.empty()
      }
   }

   override fun doValidateUpdate(dto: ChecklistDto, parent: String): ImmutableList<ValidationError> {
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

      return errors.toImmutable()
   }
}
