package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.MessageCodes.System.NOT_FOUND
import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeValidator @Inject constructor (
   private val employeeService: EmployeeService
) {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeValidator::class.java)

   @Throws(ValidationException::class)
   fun validateSave(vo: EmployeeValueObject) {
      logger.debug("Validating Save Employee {}", vo)

      val errors = mutableSetOf<ValidationError>() // TODO some more validation when the Employee class gets richer

      if (errors.isNotEmpty()) {
         logger.debug("Validating Save Employee {} had errors", vo)

         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: EmployeeValueObject) {
      logger.debug("Validating Update Employee {}", vo)

      val errors = mutableSetOf<ValidationError>() // TODO some more validation when the Employee class gets richer
      val id = vo.id

      if (id == null) {
         errors.add(element = ValidationError("id", NOT_NULL, listOf("id")))
      } else if ( !employeeService.exists(id = id) ) {
         errors.add(ValidationError("id", NOT_FOUND, listOf(id)))
      }

      if (errors.isNotEmpty()) {
         logger.debug("Validating Update Employee {} had errors", vo)

         throw ValidationException(errors)
      }
   }
}
