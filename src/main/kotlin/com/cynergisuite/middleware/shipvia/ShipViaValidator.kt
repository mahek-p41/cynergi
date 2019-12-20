package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.shipvia.infrastructure.ShipViaRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShipViaValidator @Inject constructor(
   private val shipViaRepository: ShipViaRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaValidator::class.java)

   @Throws(ValidationException::class)
   fun validateSave(vo: ShipViaValueObject){
      logger.trace("Validating Save ShipVia {}", vo)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: ShipViaValueObject){
      logger.trace("Validating Update ShipVia {}", vo)

      doValidation { errors ->
         val id = vo.id

         if (id == null) {
            errors.add(element = ValidationError("id", NotNull("id")))
         } else if ( !shipViaRepository.exists(id = id) ) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }
   }
}
