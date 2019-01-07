package com.hightouchinc.cynergi.middleware.validator.spi

import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.extensions.isNotEmpty
import com.hightouchinc.cynergi.middleware.validator.Validator
import org.eclipse.collections.api.list.ImmutableList

abstract class ValidatorBase<DTO>: Validator<DTO> {

   protected abstract fun doValidateSave(dto: DTO): ImmutableList<ValidationError>
   protected abstract fun doValidateUpdate(dto: DTO): ImmutableList<ValidationError>

   override fun validateSave(dto: DTO) {
      val errors = doValidateSave(dto = dto)

      if (errors.isNotEmpty()) {
         throw ValidationException(errors)
      }
   }

   override fun validateUpdate(dto: DTO) {
      val errors = doValidateUpdate(dto = dto)

      if (errors.isNotEmpty()) {
         throw ValidationException(errors)
      }
   }
}
