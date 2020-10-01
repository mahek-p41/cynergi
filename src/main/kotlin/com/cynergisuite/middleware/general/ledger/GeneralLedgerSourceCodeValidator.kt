package com.cynergisuite.middleware.general.ledger

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerSourceCodeValidator @Inject constructor(
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val companyRepository: CompanyRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSourceCodeValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: GeneralLedgerSourceCodeDTO, company: Company): GeneralLedgerSourceCodeEntity {
      logger.trace("Validating Save GeneralLedgerSourceCode {}", dto)

      doValidation { errors -> doSharedValidation(errors, dto, company) }

      return GeneralLedgerSourceCodeEntity(dto = dto, company = company)
   }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateUpdate(id: Long, dto: GeneralLedgerSourceCodeDTO, company: Company): GeneralLedgerSourceCodeEntity {
      logger.trace("Validating Update GeneralLedgerSourceCode {}", dto)

      val existing = generalLedgerSourceCodeRepository.findOne(id, company) ?: throw NotFoundException(id)

      doValidation { errors -> doSharedValidation(errors, dto, company) }

      return existing.copy(value = dto.value!!, description = dto.description!!)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, dto: GeneralLedgerSourceCodeDTO, company: Company) {
      if (dto.value == null) {
         errors.add(ValidationError("value", NotNull("value")))
      }

      if (dto.description == null) {
         errors.add(ValidationError("description", NotNull("description")))
      }

      if (generalLedgerSourceCodeRepository.exists(dto.value!!, company)) {
         errors.add(ValidationError("value", Duplicate("value")))
      }
   }
}
