package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.shipvia.infrastructure.ShipViaRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class ShipViaValidator @Inject constructor(
   private val shipViaRepository: ShipViaRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid vo: ShipViaValueObject, company: Company): ShipViaEntity {
      logger.trace("Validating Save ShipVia {}", vo)

      doValidation { errors ->  } // TODO add checks for duplicates

      return ShipViaEntity(vo, company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, vo: ShipViaValueObject): ShipViaEntity {
      logger.trace("Validating Update ShipVia {}", vo)

      val existing = shipViaRepository.findOne(id) ?: throw NotFoundException(id)

      doValidation { errors ->  } // TODO add checks for duplicates

      return existing.copy(description = vo.description!!)
   }
}
