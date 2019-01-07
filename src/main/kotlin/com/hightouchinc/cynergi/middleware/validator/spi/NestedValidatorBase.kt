package com.hightouchinc.cynergi.middleware.validator.spi

import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.validator.NestedValidator

abstract class NestedValidatorBase<DTO, PARENT>: NestedValidator<DTO, PARENT> {
   protected abstract fun doValidateSave(dto: DTO, parent: PARENT): List<ValidationError>
   protected abstract fun doValidateUpdate(dto: DTO, parent: PARENT): List<ValidationError>

   override fun validateSave(dto: DTO, parent: PARENT) {
      val errors = doValidateSave(dto = dto, parent = parent)

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
