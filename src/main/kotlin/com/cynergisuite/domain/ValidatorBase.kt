package com.cynergisuite.domain

import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class ValidatorBase {
   companion object {
      val logger: Logger = LoggerFactory.getLogger(ValidatorBase::class.java)
   }

   @Throws(ValidationException::class)
   protected fun doValidation(validator: (MutableSet<ValidationError>) -> Unit) {
      val errors = mutableSetOf<ValidationError>()

      validator(errors)

      if (errors.isNotEmpty()) {
         logger.warn("Validation encountered errors {}", errors)

         throw ValidationException(errors)
      }
   }

   protected fun <RESULT> doValidationWithResult(validator: (MutableSet<ValidationError>) -> RESULT?): RESULT {
      val errors = mutableSetOf<ValidationError>()
      val result: RESULT? = validator(errors)

      if (errors.isNotEmpty()) {
         logger.warn("Validation encountered errors {}", errors)

         throw ValidationException(errors)
      }

      return result!!
   }
}
