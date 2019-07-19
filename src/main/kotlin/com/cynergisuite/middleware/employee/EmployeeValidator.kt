package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeValidator @Inject constructor (
   private val employeeRepository: EmployeeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeValidator::class.java)

   @Throws(ValidationException::class)
   fun validateSave(vo: EmployeeValueObject) {
      logger.trace("Validating Save Employee {}", vo)

      val errors = mutableSetOf<ValidationError>() // TODO some more validation when the Employee class gets richer

      if (errors.isNotEmpty()) {
         logger.debug("Validating Save Employee {} had errors", vo)

         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: EmployeeValueObject) {
      logger.trace("Validating Update Employee {}", vo)

      val errors = mutableSetOf<ValidationError>() // TODO some more validation when the Employee class gets richer
      val id = vo.id

      if (id == null) {
         errors.add(element = ValidationError("id", NotNull("id")))
      } else if ( !employeeRepository.exists(id = id, loc = vo.loc!!) ) {
         errors.add(ValidationError("id", NotFound(id)))
      }

      if (errors.isNotEmpty()) {
         logger.debug("Validating Update Employee {} had errors", vo)

         throw ValidationException(errors)
      }
   }
}
