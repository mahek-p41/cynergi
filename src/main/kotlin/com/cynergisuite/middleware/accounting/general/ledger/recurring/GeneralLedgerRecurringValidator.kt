package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerRecurringValidator @Inject constructor(
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val recurringTypeRepository: GeneralLedgerRecurringTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringValidator::class.java)

   fun validateCreate(dto: GeneralLedgerRecurringDTO, company: Company): GeneralLedgerRecurringEntity {
      logger.trace("Validating Create GeneralLedgerRecurring{}", dto)

      dto.lastTransferDate = null

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: Long, dto: GeneralLedgerRecurringDTO, company: Company): GeneralLedgerRecurringEntity {
      logger.debug("Validating Update GeneralLedgerRecurring{}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: GeneralLedgerRecurringDTO, company: Company): GeneralLedgerRecurringEntity {
      val source = sourceCodeRepository.findOne(dto.source!!.id!!, company)
      val recurringType = recurringTypeRepository.findOne(dto.type!!.value)

      doValidation { errors ->
         // non-nullable validations
         source ?: errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))
         recurringType ?: errors.add(ValidationError("type.value", NotFound(dto.type!!.value)))
      }

      return GeneralLedgerRecurringEntity(
         dto,
         source!!,
         recurringType!!
      )
   }
}
