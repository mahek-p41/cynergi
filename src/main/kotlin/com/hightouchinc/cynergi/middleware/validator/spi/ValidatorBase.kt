package com.hightouchinc.cynergi.middleware.validator.spi

import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.validator.Validator
import org.hibernate.validator.internal.engine.ConstraintViolationImpl
import org.hibernate.validator.internal.engine.path.PathImpl
import javax.validation.ConstraintViolation

abstract class ValidatorBase<DTO>: Validator<DTO> {

   protected abstract fun doValidateSave(dto: DTO): List<ValidationError>
   protected abstract fun doValidateUpdate(dto: DTO): List<ValidationError>

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
