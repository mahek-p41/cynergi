package com.cynergisuite.middleware.shipvia

import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.SystemCode.NotFound
import com.cynergisuite.middleware.localization.Validation.NotNull
import com.cynergisuite.middleware.shipvia.infrastructure.ShipViaRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShipViaValidator @Inject constructor(
   private val shipViaRepository: ShipViaRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaValidator::class.java)

   @Throws(ValidationException::class)
   fun validateSave(vo: ShipViaValueObject){
      logger.trace("Validating Save ShipVia {}", vo)

      val errors = mutableSetOf<ValidationError>()
      if (errors.isNotEmpty()) {
         logger.debug("Validating Save Employee {} had errors", vo)

         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: ShipViaValueObject){
      logger.trace("Validating Update ShipVia {}", vo)

      val errors = mutableSetOf<ValidationError>()
      val id = vo.id

      if (id == null) {
         errors.add(element = ValidationError("id", NotNull, listOf("id")))
      } else if ( !shipViaRepository.exists(id = id) ) {
         errors.add(ValidationError("id", NotFound, listOf(id)))
      }

      if (errors.isNotEmpty()) {
         logger.debug("Validating Update Employee {} had errors", vo)

         throw ValidationException(errors)
      }
   }
}
