package com.hightouchinc.cynergi.middleware.validator.spi

import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.extensions.isNotEmpty
import com.hightouchinc.cynergi.middleware.validator.NestedValidator
import org.eclipse.collections.api.list.ImmutableList

abstract class NestedValidatorBase<DTO, PARENT>: NestedValidator<DTO, PARENT> {
   protected abstract fun doValidateSave(dto: DTO, parent: PARENT): ImmutableList<ValidationError>
   protected abstract fun doValidateUpdate(dto: DTO, parent: PARENT): ImmutableList<ValidationError>

   override fun validateSave(dto: DTO, parent: PARENT) {
      val errors = doValidateSave(dto = dto, parent = parent)
      listOf<Any>().isNotEmpty()
      if (errors.isNotEmpty()) {
         throw ValidationException(errors)
      }
   }

   override fun validateUpdate(dto: DTO, parent: PARENT) {
      val errors = doValidateUpdate(dto = dto, parent = parent)

      if (errors.isNotEmpty()) {
         throw ValidationException(errors)
      }
   }
}
