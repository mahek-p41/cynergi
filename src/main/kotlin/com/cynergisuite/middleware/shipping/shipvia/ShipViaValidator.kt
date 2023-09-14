package com.cynergisuite.middleware.shipping.shipvia

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.shipping.shipvia.infrastructure.ShipViaRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Singleton
class ShipViaValidator @Inject constructor(
   private val shipViaRepository: ShipViaRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid vo: ShipViaDTO, company: CompanyEntity): ShipViaEntity {
      logger.trace("Validating Save ShipVia {}", vo)

      doValidation { errors -> doSharedValidation(errors, vo, company) }

      return ShipViaEntity(vo, company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: UUID, @Valid vo: ShipViaDTO, company: CompanyEntity): ShipViaEntity {
      logger.trace("Validating Update ShipVia {}", vo)

      val existing = shipViaRepository.findOne(id, company) ?: throw NotFoundException(id)

      doValidation { errors -> doSharedValidation(errors, vo, company) }

      return existing.copy(description = vo.description!!)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, vo: ShipViaDTO, company: CompanyEntity) {
      if (shipViaRepository.exists(vo.description!!, company)) {
         errors.add(ValidationError("description", Duplicate(vo.description)))
      }
   }
}
