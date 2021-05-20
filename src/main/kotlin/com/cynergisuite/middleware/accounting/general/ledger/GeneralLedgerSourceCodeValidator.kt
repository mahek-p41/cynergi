package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerSourceCodeValidator @Inject constructor(
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSourceCodeValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: GeneralLedgerSourceCodeDTO, company: Company): GeneralLedgerSourceCodeEntity {
      logger.trace("Validating Save GeneralLedgerSourceCode {}", dto)

      doValidation { errors -> doCreateValidation(errors, dto, company) }

      return GeneralLedgerSourceCodeEntity(dto)
   }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateUpdate(id: UUID, dto: GeneralLedgerSourceCodeDTO, company: Company): GeneralLedgerSourceCodeEntity {
      logger.trace("Validating Update GeneralLedgerSourceCode {}", dto)

      val existing = generalLedgerSourceCodeRepository.findOne(id, company) ?: throw NotFoundException(id)

      doValidation { errors -> doUpdateValidation(errors, dto) }

      return existing.copy(value = dto.value!!, description = dto.description!!)
   }

   private fun doUpdateValidation(errors: MutableSet<ValidationError>, dto: GeneralLedgerSourceCodeDTO) {
      if (dto.value == null) {
         errors.add(ValidationError("value", NotNull("value")))
      }

      if (dto.description == null) {
         errors.add(ValidationError("description", NotNull("description")))
      }
   }

   private fun doCreateValidation(errors: MutableSet<ValidationError>, dto: GeneralLedgerSourceCodeDTO, company: Company) {
      doUpdateValidation(errors, dto)

      if (generalLedgerSourceCodeRepository.exists(dto.value!!, company)) {
         errors.add(ValidationError("value", Duplicate("value")))
      }
   }
}
